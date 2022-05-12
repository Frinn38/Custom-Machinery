package fr.frinn.custommachinery.api.component.variant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Implements this to make a variant for a IMachineComponent.
 * Exemple of component variants : fuel and upgrade for the item component.
 * All variants must be singletons and registered to the corresponding MachineComponentType AFTER registry events are fired (common setup is fine).
 */
public interface IComponentVariant {

    /**
     * A codec used to parse a component variant for a specific MachineComponentType.
     * The MachineComponentType must be passed as a supplied (RegistryObject is fine to use) because the codec is usually loaded statically before registry events are fired.
     * The class is used to cast the resulting variant to a specific class, like ItemComponentVariant.
     */
    static <T extends IMachineComponent, V extends IComponentVariant> Codec<V> codec(Supplier<MachineComponentType<T>> type, Class<V> variantClass){
        return ResourceLocation.CODEC.comapFlatMap(id -> {
            IComponentVariant variant = type.get().getVariant(id);
            if(variant == null)
                return DataResult.error(String.format("Invalid component variant: %s for type: %s", id, type.get().getRegistryName()));
            if(!variantClass.isInstance(variant))
                return DataResult.error(String.format("Invalid class: %s can't be cast to: %s for component variant of type: %s and id: %s.%n This is a mod or addon issue. If it happen please report it.", variant.getClass(), variantClass, type.get().getRegistryName(), id));
            return DataResult.success(variantClass.cast(variant));
        }, IComponentVariant::getId);
    }

    /**
     * @return The id of this variant, all variant of a component must have differents ids.
     */
    ResourceLocation getId();
}
