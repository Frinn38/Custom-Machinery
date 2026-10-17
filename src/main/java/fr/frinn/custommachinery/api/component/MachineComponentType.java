package fr.frinn.custommachinery.api.component;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Used to build and/or compare {@link IMachineComponent}.
 * An {@link IMachineComponent} MUST be linked to a single {@link MachineComponentType}.
 * All instances of this class must be created and registered using {@link net.neoforged.neoforge.registries.DeferredRegister}.
 * @param <C> The {@link IMachineComponent} handled by this {@link MachineComponentType}.
 */
public class MachineComponentType<C extends IMachineComponent> {

    /**
     * The {@link ResourceKey} pointing to the {@link MachineComponentType} vanilla registry.
     * Can be used to create a {@link net.neoforged.neoforge.registries.DeferredRegister} for registering your {@link MachineComponentType}.
     */
    public static final ResourceKey<Registry<MachineComponentType<? extends IMachineComponent>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("component_type"));

    /**
     * Use this factory method if you need to parse extra data inside the machine JSON.
     * The data will be stored as a {@link IMachineComponentTemplate} inside the {@link ICustomMachine} and will be used to build the specified {@link IMachineComponent}
     * when a machine tile is created.
     * @param codec A {@link Codec} used to deserialize the {@link IMachineComponentTemplate} from the machine JSON.
     */
    public static <T extends IMachineComponent> MachineComponentType<T> create(NamedCodec<? extends IMachineComponentTemplate<T>> codec) {
        return new MachineComponentType<>(codec);
    }

    /**
     * Use this factory method if the {@link IMachineComponent} don't need any extra data provided in the machine JSON.
     * @param defaultComponentBuilder Usually a method reference to the component constructor.
     *                                This will be used to build the component when the {@link MachineTile} is created.
     */
    public static <T extends IMachineComponent> MachineComponentType<T> create(Function<IMachineComponentManager, T> defaultComponentBuilder) {
        return new MachineComponentType<>(defaultComponentBuilder);
    }

    /**
     * Use this factory method if the component may have extra data provided in the machine JSON,
     * or use a default factory method if the user didn't specify the component in the machine JSON.
     * @param codec The {@link Codec} used to deserialize the {@link IMachineComponent} in the machine JSON, if specified by the user.
     * @param defaultComponentBuilder The factory method used if the user didn't specify the component in the machine JSON.
     */
    public static <T extends IMachineComponent> MachineComponentType<T> create(NamedCodec<? extends IMachineComponentTemplate<T>> codec, Function<IMachineComponentManager, T> defaultComponentBuilder) {
        return new MachineComponentType<>(codec, defaultComponentBuilder);
    }

    @Nullable
    private NamedCodec<? extends IMachineComponentTemplate<C>> codec;
    private boolean isSingle = true;
    @Nullable
    private BiFunction<IMachineComponentManager, List<C>, IComponentHandler<C>> handlerBuilder;
    private boolean defaultComponent = false;
    @Nullable
    private Function<IMachineComponentManager, C> defaultComponentBuilder;

    /**
     * A constructor for {@link MachineComponentType}.
     * Use {@link MachineComponentType#create(NamedCodec)} instead.
     */
    private MachineComponentType(NamedCodec<? extends IMachineComponentTemplate<C>> codec) {
        this.codec = codec;
    }

    /**
     * A constructor for {@link MachineComponentType}.
     * Use {@link MachineComponentType#create(Function)} instead.
     */
    private MachineComponentType(Function<IMachineComponentManager, C> defaultComponentBuilder) {
        this.defaultComponent = true;
        this.defaultComponentBuilder = defaultComponentBuilder;
    }

    /**
     * A constructor for {@link MachineComponentType}.
     * Use {@link MachineComponentType#create(NamedCodec, Function)} instead.
     */
    private MachineComponentType(NamedCodec<? extends IMachineComponentTemplate<C>> codec, Function<IMachineComponentManager, C> defaultComponentBuilder) {
        this.codec = codec;
        this.defaultComponent = true;
        this.defaultComponentBuilder = defaultComponentBuilder;
    }

    /**
     * By default, a {@link IMachineComponentManager} can hold only one {@link IMachineComponent} for each type.
     * Use this method to override the default behavior and tell the {@link IMachineComponentManager} to use a {@link IComponentHandler} instead of a {@link IMachineComponent}.
     * The {@link IComponentHandler} will hold all {@link IMachineComponent} for its type and redirect the {@link IMachineComponent} logic to them as needed.
     * @param handlerBuilder Usually a method reference to the {@link IComponentHandler} constructor.
     *                       This will be used to build the {@link IComponentHandler} when the {@link IMachineComponentManager} is created.
     * @return this
     */
    public MachineComponentType<C> setNotSingle(BiFunction<IMachineComponentManager, List<C>, IComponentHandler<C>> handlerBuilder) {
        this.isSingle = false;
        this.handlerBuilder = handlerBuilder;
        return this;
    }

    /**
     * @return The {@link NamedCodec} passed in the constructor. Used by the {@link IMachineComponent} dispatch codec to deserialize an {@link IMachineComponentTemplate} from the machine JSON.
     */
    public NamedCodec<? extends IMachineComponentTemplate<C>> getCodec() {
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
    public IComponentHandler<C> getHandler(IMachineComponentManager manager, List<C> components) {
        if(this.isSingle || this.handlerBuilder == null)
            throw new IllegalStateException("Machine component manager for machine '" + manager.getTile().getMachine().getId() + "' tried to build an handler for single component '" + getId() + "'");
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
    public Function<IMachineComponentManager, C> getDefaultComponentBuilder() {
        if(this.defaultComponentBuilder == null)
            throw new IllegalStateException("Trying to build machine component for type: " + this.getId() + " as default component");
        return this.defaultComponentBuilder;
    }

    /**
     * A helper method to get the ID of this {@link MachineComponentType}.
     * @return The ID of this {@link MachineComponentType}, or null if it is not registered.
     */
    public Identifier getId() {
        Identifier id = ICustomMachineryAPI.INSTANCE.componentRegistrar().getKey(this);
        if(id == null)
            throw new IllegalStateException("Trying to get id for an unregistered machine component type");
        return id;
    }

    /**
     * A helper method to get a {@link MachineComponentId} of this type, with an optional unique id for non-single components.
     * @param uniqueId A unique id for non-single components.
     * @return A {@link MachineComponentId} linking to an {@link IMachineComponent} of this type.
     */
    public MachineComponentId<C> componentId(@Nullable String uniqueId) {
        if(this.isSingle || uniqueId == null || uniqueId.isBlank())
            return MachineComponentId.forType(this);
        return new MachineComponentId<>(this, uniqueId, this.getId().withPath(path -> path + "/" + uniqueId));
    }

    /**
     * Utility method to get the display name of a {@link MachineComponentType}.
     * The translation key for the {@link MachineComponentType} will be : "namespace.machine.component.path".
     */
    public Component getTranslatedName() {
        return Component.translatable(getId().getNamespace() + ".machine.component." + getId().getPath());
    }
}
