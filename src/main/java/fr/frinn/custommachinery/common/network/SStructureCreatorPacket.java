package fr.frinn.custommachinery.common.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fr.frinn.custommachinery.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SStructureCreatorPacket {

    private final JsonElement keysJson;
    private final JsonElement patternJson;

    public SStructureCreatorPacket(JsonElement keysJson, JsonElement patternJson) {
        this.keysJson = keysJson;
        this.patternJson = patternJson;
    }

    public static void encode(SStructureCreatorPacket pkt, FriendlyByteBuf buffer) {
        buffer.writeUtf(pkt.keysJson.toString());
        buffer.writeUtf(pkt.patternJson.toString());
    }

    public static SStructureCreatorPacket decode(FriendlyByteBuf buffer) {
        JsonParser parser = new JsonParser();
        return new SStructureCreatorPacket(parser.parse(buffer.readUtf()), parser.parse(buffer.readUtf()));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if(context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT)
            context.get().enqueueWork(() -> ClientPacketHandler.handleStructureCreatorPacket(this.keysJson, this.patternJson));
        context.get().setPacketHandled(true);
    }
}
