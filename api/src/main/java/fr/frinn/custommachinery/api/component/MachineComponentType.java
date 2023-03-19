package fr.frinn.custommachinery.api.component;

import com.mojang.serialization.Codec;
import dev.architectury.core.RegistryEntry;
import dev.architectury.registry.registries.DeferredRegister;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.builder.IMachineComponentBuilder;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Used to build and/or compare {@link IMachineComponent}.
 * An {@link IMachineComponent} MUST be linked to a single {@link MachineComponentType}.
 * All instances of this class must be created and registered using {@link Registry} for Fabric or {@link DeferredRegister} for Forge or Architectury.
 * @param <T> The {@link IMachineComponent} handled by this {@link MachineComponentType}.
 */
public class MachineComponentType<T extends IMachineComponent> extends RegistryEntry<MachineComponentType<T>> {

    /**
     * The {@link ResourceKey} pointing to the {@link MachineComponentType} vanilla registry.
     * Can be used to create a {@link DeferredRegister} for registering your {@link MachineComponentType}.
     */
    public static final ResourceKey<Registry<MachineComponentType<? extends IMachineComponent>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("component_type"));

    /**
     * Use this factory method if you need to parse extra data inside the machine json.
     * The data will be stored as a {@link IMachineComponentTemplate} inside the {@link ICustomMachine} and will be used to build the specified {@link IMachineComponent}
     * when a machine tile is created.
     * @param codec A {@link Codec} used to deserialize the {@link IMachineComponentTemplate} from the machine json.
     */
    public static <T extends IMachineComponent> MachineComponentType<T> create(NamedCodec<? extends IMachineComponentTemplate<T>> codec) {
        return new MachineComponentType<T>(codec);
    }

    /**
     * Use this factory method if the {@link IMachineComponent} don't need any extra data provided in the machine json.
     * @param defaultComponentBuilder Usually a method reference to the component constructor.
     *                                This will be used to build the component when the {@link MachineTile} is created.
     */
    public static <T extends IMachineComponent> MachineComponentType<T> create(Function<IMachineComponentManager, T> defaultComponentBuilder) {
        return new MachineComponentType<T>(defaultComponentBuilder);
    }

    /**
     * Use this factory method if the component may have extra data provided in the machine json,
     * or use a default factory method if the user didn't specify the component in the machine json.
     * @param codec The {@link Codec} used to deserialize the {@link IMachineComponent} in the machine json, if specified by the user.
     * @param defaultComponentBuilder The factory method used if the user didn't specify the component in the machine json.
     */
    public static <T extends IMachineComponent> MachineComponentType<T> create(NamedCodec<? extends IMachineComponentTemplate<T>> codec, Function<IMachineComponentManager, T> defaultComponentBuilder) {
        return new MachineComponentType<T>(codec, defaultComponentBuilder);
    }

    private NamedCodec<? extends IMachineComponentTemplate<T>> codec;
    private boolean isSingle = true;
    private BiFunction<IMachineComponentManager, List<T>, IComponentHandler<T>> handlerBuilder;
    private boolean defaultComponent = false;
    private Function<IMachineComponentManager, T> defaultComponentBuilder;
    private Supplier<IMachineComponentBuilder<T>> GUIComponentBuilder;

    /**
     * A constructor for {@link MachineComponentType}.
     * Use {@link MachineComponentType#create(NamedCodec)} instead.
     */
    private MachineComponentType(NamedCodec<? extends IMachineComponentTemplate<T>> codec) {
        this.codec = codec;
    }

    /**
     * A constructor for {@link MachineComponentType}.
     * Use {@link MachineComponentType#create(Function)} instead.
     */
    private MachineComponentType(Function<IMachineComponentManager, T> defaultComponentBuilder) {
        this.defaultComponent = true;
        this.defaultComponentBuilder = defaultComponentBuilder;
    }

    /**
     * A constructor for {@link MachineComponentType}.
     * Use {@link MachineComponentType#create(NamedCodec, Function)} instead.
     */
    private MachineComponentType(NamedCodec<? extends IMachineComponentTemplate<T>> codec, Function<IMachineComponentManager, T> defaultComponentBuilder) {
        this.codec = codec;
        this.defaultComponent = true;
        this.defaultComponentBuilder = defaultComponentBuilder;
    }

    /**
     * By default, a {@link IMachineComponentManager} can hold only one {@link IMachineComponent} for each type.
     * Use this method to override the default behaviour and tell the {@link IMachineComponentManager} to use a {@link IComponentHandler} instead of a {@link IMachineComponent}.
     * The {@link IComponentHandler} will hold all {@link IMachineComponent} for its type and redirect the {@link IMachineComponent} logic to them as needed.
     * @param handlerBuilder Usually a method reference to the {@link IComponentHandler} constructor.
     *                       This will be used to build the {@link IComponentHandler} when the {@link IMachineComponentManager} is created.
     * @return this
     */
    public MachineComponentType<T> setNotSingle(BiFunction<IMachineComponentManager, List<T>, IComponentHandler<T>> handlerBuilder) {
        this.isSingle = false;
        this.handlerBuilder = handlerBuilder;
        return this;
    }

    /**
     * @return The {@link NamedCodec} passed in the constructor. Used by the {@link IMachineComponent} dispatch codec to deserialize an {@link IMachineComponentTemplate} from the machine json.
     */
    public NamedCodec<? extends IMachineComponentTemplate<T>> getCodec() {
        if(this.codec != null)
            return this.codec;
        else throw new RuntimeException("Error while trying to serialize or deserialize Machine Component template: " + getId() + ", Codec not present !");
    }

    /**
     * If true the {@link IMachineComponentManager} will allow only one {@link IMachineComponent} of this type.
     * If false the {@link IMachineComponentManager} will try to create a {@link IComponentHandler} to hold several {@link IMachineComponent} of this type.
     */
    public boolean isSingle() {
        return this.isSingle;
    }

    /**
     * Used by the {@link IMachineComponentManager} to create a {@link IComponentHandler} to hold several {@link IMachineComponent} of the same type.
     * @param manager The {@link IMachineComponentManager}, should be passed to the {@link IComponentHandler} as every {@link IMachineComponent}.
     * @param components All the {@link IMachineComponent} of the same type that will be handled by this {@link IComponentHandler}.
     *                   This is an unmodifiable list.
     * @return The Created {@link IComponentHandler} that will be put inside the {@link IMachineComponentManager} and hold all {@link IMachineComponent} for this type.
     */
    public IComponentHandler<T> getHandler(IMachineComponentManager manager, List<T> components) {
        if(this.isSingle || this.handlerBuilder == null)
            return null;
        return this.handlerBuilder.apply(manager, components);
    }

    /**
     * If true the {@link IMachineComponentManager} will create an instance of the handled {@link IMachineComponent} using {@link MachineComponentType#getDefaultComponentBuilder()}.
     */
    public boolean isDefaultComponent() {
        return this.defaultComponent;
    }

    /**
     * @return The Function used by the {@link IMachineComponentManager} to build an {@link IMachineComponent} passed in this {@link MachineComponentType} constructor.
     */
    public Function<IMachineComponentManager, T> getDefaultComponentBuilder() {
        return this.defaultComponentBuilder;
    }

    /**
     * Used to set a {@link IMachineComponentBuilder} that can be used by the machine creation gui to create an {@link IMachineComponentTemplate}.
     * @param builder The {@link IMachineComponentBuilder} used by the machine creation gui.
     * @return this
     */
    public MachineComponentType<T> setGUIBuilder(Supplier<IMachineComponentBuilder<T>> builder) {
        this.GUIComponentBuilder = builder;
        return this;
    }

    /**
     * @return true if this component can be created by the machine creation gui.
     */
    public boolean haveGUIBuilder() {
        return this.GUIComponentBuilder != null;
    }

    /**
     * @return The {@link IMachineComponentBuilder} that will be used by the machine component gui.
     */
    public Supplier<IMachineComponentBuilder<T>> getGUIBuilder() {
        if(this.GUIComponentBuilder != null)
            return this.GUIComponentBuilder;
        else throw new IllegalStateException("Error while trying to get a builder for Machine Component: " + getId() + " builder not present !");
    }

    /**
     * A helper method to get the ID of this {@link MachineComponentType}.
     * @return The ID of this {@link MachineComponentType}, or null if it is not registered.
     */
    public ResourceLocation getId() {
        return ICustomMachineryAPI.INSTANCE.componentRegistrar().getId(this);
    }

    /**
     * Utility method to get the display name of a {@link MachineComponentType}.
     * The translation key for the {@link MachineComponentType} will be : "namespace.machine.component.path".
     */
    public TranslatableComponent getTranslatedName() {
        if(getId() == null)
            throw new IllegalStateException("Trying to get the registry name of an unregistered MachineComponentType");
        return new TranslatableComponent(getId().getNamespace() + ".machine.component." + getId().getPath());
    }
}
