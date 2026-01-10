package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.requirement.StructureRequirement;
import fr.frinn.custommachinery.common.util.BlockIngredient;
import fr.frinn.custommachinery.common.util.BlockStructure;
import fr.frinn.custommachinery.common.util.MachineList;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Map;

public record CPlaceStructurePacket(ResourceLocation machine, List<List<String>> pattern, Map<Character, List<BlockIngredient>> keys) implements CustomPacketPayload {

    public static final NamedCodec<List<List<String>>> PATTERN_CODEC = NamedCodec.STRING.forcedListOf().forcedListOf();
    public static final NamedCodec<Map<Character, List<BlockIngredient>>> KEYS_CODEC = NamedCodec.unboundedMap(DefaultCodecs.CHARACTER, BlockIngredient.CODEC.listOf(), "Structure keys");

    public static final Type<CPlaceStructurePacket> TYPE = new Type<>(CustomMachinery.rl("place_structure"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CPlaceStructurePacket> CODEC = new StreamCodec<>() {
        @Override
        public CPlaceStructurePacket decode(RegistryFriendlyByteBuf buf) {
            return new CPlaceStructurePacket(buf.readResourceLocation(), PATTERN_CODEC.fromNetwork(buf), KEYS_CODEC.fromNetwork(buf));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CPlaceStructurePacket packet) {
            buf.writeResourceLocation(packet.machine);
            PATTERN_CODEC.toNetwork(packet.pattern, buf);
            KEYS_CODEC.toNetwork(packet.keys, buf);
        }
    };

    @Override
    public Type<CPlaceStructurePacket> type() {
        return TYPE;
    }

    public static void handle(CPlaceStructurePacket packet, IPayloadContext context) {
        if(context.player() instanceof ServerPlayer player && player.getAbilities().instabuild) {
            context.enqueueWork(() -> {
                MachineList.findNearest(player, packet.machine, 20).flatMap(tile -> tile.getComponentManager().getComponent(Registration.STRUCTURE_MACHINE_COMPONENT.get())).ifPresent(component -> {
                    BlockStructure structure = StructureRequirement.makeStructure(packet.pattern, packet.keys);
                    component.placeStructure(structure, false);
                });
            });
        }
    }
}
