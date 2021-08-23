package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkManager {

    private static int index = -1;

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(CustomMachinery.MODID, "network_channel"))
            .clientAcceptedVersions("1"::equals)
            .serverAcceptedVersions("1"::equals)
            .networkProtocolVersion(() -> "1")
            .simpleChannel();

    public static void registerMessages() {
        CHANNEL.registerMessage(index++, SUpdateMachinesPacket.class, SUpdateMachinesPacket::encode, SUpdateMachinesPacket::decode, SUpdateMachinesPacket::handle);
        CHANNEL.registerMessage(index++, CAddMachinePacket.class, CAddMachinePacket::encode, CAddMachinePacket::decode, CAddMachinePacket::handle);
        CHANNEL.registerMessage(index++, SUpdateContainerPacket.class, SUpdateContainerPacket::encode, SUpdateContainerPacket::decode, SUpdateContainerPacket::handle);
        CHANNEL.registerMessage(index++, SUpdateUpgradesPacket.class, SUpdateUpgradesPacket::encode, SUpdateUpgradesPacket::decode, SUpdateUpgradesPacket::handle);
        CHANNEL.registerMessage(index++, SCraftingManagerStatusChangedPacket.class, SCraftingManagerStatusChangedPacket::encode, SCraftingManagerStatusChangedPacket::decode, SCraftingManagerStatusChangedPacket::handle);
        CHANNEL.registerMessage(index++, SRefreshCustomMachineTilePacket.class, SRefreshCustomMachineTilePacket::encode, SRefreshCustomMachineTilePacket::decode, SRefreshCustomMachineTilePacket::handle);
        CHANNEL.registerMessage(index++, SLootTablesPacket.class, SLootTablesPacket::encode, SLootTablesPacket::decode, SLootTablesPacket::handle);
    }
}
