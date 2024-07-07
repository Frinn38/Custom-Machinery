package fr.frinn.custommachinery.common.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
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
        super(Registration.CUSTOM_MACHINE_BLOCK.get(), new Reference2ObjectArrayMap<>(), null);
        this.appearance = appearance;
    }

    @Override
    public boolean is(TagKey<Block> tag) {
        return BuiltInRegistries.BLOCK.getTag(this.appearance.getMiningLevel()).map(named -> named.stream().allMatch(holder -> holder.is(tag))).orElse(false) || this.appearance.getTool().contains(tag);
    }

    @Override
    public boolean is(HolderSet<Block> holder) {
        return holder.unwrapKey().map(this::is).orElse(false);
    }

    @Override
    public boolean requiresCorrectToolForDrops() {
        return this.appearance.requiresCorrectToolForDrops();
    }
}
