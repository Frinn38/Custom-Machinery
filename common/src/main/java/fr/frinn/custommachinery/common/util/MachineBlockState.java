package fr.frinn.custommachinery.common.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MachineBlockState extends BlockState {

    public static final LoadingCache<MachineAppearance, MachineBlockState> CACHE = CacheBuilder.newBuilder().maximumSize(20).build(new CacheLoader<>() {
        @Override
        public MachineBlockState load(MachineAppearance appearance) {
            return new MachineBlockState(appearance);
        }
    });

    private final MachineAppearance appearance;

    public MachineBlockState(MachineAppearance appearance) {
        super(Registration.CUSTOM_MACHINE_BLOCK.get(), ImmutableMap.of(), null);
        this.appearance = appearance;
    }

    @Override
    public boolean is(TagKey<Block> tag) {
        return this.appearance.getMiningLevel() == tag || this.appearance.getTool().contains(tag);
    }

    @Override
    public boolean requiresCorrectToolForDrops() {
        return this.appearance.requiresCorrectToolForDrops();
    }
}
