package fr.frinn.custommachinery.impl.codec;

import com.mojang.serialization.DataResult;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class RegistryCodecs {

    public static final NamedCodec<Identifier> CM_LOC_CODEC = NamedCodec.STRING.comapFlatMap(
            s -> {
                try {
                    if(s.contains(":"))
                        return DataResult.success(Identifier.parse(s));
                    else
                        return DataResult.success(Identifier.fromNamespaceAndPath(ICustomMachineryAPI.INSTANCE.modid(), s));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            },
            Identifier::toString,
            "CM Resource location"
    );

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

    public static <V> NamedCodec<V> of(Registry<V> registry, boolean isCM) {
        if(isCM)
            return CM_LOC_CODEC.flatXmap(id -> {
                if(!registry.containsKey(id))
                    return DataResult.error(() -> "Unknown registry key in " + registry.key() + ": " + id);
                return DataResult.success(registry.getValueOrThrow(ResourceKey.create(registry.key(), id)));
            }, value -> {
                Identifier id = registry.getKey(value);
                if(id == null)
                    return DataResult.error(() -> "Unknown registry key in " + registry.key() + ": " + value);
                return DataResult.success(id);
            }, "Registry: " + registry.key());
        return NamedCodec.of(registry.byNameCodec());
    }
}
