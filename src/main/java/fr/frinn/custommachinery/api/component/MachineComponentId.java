package fr.frinn.custommachinery.api.component;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.codec.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public record MachineComponentId<C extends IMachineComponent>(MachineComponentType<C> type, String uniqueId, Identifier id) {

    public static final NamedCodec<MachineComponentId<?>> CODEC = NamedCodec.either(
            NamedCodec.<MachineComponentId<?>>record(completeBuilder -> completeBuilder.group(
                    RegistryCodecs.MACHINE_COMPONENT.fieldOf("type").forGetter(MachineComponentId::type),
                    NamedCodec.STRING.optionalFieldOf("id", "").forGetter(MachineComponentId::uniqueId)
            ).apply(completeBuilder, (type, id) -> type.isSingle() ? forType(type) : type.componentId(id)), "Complete component id"),
            DefaultCodecs.IDENTIFIER.comapFlatMap(id -> {
                try {
                    return DataResult.success(forId(id));
                } catch (IllegalStateException e) {
                    return DataResult.<MachineComponentId<?>>error(() -> "Can't deserialize machine component id: " + id + "\n" + e.getMessage());
                }
            }, MachineComponentId::id, "Flat component id")
    ).xmap(either -> either.map(Function.identity(), Function.identity()), Either::right, "Machine component id");

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineComponentId<?>> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(MachineComponentType.REGISTRY_KEY),
            MachineComponentId::type,
            ByteBufCodecs.STRING_UTF8,
            MachineComponentId::uniqueId,
            Identifier.STREAM_CODEC,
            MachineComponentId::id,
            MachineComponentId::new
    );

    public static <C extends IMachineComponent> MachineComponentId<C> forType(MachineComponentType<C> type) {
        if(!type.isSingle())
            throw new IllegalStateException("Non unique component of type: " + type.getId() + " must have unique id");
        return new MachineComponentId<>(type, "", type.getId());
    }

    public static <C extends IMachineComponent> MachineComponentId<C> forId(Identifier id) {
        int index = id.getPath().lastIndexOf('/');
        String typeString;
        if(index == -1)
            typeString = id.toString();
        else
            typeString = id.toString().substring(0, index);
        Identifier typeId = Identifier.parse(typeString);
        MachineComponentType<C> type = Utils.cast(CMRegistration.MACHINE_COMPONENT_TYPE_REGISTRY.getValue(typeId));
        if(type == null)
            throw new IllegalStateException("No component type registered for type: " + typeId + " in component id: " + id);
        if(index == -1)
            return forType(type);
        return new MachineComponentId<>(type, id.toString().substring(index), id);
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        else if (obj instanceof MachineComponentId<?> componentId)
            return this.id.equals(componentId.id());
        else if(obj instanceof Identifier identifier)
            return this.id.equals(identifier);
        else
            return obj instanceof CharSequence s && this.id.toString().contentEquals(s);
    }

    public boolean equals(String string) {
        return this.id.toString().equals(string);
    }
}
