package fr.frinn.custommachinery.common.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class MachineBlockState extends BlockState {

    public static final LoadingCache<MachineAppearance, MachineBlockState> CACHE = CacheBuilder.newBuilder().maximumSize(20).build(new CacheLoader<>() {
        @Override
        public MachineBlockState load(MachineAppearance appearance) {
            return new MachineBlockState(appearance);
        }
    });

    private final MachineAppearance appearance;

    public MachineBlockState(MachineAppearance appearance) {
        super(CMRegistration.CUSTOM_MACHINE_BLOCK.get(), new Property[0], new Comparable[0]);
        this.appearance = appearance;
    }

    @Override
    public boolean is(TagKey<Block> tag) {
        if(this.appearance.getMiningLevel() == tag || this.appearance.getTool().contains(tag))
            return true;
        return BuiltInRegistries.BLOCK.get(this.appearance.getMiningLevel()).map(named -> named.stream().allMatch(block -> block.is(tag))).orElse(false);
    }

    @Override
    public boolean is(HolderSet<Block> holder) {
        if(holder.contains(this.typeHolder()))
            return true;
        return holder.unwrapKey().map(this::is).orElse(false);
    }

    @Override
    public boolean requiresCorrectToolForDrops() {
        return this.appearance.requiresCorrectToolForDrops();
    }
}
