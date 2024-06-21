package fr.frinn.custommachinery.api.component.variant;

import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.impl.codec.NamedMapCodec;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Implements this to make a variant for a IMachineComponent.
 * Example of component variants : fuel and upgrade for the item component.
 * All variants must be singletons and registered to the corresponding MachineComponentType AFTER registry events are fired (common setup is fine).
 */
public interface IComponentVariant {

    /**
     * A codec used to parse a component variant for a specific MachineComponentType.
     * The MachineComponentType must be passed as a supplier (RegistryObject is fine to use) because the codec is usually loaded statically before registry events are fired.
     * The class is used to cast the resulting variant to a specific class, like ItemComponentVariant.
     */
    static <C extends IMachineComponent> NamedMapCodec<IComponentVariant> codec(Supplier<MachineComponentType<C>> type){
        return RegistrarCodec.CM_LOC_CODEC.dispatch("variant", IComponentVariant::getId, id -> ICustomMachineryAPI.INSTANCE.getVariantCodec(type.get(), id), "Machine component variant").aliases("varient");
    }

    /**
     * @return The id of this variant, all variant of a component must have different ids.
     */
    ResourceLocation getId();

    /**
     * @return A codec used to parse this variant.
     */
    NamedCodec<? extends IComponentVariant> getCodec();
}
