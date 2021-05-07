package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkManager {

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(CustomMachinery.MODID, "network_channel"))
            .clientAcceptedVersions("1"::equals)
            .serverAcceptedVersions("1"::equals)
            .networkProtocolVersion(() -> "1")
            .simpleChannel();

    public static void registerMessages() {
        CHANNEL.registerMessage(0, SUpdateMachinesPacket.class, SUpdateMachinesPacket::encode, SUpdateMachinesPacket::decode, SUpdateMachinesPacket::handle);
        CHANNEL.registerMessage(1, SCraftingManagerErrorPacket.class, SCraftingManagerErrorPacket::encode, SCraftingManagerErrorPacket::decode, SCraftingManagerErrorPacket::handle);
        CHANNEL.registerMessage(2, SUpdateCustomTilePacket.class, SUpdateCustomTilePacket::encode, SUpdateCustomTilePacket::decode, SUpdateCustomTilePacket::handle);
        CHANNEL.registerMessage(3, SUpdateCustomTileLightPacket.class, SUpdateCustomTileLightPacket::encode, SUpdateCustomTileLightPacket::decode, SUpdateCustomTileLightPacket::handle);
        CHANNEL.registerMessage(4, CAddMachinePacket.class, CAddMachinePacket::encode, CAddMachinePacket::decode, CAddMachinePacket::handle);
        CHANNEL.registerMessage(5, SUpdateContainerPacket.class, SUpdateContainerPacket::encode, SUpdateContainerPacket::decode, SUpdateContainerPacket::handle);
    }
}
