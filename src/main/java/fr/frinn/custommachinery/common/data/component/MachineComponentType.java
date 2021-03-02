package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.data.component.handler.IComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;

public class MachineComponentType<T extends IMachineComponent> extends ForgeRegistryEntry<MachineComponentType<? extends IMachineComponent>> {

    public static final Codec<MachineComponentType<? extends IMachineComponent>> CODEC = ResourceLocation.CODEC.xmap(Registration.MACHINE_COMPONENT_TYPE_REGISTRY::getValue, MachineComponentType::getRegistryName);

    private Codec<? extends IMachineComponentTemplate<T>> codec;
    private boolean isSingle = true;
    private Function<MachineComponentManager, IComponentHandler<T>> handlerBuilder;

    public MachineComponentType(Codec<? extends IMachineComponentTemplate<T>> codec) {
        this.codec = codec;
    }

    public MachineComponentType<T> setNotSingle(Function<MachineComponentManager, IComponentHandler<T>> handlerBuilder) {
        this.isSingle = false;
        this.handlerBuilder = handlerBuilder;
        return this;
    }

    public Codec<? extends IMachineComponentTemplate<T>> getCodec() {
        return this.codec;
    }

    public boolean isSingle() {
        return this.isSingle;
    }

    public IComponentHandler<T> getHandler(MachineComponentManager manager) {
        if(this.isSingle || this.handlerBuilder == null)
            return null;
        return this.handlerBuilder.apply(manager);
    }
}
