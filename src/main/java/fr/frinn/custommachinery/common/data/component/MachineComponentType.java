package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.data.builder.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.common.data.component.handler.IComponentHandler;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Function;
import java.util.function.Supplier;

public class MachineComponentType<T extends IMachineComponent> extends ForgeRegistryEntry<MachineComponentType<? extends IMachineComponent>> {

    private Codec<? extends IMachineComponentTemplate<T>> codec;
    private boolean isSingle = true;
    private Function<MachineComponentManager, IComponentHandler<T>> handlerBuilder;
    private boolean defaultComponent = false;
    private Function<MachineComponentManager, T> defaultComponentBuilder;
    private Supplier<IMachineComponentBuilder<T>> GUIComponentBuilder;

    public MachineComponentType(Codec<? extends IMachineComponentTemplate<T>> codec) {
        this.codec = codec;
    }

    public MachineComponentType(Function<MachineComponentManager, T> defaultComponentBuilder) {
        this.defaultComponent = true;
        this.defaultComponentBuilder = defaultComponentBuilder;
    }

    public MachineComponentType<T> setNotSingle(Function<MachineComponentManager, IComponentHandler<T>> handlerBuilder) {
        this.isSingle = false;
        this.handlerBuilder = handlerBuilder;
        return this;
    }

    public Codec<? extends IMachineComponentTemplate<T>> getCodec() {
        if(this.codec != null)
            return this.codec;
        else throw new RuntimeException("Error while trying to serialize or deserialize Machine Component: " + getRegistryName() + ", Codec not present !");
    }

    public boolean isSingle() {
        return this.isSingle;
    }

    public IComponentHandler<T> getHandler(MachineComponentManager manager) {
        if(this.isSingle || this.handlerBuilder == null)
            return null;
        return this.handlerBuilder.apply(manager);
    }

    public boolean isDefaultComponent() {
        return this.defaultComponent;
    }

    public Function<MachineComponentManager, T> getDefaultComponentBuilder() {
        return this.defaultComponentBuilder;
    }

    public MachineComponentType<T> setGUIBuilder(Supplier<IMachineComponentBuilder<T>> builder) {
        this.GUIComponentBuilder = builder;
        return this;
    }

    public boolean haveGUIBuilder() {
        return this.GUIComponentBuilder != null;
    }

    public Supplier<IMachineComponentBuilder<T>> getGUIBuilder() {
        if(this.GUIComponentBuilder != null)
            return this.GUIComponentBuilder;
        else throw new IllegalStateException("Error while trying to get a builder for Machine Component: " + getRegistryName() + " builder not present !");
    }

    public TranslationTextComponent getTranslatedName() {
        return new TranslationTextComponent(getRegistryName().getNamespace() + ".machine.component." + getRegistryName().getPath());
    }
}
