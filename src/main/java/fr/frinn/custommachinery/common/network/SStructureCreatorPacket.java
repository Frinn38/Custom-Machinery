package fr.frinn.custommachinery.common.network;

import com.google.gson.*;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SStructureCreatorPacket {

    private final JsonElement keysJson;
    private final JsonElement patternJson;

    public SStructureCreatorPacket(JsonElement keysJson, JsonElement patternJson) {
        this.keysJson = keysJson;
        this.patternJson = patternJson;
    }

    public static void encode(SStructureCreatorPacket pkt, PacketBuffer buffer) {
        buffer.writeString(pkt.keysJson.toString());
        buffer.writeString(pkt.patternJson.toString());
    }

    public static SStructureCreatorPacket decode(PacketBuffer buffer) {
        JsonParser parser = new JsonParser();
        return new SStructureCreatorPacket(parser.parse(buffer.readString()), parser.parse(buffer.readString()));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> ClientPacketHandler.handleStructureCreatorPacket(this.keysJson, this.patternJson));
        }
        context.get().setPacketHandled(true);
    }
}
