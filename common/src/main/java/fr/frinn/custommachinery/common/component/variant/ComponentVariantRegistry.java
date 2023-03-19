package fr.frinn.custommachinery.common.component.variant;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.variant.IComponentVariant;
import fr.frinn.custommachinery.api.component.variant.RegisterComponentVariantEvent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class ComponentVariantRegistry {

    private static Map<MachineComponentType<? extends IMachineComponent>, Map<ResourceLocation, NamedCodec<? extends IComponentVariant>>> map = Collections.emptyMap();

    public static void init() {
        RegisterComponentVariantEvent event = new RegisterComponentVariantEvent();
        RegisterComponentVariantEvent.EVENT.invoker().registerComponentVariant(event);
        map = event.getComponentVariants();
    }

    @Nullable
    public static <C extends IMachineComponent> NamedCodec<? extends IComponentVariant> getVariantCodec(MachineComponentType<C> type, ResourceLocation id) {
        Map<ResourceLocation, NamedCodec<? extends IComponentVariant>> variantsMap = map.get(type);
        if(variantsMap == null)
            return null;
        return variantsMap.get(id);
    }
}
