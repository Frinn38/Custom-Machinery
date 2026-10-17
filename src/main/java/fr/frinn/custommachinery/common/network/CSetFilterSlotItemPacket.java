package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.MachineComponentId;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CSetFilterSlotItemPacket(ItemStack stack, BlockPos pos, MachineComponentId<ItemMachineComponent> slotId) implements CustomPacketPayload {

    public static final Type<CSetFilterSlotItemPacket> TYPE = new Type<>(CustomMachinery.rl("set_filter_slot_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CSetFilterSlotItemPacket> CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            CSetFilterSlotItemPacket::stack,
            BlockPos.STREAM_CODEC,
            CSetFilterSlotItemPacket::pos,
            MachineComponentId.STREAM_CODEC,
            CSetFilterSlotItemPacket::slotId,
            (stack, pos, slotId) -> new CSetFilterSlotItemPacket(stack, pos, Utils.cast(slotId))
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
                            .getComponent(packet.slotId)
                            .ifPresent(component -> {
                                if(component.getType() == CMRegistration.ITEM_FILTER_MACHINE_COMPONENT.get())
                                    component.setItemStack(packet.stack);
                            });
                }
            });
    }
}
