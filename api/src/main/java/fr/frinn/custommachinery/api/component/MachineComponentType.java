package fr.frinn.custommachinery.api.component;

import com.mojang.serialization.Codec;
import dev.architectury.core.RegistryEntry;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.component.builder.IMachineComponentBuilder;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.api.component.variant.IComponentVariant;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Used to build and/or compare IMachineComponent.
 * An IMachineComponent can have only one type.
 * All instances of this class must be created and registered using RegistryEvent or DeferredRegister.
 * @param <T> The component handled by this type.
 */
public class MachineComponentType<T extends IMachineComponent> extends RegistryEntry<MachineComponentType<T>> {

    public static final ResourceKey<Registry<MachineComponentType<? extends IMachineComponent>>> REGISTRY_KEY = ResourceKey.createRegistryKey(ICustomMachineryAPI.INSTANCE.rl("component_type"));

    private Codec<? extends IMachineComponentTemplate<T>> codec;
    private boolean isSingle = true;
    private BiFunction<IMachineComponentManager, List<T>, IComponentHandler<T>> handlerBuilder;
    private boolean defaultComponent = false;
    private Function<IMachineComponentManager, T> defaultComponentBuilder;
    private Supplier<IMachineComponentBuilder<T>> GUIComponentBuilder;
    private final Map<ResourceLocation, IComponentVariant> variants = new ConcurrentHashMap<>();

    /**
     * Use this constructor if you need to parse extra data inside the machine json.
     * The data will be stored as a IMachineComponentTemplate inside the CustomMachine and will be used to build the component when a machine tile is created.
     * @param codec A Codec used to deserialize the IMachineComponentTemplate from the machine json.
     */
    public MachineComponentType(Codec<? extends IMachineComponentTemplate<T>> codec) {
        this.codec = codec;
    }

    /**
     * Use this constructor if yoy DON'T need any extra data provided by the machine maker.
     * @param defaultComponentBuilder Usually a method reference to the component constructor.
     *                                This will be used to build the component when the machine tile is created.
     */
    public MachineComponentType(Function<IMachineComponentManager, T> defaultComponentBuilder) {
        this.defaultComponent = true;
        this.defaultComponentBuilder = defaultComponentBuilder;
    }

    /**
     * Use this constructor if the component can have extra data provided by the machine maker, or use a default factory method if the user didn't specified the component in the machine json.
     * @param codec The codec used to deserialize the component in the machine json, if specified by the user.
     * @param defaultComponentBuilder The factory method used if the user didn't specified the component in the machine json.
     */
    public MachineComponentType(Codec<? extends IMachineComponentTemplate<T>> codec, Function<IMachineComponentManager, T> defaultComponentBuilder) {
        this.codec = codec;
        this.defaultComponent = true;
        this.defaultComponentBuilder = defaultComponentBuilder;
    }

    /**
     * By default, a IMachineComponentManager can hold only one component for each type.
     * Use this method to override the default behaviour and tell the IMachineComponentManager to use a IComponentHandler instead of a IMachineComponent.
     * The IComponentHandler will hold all components for it's type and redirect the component logic to them as needed.
     * @param handlerBuilder Usually a method reference to the IComponentHandler constructor.
     *                       This will be used to build the component handler when the machine tile is created.
     * @return this
     */
    public MachineComponentType<T> setNotSingle(BiFunction<IMachineComponentManager, List<T>, IComponentHandler<T>> handlerBuilder) {
        this.isSingle = false;
        this.handlerBuilder = handlerBuilder;
        return this;
    }

    /**
     * @return The codec passed in the constructor. Used by the IMachineComponent dispatch codec to deserialize a IMachineComponentTemplate from the machine json.
     */
    public Codec<? extends IMachineComponentTemplate<T>> getCodec() {
        if(this.codec != null)
            return this.codec;
        else throw new RuntimeException("Error while trying to serialize or deserialize Machine Component template: " + getId() + ", Codec not present !");
    }

    /**
     * If true the IMachineComponentManager will allow only one component of this type.
     * If false the IMachineComponentManager will try to create a IComponentHandler to hold several components of this type.
     */
    public boolean isSingle() {
        return this.isSingle;
    }

    /**
     * Used by the IMachineComponentManager to create a IComponentHandler to hold several components of the same type.
     * @param manager The IMachineComponentManager, should be passed to the IComponentHandler as every IMachineComponent.
     * @param components All the components of the same type that will be handled by this handler.
     *                   This is an unmodifiable list.
     * @return The Created IComponentHandler that will be put inside the IMachineComponentManager and hold all components for this type.
     */
    public IComponentHandler<T> getHandler(IMachineComponentManager manager, List<T> components) {
        if(this.isSingle || this.handlerBuilder == null)
            return null;
        return this.handlerBuilder.apply(manager, components);
    }

    /**
     * If true the IMachineComponentManager will create an instance of this component using the Function returned by the getDefaultComponentBuilder method.
     */
    public boolean isDefaultComponent() {
        return this.defaultComponent;
    }

    /**
     * @return The Function used by the IMachineComponentManager to build an IMachineComponent passed in this MachineComponentType constructor.
     */
    public Function<IMachineComponentManager, T> getDefaultComponentBuilder() {
        return this.defaultComponentBuilder;
    }

    /**
     * Used to set a IMachineComponentBuilder that can be used by the machine creation gui to create an IMachineComponentTemplate.
     * @param builder The IMachineComponentBuilder used by the machine creation gui.
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
     * @return The IMachineComponentBuilder that will be used by the machine component gui.
     */
    public Supplier<IMachineComponentBuilder<T>> getGUIBuilder() {
        if(this.GUIComponentBuilder != null)
            return this.GUIComponentBuilder;
        else throw new IllegalStateException("Error while trying to get a builder for Machine Component: " + getId() + " builder not present !");
    }

    public ResourceLocation getId() {
        return ICustomMachineryAPI.INSTANCE.componentRegistrar().getId(this);
    }

    /**
     * Utility method to get a component display name.
     * The translation key for the MachineComponentType will be : "namespace.machine.component.path".
     */
    public TranslatableComponent getTranslatedName() {
        if(getId() == null)
            throw new IllegalStateException("Trying to get the registry name of an unregistered MachineComponentType");
        return new TranslatableComponent(getId().getNamespace() + ".machine.component." + getId().getPath());
    }

    /**
     * Add a variant to this component type.
     * The variant must be a singleton, and it's id unique.
     * This method is thread-safe, but must be used after registration (common setup is good).
     * @param variant The variant to register.
     */
    public IComponentVariant addVariant(IComponentVariant variant) {
        if(this.variants.containsKey(variant.getId()))
            throw new IllegalArgumentException("A component variant of type: " + getId() + " with id: " + variant.getId() + " is already registered !");
        this.variants.put(variant.getId(), variant);
        return variant;
    }

    /**
     * @return A component variant for this component type, or null if no component variant is registered with this id.
     */
    public IComponentVariant getVariant(ResourceLocation id) {
        return this.variants.get(id);
    }
}
