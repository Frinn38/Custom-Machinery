package fr.frinn.custommachinery.impl.network;

import fr.frinn.custommachinery.api.network.DataType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Data<T>(DataType<T> type, short id, T value) {

    public static final StreamCodec<RegistryFriendlyByteBuf, Data<?>> STREAM_CODEC = ByteBufCodecs.registry(DataType.REGISTRY_KEY).dispatch(Data::type, DataType::streamCodec);

}
