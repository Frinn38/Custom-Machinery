package fr.frinn.custommachinery.api.components.variant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public interface IComponentVariant {

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

    ResourceLocation getId();
}
