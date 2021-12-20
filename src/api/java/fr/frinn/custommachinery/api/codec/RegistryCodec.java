package fr.frinn.custommachinery.api.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import fr.frinn.custommachinery.api.CustomMachineryAPI;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Utility class to create a Codec to encode and decode registry objects.
 * @param <T> The {@link IForgeRegistryEntry} encoded or decoded by this codec.
 */
public class RegistryCodec<T extends IForgeRegistryEntry<T>> implements Codec<T> {

    public static final Codec<Item> ITEM                                      = of(ForgeRegistries.ITEMS);
    public static final Codec<Block> BLOCK                                    = of(ForgeRegistries.BLOCKS);
    public static final Codec<EntityType<?>> ENTITY_TYPE                      = of(ForgeRegistries.ENTITIES);
    public static final Codec<Effect> EFFECT                                  = of(ForgeRegistries.POTIONS);
    public static final Codec<Fluid> FLUID                                    = of(ForgeRegistries.FLUIDS);
    public static final Codec<MachineComponentType<?>> MACHINE_COMPONENT_TYPE = of(CustomMachineryAPI.getComponentRegistry());
    public static final Codec<GuiElementType<?>> GUI_ELEMENT_TYPE             = of(CustomMachineryAPI.getGuiElementRegistry());
    public static final Codec<RequirementType<?>> REQUIREMENT_TYPE            = of(CustomMachineryAPI.getRequirementRegistry());

    private final IForgeRegistry<T> registry;

    /**
     * Create a codec for any {@link IForgeRegistryEntry} using its corresponding {@link IForgeRegistry}.
     * @param registry The Forge registry to use.
     * @param <T> The {@link IForgeRegistryEntry} the codec will encode/decode.
     * @return A Codec<T>
     */
    public static <T extends IForgeRegistryEntry<T>> RegistryCodec<T> of(IForgeRegistry<T> registry) {
        return new RegistryCodec<>(registry);
    }

    private RegistryCodec(IForgeRegistry<T> registry) {
        this.registry = registry;
    }

    @Override
    public <D> DataResult<D> encode(T input, DynamicOps<D> ops, D prefix) {
        if(ops.compressMaps())
            throw new IllegalArgumentException("This codec does not support integer ids for registry entries");
        ResourceLocation key = registry.getKey(input);
        if(key == null)
        {
            return DataResult.error("Unknown registry element " + input);
        }
        D toMerge = ops.createString(key.toString());
        return ops.mergeToPrimitive(prefix, toMerge).setLifecycle(Lifecycle.stable());
    }

    @Override
    public <D> DataResult<Pair<T, D>> decode(DynamicOps<D> ops, D input) {
        if(ops.compressMaps())
            throw new IllegalArgumentException("This codec does not support integer ids for registry entries");
        return ResourceLocation.CODEC.decode(ops, input).flatMap(keyValuePair -> !registry.containsKey(keyValuePair.getFirst()) ?
                DataResult.error("Unknown registry key: " + keyValuePair.getFirst()) :
                DataResult.success(keyValuePair.mapFirst(registry::getValue))).setLifecycle(Lifecycle.stable());
    }

    @Override
    public String toString() {
        return this.registry.getRegistryName().toString();
    }
}
