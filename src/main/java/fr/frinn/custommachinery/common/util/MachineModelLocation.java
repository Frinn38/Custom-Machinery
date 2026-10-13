package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.util.IMachineModelLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class MachineModelLocation {

    public record MachineBlockModelLocation(Either<BlockState, Identifier> location) implements IMachineModelLocation.Block {
        public static final NamedCodec<MachineBlockModelLocation> CODEC = NamedCodec.either(DefaultCodecs.BLOCKSTATE, DefaultCodecs.IDENTIFIER)
                .xmap(MachineBlockModelLocation::new, MachineBlockModelLocation::location, "Machine block model location");
        public static final MachineBlockModelLocation DEFAULT = new MachineBlockModelLocation(Either.right(CustomMachinery.rl("")));
    }

    public record MachineItemModelLocation(Either<ItemStack, Identifier> location) implements IMachineModelLocation.Item {
        public static final NamedCodec<MachineItemModelLocation> CODEC = NamedCodec.either(DefaultCodecs.ITEM_OR_STACK, DefaultCodecs.IDENTIFIER)
                .xmap(MachineItemModelLocation::new, MachineItemModelLocation::location, "Machine item model location");
        public static final MachineItemModelLocation DEFAULT = new MachineItemModelLocation(Either.right(CustomMachinery.rl("")));
    }
}
