package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SUpdateUpgradesPacket {

    private final List<MachineUpgrade> upgrades;

    public SUpdateUpgradesPacket(List<MachineUpgrade> upgrades) {
        this.upgrades = upgrades;
    }

    public static void encode(SUpdateUpgradesPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.upgrades.size());
        pkt.upgrades.forEach(upgrade -> {
            try {
                buf.writeWithCodec(MachineUpgrade.CODEC, upgrade);
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        });
    }

    public static SUpdateUpgradesPacket decode(FriendlyByteBuf buf) {
        List<MachineUpgrade> upgrades = new ArrayList<>();
        int size = buf.readVarInt();
        for(int i = 0; i < size; i++) {
            try {
                MachineUpgrade upgrade = buf.readWithCodec(MachineUpgrade.CODEC);
                upgrades.add(upgrade);
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        }
        return new SUpdateUpgradesPacket(upgrades);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
            context.get().enqueueWork(() -> {
                CustomMachinery.UPGRADES.clear();
                CustomMachinery.UPGRADES.addAll(this.upgrades);
            });
        context.get().setPacketHandled(true);
    }
}
