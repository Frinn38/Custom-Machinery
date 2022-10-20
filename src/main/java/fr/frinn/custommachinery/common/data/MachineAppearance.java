package fr.frinn.custommachinery.common.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import fr.frinn.custommachinery.api.CustomMachineryAPI;
import fr.frinn.custommachinery.api.machine.IMachineAppearance;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.common.ToolType;

import java.util.Map;
import java.util.stream.Stream;

public class MachineAppearance implements IMachineAppearance {

    public static final MapCodec<Map<MachineAppearanceProperty<?>, Object>> CODEC = new MapCodec<Map<MachineAppearanceProperty<?>, Object>>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Registration.APPEARANCE_PROPERTY_REGISTRY.get().getKeys().stream().map(loc -> ops.createString(loc.toString()));
        }

        @Override
        public <T> DataResult<Map<MachineAppearanceProperty<?>, Object>> decode(DynamicOps<T> ops, MapLike<T> input) {
            ImmutableMap.Builder<MachineAppearanceProperty<?>, Object> properties = ImmutableMap.builder();

            for(MachineAppearanceProperty<?> property : Registration.APPEARANCE_PROPERTY_REGISTRY.get()) {
                if(property.getRegistryName() != null && input.get(property.getRegistryName().toString()) != null) {
                    DataResult<?> result = property.getCodec().parse(ops, input.get(property.getRegistryName().toString()));
                    if(result.result().isPresent())
                        properties.put(property, result.result().get());
                    else if(result.error().isPresent()) {
                        CustomMachineryAPI.getLogger().warn("Couldn't deserialize appearance property: %s, invalid value: %s, error: %s, using default value instead.", property.getRegistryName(), input.get(property.getRegistryName().toString()), result.error().get().message());
                        properties.put(property, property.getDefaultValue());
                    }
                } else if(property.getRegistryName() != null && input.get(property.getRegistryName().getPath()) != null) {
                    DataResult<?> result = property.getCodec().parse(ops, input.get(property.getRegistryName().getPath()));
                    if(result.result().isPresent())
                        properties.put(property, result.result().get());
                    else if(result.error().isPresent()) {
                        CustomMachineryAPI.getLogger().warn("Couldn't deserialize appearance property: %s, invalid value: %s, error: %s, using default value instead.", property.getRegistryName(), input.get(property.getRegistryName().getPath()), result.error().get().message());
                        properties.put(property, property.getDefaultValue());
                    }
                } else {
                    properties.put(property, property.getDefaultValue());
                }
            }

            return DataResult.success(properties.build());
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> RecordBuilder<T> encode(Map<MachineAppearanceProperty<?>, Object> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            for(Map.Entry<MachineAppearanceProperty<?>, Object> entry : input.entrySet()) {
                if(entry.getValue() != entry.getKey().getDefaultValue() && entry.getKey().getRegistryName() != null)
                    prefix.add(entry.getKey().getRegistryName().toString(), ((Codec<Object>)entry.getKey().getCodec()).encodeStart(ops, entry.getValue()));
            }
            return prefix;
        }

        @Override
        public String toString() {
            return "Machine Appearance";
        }
    };

    private final Map<MachineAppearanceProperty<?>, Object> properties;

    public MachineAppearance(Map<MachineAppearanceProperty<?>, Object> properties) {
        this.properties = properties;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(MachineAppearanceProperty<T> property) {
        if(!this.properties.containsKey(property))
            throw new IllegalStateException("Can't get Machine Appearance property for: " + property.getRegistryName() + ", this property may not be registered");
        return (T)this.properties.get(property);
    }

    @Override
    public ResourceLocation getBlockModel() {
        return getProperty(Registration.BLOCK_MODEL_PROPERTY.get());
    }

    @Override
    public ResourceLocation getItemModel() {
        return getProperty(Registration.ITEM_MODEL_PROPERTY.get());
    }

    @Override
    public SoundEvent getSound() {
        return getProperty(Registration.SOUND_PROPERTY.get());
    }

    @Override
    public int getLightLevel() {
        return getProperty(Registration.LIGHT_PROPERTY.get());
    }

    @Override
    public int getColor() {
        return getProperty(Registration.COLOR_PROPERTY.get());
    }

    @Override
    public float getHardness() {
        return getProperty(Registration.HARDNESS_PROPERTY.get());
    }

    @Override
    public float getResistance() {
        return getProperty(Registration.RESISTANCE_PROPERTY.get());
    }

    @Override
    public ToolType getTool() {
        return getProperty(Registration.TOOL_TYPE_PROPERTY.get());
    }

    @Override
    public int getMiningLevel() {
        return getProperty(Registration.MINING_LEVEL_PROPERTY.get());
    }

    @Override
    public VoxelShape getShape() {
        return getProperty(Registration.SHAPE_PROPERTY.get());
    }

    @Override
    public MachineAppearance copy() {
        return new MachineAppearance(ImmutableMap.copyOf(this.properties));
    }

    public Map<MachineAppearanceProperty<?>, Object> getProperties() {
        return this.properties;
    }
}
