package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.architectury.registry.registries.Registrar;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class RegistrarCodec<V> implements Codec<V> {

    /** Vanilla registries **/
    public static final Codec<Item> ITEM = of(ICustomMachineryAPI.INSTANCE.registrar(Registry.ITEM_REGISTRY), false);
    public static final Codec<Block> BLOCK = of(ICustomMachineryAPI.INSTANCE.registrar(Registry.BLOCK_REGISTRY), false);
    public static final Codec<Fluid> FLUID = of(ICustomMachineryAPI.INSTANCE.registrar(Registry.FLUID_REGISTRY), false);
    public static final Codec<EntityType<?>> ENTITY = of(ICustomMachineryAPI.INSTANCE.registrar(Registry.ENTITY_TYPE_REGISTRY), false);
    public static final Codec<Enchantment> ENCHANTMENT = of(ICustomMachineryAPI.INSTANCE.registrar(Registry.ENCHANTMENT_REGISTRY), false);
    public static final Codec<MobEffect> EFFECT = of(ICustomMachineryAPI.INSTANCE.registrar(Registry.MOB_EFFECT_REGISTRY), false);

    /**CM registries**/
    public static final Codec<MachineComponentType<?>> MACHINE_COMPONENT = of(ICustomMachineryAPI.INSTANCE.componentRegistrar(), true);
    public static final Codec<RequirementType<?>> REQUIREMENT = of(ICustomMachineryAPI.INSTANCE.requirementRegistrar(), true);
    public static final Codec<GuiElementType<?>> GUI_ELEMENT = of(ICustomMachineryAPI.INSTANCE.guiElementRegistrar(), true);
    public static final Codec<MachineAppearanceProperty<?>> APPEARANCE_PROPERTY = of(ICustomMachineryAPI.INSTANCE.appearancePropertyRegistrar(), true);
    public static final Codec<DataType<?, ?>> DATA = of(ICustomMachineryAPI.INSTANCE.dataRegistrar(), true);
    public static final Codec<ProcessorType<?>> CRAFTING_PROCESSOR = of(ICustomMachineryAPI.INSTANCE.processorRegistrar(), true);

    public static final Codec<ResourceLocation> CM_LOC_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                try {
                    if(s.contains(":"))
                        return DataResult.success(new ResourceLocation(s));
                    else
                        return DataResult.success(ICustomMachineryAPI.INSTANCE.rl(s));
                } catch (Exception e) {
                    return DataResult.error(e.getMessage());
                }
            }, ResourceLocation::toString
    );

    public static <V> RegistrarCodec<V> of(Registrar<V> registrar, boolean isCM) {
        return new RegistrarCodec<>(registrar, isCM);
    }

    private final Registrar<V> registrar;
    private final boolean isCM;

    private RegistrarCodec(Registrar<V> registrar, boolean isCM) {
        this.registrar = registrar;
        this.isCM = isCM;
    }

    @Override
    public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        return (this.isCM ? CM_LOC_CODEC : ResourceLocation.CODEC).decode(ops, input).flatMap(keyValuePair ->
                !this.registrar.contains(keyValuePair.getFirst())
                        ? DataResult.error("Unknown registry key in " + this.registrar.key() + ": " + keyValuePair.getFirst())
                        : DataResult.success(keyValuePair.mapFirst(this.registrar::get))
        );
    }

    @Override
    public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        return ResourceLocation.CODEC.encode(this.registrar.getId(input), ops, prefix);
    }
}
