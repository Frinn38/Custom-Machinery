package fr.frinn.custommachinery.common.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.util.IMachineModelLocation;
import net.minecraft.IdentifierException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MachineModelLocation implements IMachineModelLocation {

    public static final MachineModelLocation DEFAULT = MachineModelLocation.of(CustomMachinery.rl("default/custom_machine_default").toString());

    public static final NamedCodec<MachineModelLocation> CODEC = NamedCodec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(MachineModelLocation.of(s));
        } catch (IdentifierException e) {
            return DataResult.error(e::getMessage);
        }
    }, MachineModelLocation::toString, "Model location");

    private final String loc;
    @Nullable
    private final BlockState state;
    @Nullable
    private final Item item;
    @Nullable
    private final Identifier id;
    @Nullable
    private final String properties;

    public static MachineModelLocation of(String loc) {
        BlockState state = null;
        try {
            state = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, new StringReader(loc), false).blockState();
        } catch (CommandSyntaxException ignored) {}
        Identifier id = null;
        String properties = null;
        if(loc.contains("#")) {
            id = Identifier.parse(loc.substring(0, loc.indexOf("#")));
            properties = loc.substring(loc.indexOf("#") + 1);
        } else if(state == null)
            id = Identifier.parse(loc);

        Item item = null;
        if(id != null && BuiltInRegistries.ITEM.containsKey(id))
            item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);

        return new MachineModelLocation(loc, state, item, id, properties);
    }

    private MachineModelLocation(String loc, @Nullable BlockState state, @Nullable Item item, @Nullable Identifier id, @Nullable String properties) {
        this.loc = loc;
        this.state = state;
        this.item = item;
        this.id = id;
        this.properties = properties;
    }

    @Override
    @Nullable
    public BlockState getState() {
        return this.state;
    }

    @Override
    @Nullable
    public Item getItem() {
        return this.item;
    }

    @Override
    @Nullable
    public Identifier getLoc() {
        return this.id;
    }

    @Override
    @Nullable
    public String getProperties() {
        return this.properties;
    }

    @Override
    public String toString() {
        return this.loc;
    }
}
