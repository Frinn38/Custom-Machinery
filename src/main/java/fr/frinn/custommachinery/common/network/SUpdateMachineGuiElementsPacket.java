package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collections;
import java.util.List;

public record SUpdateMachineGuiElementsPacket(BlockPos pos, List<IGuiElement> elements) implements CustomPacketPayload {

    public static final Type<SUpdateMachineGuiElementsPacket> TYPE = new Type<>(CustomMachinery.rl("update_machine_gui_elements"));

    public static final StreamCodec<FriendlyByteBuf, SUpdateMachineGuiElementsPacket> CODEC = StreamCodec.ofMember(SUpdateMachineGuiElementsPacket::write, SUpdateMachineGuiElementsPacket::read);

    @Override
    public Type<SUpdateMachineGuiElementsPacket> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        if(this.elements == null || this.elements.isEmpty())
            buf.writeBoolean(true);
        else {
            buf.writeBoolean(false);
            IGuiElement.CODEC.listOf().toNetwork(this.elements, buf);
        }
    }

    public static SUpdateMachineGuiElementsPacket read(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        if(buf.readBoolean())
            return new SUpdateMachineGuiElementsPacket(pos, Collections.emptyList());
        else
            return new SUpdateMachineGuiElementsPacket(pos, IGuiElement.CODEC.listOf().fromNetwork(buf));
    }

    public static void handle(SUpdateMachineGuiElementsPacket packet, IPayloadContext context) {
        if(context.flow().isClientbound())
            context.enqueueWork(() -> ClientPacketHandler.handleUpdateMachineGuiElementsPacket(packet.pos, packet.elements));
    }
}
