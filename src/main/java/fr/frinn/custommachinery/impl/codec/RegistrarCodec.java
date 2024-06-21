package fr.frinn.custommachinery.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.ProcessorType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class RegistrarCodec<V> implements NamedCodec<V> {

    /** Vanilla registries **/
    public static final NamedCodec<Item> ITEM = of(BuiltInRegistries.ITEM, false);
    public static final NamedCodec<Block> BLOCK = of(BuiltInRegistries.BLOCK, false);
    public static final NamedCodec<Fluid> FLUID = of(BuiltInRegistries.FLUID, false);
    public static final NamedCodec<EntityType<?>> ENTITY = of(BuiltInRegistries.ENTITY_TYPE, false);
    public static final NamedCodec<MobEffect> EFFECT = of(BuiltInRegistries.MOB_EFFECT, false);

    /**CM registries**/
    public static final NamedCodec<MachineComponentType<?>> MACHINE_COMPONENT = of(ICustomMachineryAPI.INSTANCE.componentRegistrar(), true);
    public static final NamedCodec<RequirementType<?>> REQUIREMENT = of(ICustomMachineryAPI.INSTANCE.requirementRegistrar(), true);
    public static final NamedCodec<GuiElementType<?>> GUI_ELEMENT = of(ICustomMachineryAPI.INSTANCE.guiElementRegistrar(), true);
    public static final NamedCodec<MachineAppearanceProperty<?>> APPEARANCE_PROPERTY = of(ICustomMachineryAPI.INSTANCE.appearancePropertyRegistrar(), true);
    public static final NamedCodec<DataType<?, ?>> DATA = of(ICustomMachineryAPI.INSTANCE.dataRegistrar(), true);
    public static final NamedCodec<ProcessorType<?>> CRAFTING_PROCESSOR = of(ICustomMachineryAPI.INSTANCE.processorRegistrar(), true);

    public static final NamedCodec<ResourceLocation> CM_LOC_CODEC = NamedCodec.STRING.comapFlatMap(
            s -> {
                try {
                    if(s.contains(":"))
                        return DataResult.success(ResourceLocation.parse(s));
                    else
                        return DataResult.success(ResourceLocation.fromNamespaceAndPath(ICustomMachineryAPI.INSTANCE.modid(), s));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            },
            ResourceLocation::toString,
            "CM Resource location"
    );

    public static <V> RegistrarCodec<V> of(Registry<V> registrar, boolean isCM) {
        return new RegistrarCodec<>(registrar, isCM);
    }

    private final Registry<V> registrar;
    private final boolean isCM;

    private RegistrarCodec(Registry<V> registrar, boolean isCM) {
        this.registrar = registrar;
        this.isCM = isCM;
    }

    @Override
    public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        return (this.isCM ? CM_LOC_CODEC : DefaultCodecs.RESOURCE_LOCATION).decode(ops, input).flatMap(keyValuePair ->
                !this.registrar.containsKey(keyValuePair.getFirst())
                        ? DataResult.error(() -> "Unknown registry key in " + this.registrar.key() + ": " + keyValuePair.getFirst())
                        : DataResult.success(keyValuePair.mapFirst(this.registrar::get))
        );
    }

    @Override
    public <T> DataResult<T> encode(DynamicOps<T> ops, V input, T prefix) {
        return DefaultCodecs.RESOURCE_LOCATION.encode(ops, this.registrar.getKey(input), prefix);
    }

    @Override
    public String name() {
        return this.registrar.key().location().toString();
    }
}
