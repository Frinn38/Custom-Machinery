package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public record CTransferRecipePacket(int containerId, List<Triple<Integer, Integer, Integer>> operations, boolean maxTransfer) implements CustomPacketPayload {

    public static final Type<CTransferRecipePacket> TYPE = new Type<>(CustomMachinery.rl("transfer_recipe"));

    private static final StreamCodec<ByteBuf, Triple<Integer, Integer, Integer>> BASE_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            Triple::getLeft,
            ByteBufCodecs.VAR_INT,
            Triple::getMiddle,
            ByteBufCodecs.VAR_INT,
            Triple::getRight,
            Triple::of
    );
    public static final StreamCodec<ByteBuf, CTransferRecipePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            CTransferRecipePacket::containerId,
            BASE_CODEC.apply(ByteBufCodecs.list()),
            CTransferRecipePacket::operations,
            ByteBufCodecs.BOOL,
            CTransferRecipePacket::maxTransfer,
            CTransferRecipePacket::new
    );

    @Override
    public Type<CTransferRecipePacket> type() {
        return TYPE;
    }

    public static void handle(CTransferRecipePacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player && player.containerMenu instanceof CustomMachineContainer container && container.containerId == packet.containerId) {
            for(Triple<Integer, Integer, Integer> operation : packet.operations) {
                if(operation.getLeft() < 0 || operation.getLeft() >= container.slots.size())
                    continue;
                Slot from = container.getSlot(operation.getLeft());

                if(operation.getMiddle() < 0 || operation.getMiddle() >= container.slots.size())
                    continue;
                Slot to = container.getSlot(operation.getMiddle());

                int amount = operation.getRight();

                if(from.getItem().isEmpty() || from.getItem().getCount() < amount || !from.allowModification(player))
                    continue;

                if(!to.getItem().isEmpty() && !ItemStack.isSameItemSameComponents(to.getItem(), from.getItem())) {
                    ItemHandlerHelper.giveItemToPlayer(player, to.remove(to.getItem().getCount()));
                    if(!to.getItem().isEmpty())
                        continue;
                }

                int toTransfer = packet.maxTransfer ? from.getItem().getCount() : amount;
                if(!to.getItem().isEmpty())
                    toTransfer = Math.min(toTransfer, to.getItem().getMaxStackSize() - to.getItem().getCount());

                ItemStack stack = from.getItem().copyWithCount(toTransfer);
                if(!to.mayPlace(stack))
                    continue;

                ItemStack remaining = to.safeInsert(stack);
                from.remove(toTransfer - remaining.getCount());
            }
        }
    }
}
