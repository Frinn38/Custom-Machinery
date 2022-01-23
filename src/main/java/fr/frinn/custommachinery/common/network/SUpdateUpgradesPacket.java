package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.upgrade.MachineUpgrade;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SUpdateUpgradesPacket {

    private final List<MachineUpgrade> upgrades;

    public SUpdateUpgradesPacket(List<MachineUpgrade> upgrades) {
        this.upgrades = upgrades;
    }

    public static void encode(SUpdateUpgradesPacket pkt, PacketBuffer buf) {
        buf.writeVarInt(pkt.upgrades.size());
        pkt.upgrades.forEach(upgrade -> {
            try {
                buf.func_240629_a_(MachineUpgrade.CODEC, upgrade);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static SUpdateUpgradesPacket decode(PacketBuffer buf) {
        List<MachineUpgrade> upgrades = new ArrayList<>();
        int size = buf.readVarInt();
        for(int i = 0; i < size; i++) {
            try {
                MachineUpgrade upgrade = buf.func_240628_a_(MachineUpgrade.CODEC);
                upgrades.add(upgrade);
            } catch (IOException e) {
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
