package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CSetFilterSlotItemPacket(ItemStack stack, BlockPos pos, String slotId) implements CustomPacketPayload {

    public static final Type<CSetFilterSlotItemPacket> TYPE = new Type<>(CustomMachinery.rl("set_filter_slot_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CSetFilterSlotItemPacket> CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            CSetFilterSlotItemPacket::stack,
            BlockPos.STREAM_CODEC,
            CSetFilterSlotItemPacket::pos,
            ByteBufCodecs.STRING_UTF8,
            CSetFilterSlotItemPacket::slotId,
            CSetFilterSlotItemPacket::new
    );

    @Override
    public Type<CSetFilterSlotItemPacket> type() {
        return TYPE;
    }

    public static void handle(CSetFilterSlotItemPacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player)
            context.enqueueWork(() -> {
                if(player.level().getBlockEntity(packet.pos) instanceof CustomMachineTile machine) {
                    machine.getComponentManager()
                            .getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                            .flatMap(handler -> handler.getComponentForID(packet.slotId))
                            .ifPresent(component -> {
                                if(component.getType() == Registration.ITEM_FILTER_MACHINE_COMPONENT.get())
                                    component.setItemStack(packet.stack);
                            });
                }
            });
    }
}
