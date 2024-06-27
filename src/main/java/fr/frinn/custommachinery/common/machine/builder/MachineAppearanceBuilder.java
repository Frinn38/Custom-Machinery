package fr.frinn.custommachinery.common.machine.builder;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.util.MachineModelLocation;
import fr.frinn.custommachinery.common.util.MachineShape;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MachineAppearanceBuilder {

    @Nullable
    private final MachineStatus status;
    private final Map<MachineAppearanceProperty<?>, Object> properties;

    public MachineAppearanceBuilder(@Nullable MachineStatus status) {
        this.status = status;
        Map<MachineAppearanceProperty<?>, Object> map = new HashMap<>();
        for(MachineAppearanceProperty<?> property : Registration.APPEARANCE_PROPERTY_REGISTRY)
            map.put(property, property.getDefaultValue());
        this.properties = map;
    }

    public MachineAppearanceBuilder(Map<MachineAppearanceProperty<?>, Object> properties, @Nullable MachineStatus status) {
        this.status = status;
        Map<MachineAppearanceProperty<?>, Object> map = new HashMap<>();
        for(MachineAppearanceProperty<?> property : Registration.APPEARANCE_PROPERTY_REGISTRY)
            if(!properties.containsKey(property) || properties.get(property) == null)
                map.put(property, property.getDefaultValue());
            else
                map.put(property, properties.get(property));
        this.properties = map;
    }

    @Nullable
    public MachineStatus getStatus() {
        return this.status;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(MachineAppearanceProperty<T> property) {
        if(!this.properties.containsKey(property))
            return property.getDefaultValue();
        return (T)this.properties.get(property);
    }

    public <T> void setProperty(MachineAppearanceProperty<T> property, T value) {
        this.properties.put(property, value);
    }

    public MachineModelLocation getBlockModel() {
        return getProperty(Registration.BLOCK_MODEL_PROPERTY.get());
    }

    public void setBlockModel(MachineModelLocation blockModel) {
        setProperty(Registration.BLOCK_MODEL_PROPERTY.get(), blockModel);
    }

    public MachineModelLocation getItemModel() {
        return getProperty(Registration.ITEM_MODEL_PROPERTY.get());
    }

    public void setItemModel(MachineModelLocation itemModel) {
        setProperty(Registration.ITEM_MODEL_PROPERTY.get(), itemModel);
    }

    public SoundEvent getSound() {
        return getProperty(Registration.AMBIENT_SOUND_PROPERTY.get());
    }

    public void setSound(SoundEvent sound) {
        setProperty(Registration.AMBIENT_SOUND_PROPERTY.get(), sound);
    }

    public int getLightLevel() {
        return getProperty(Registration.LIGHT_PROPERTY.get());
    }

    public void setLightLevel(int lightLevel) {
        setProperty(Registration.LIGHT_PROPERTY.get(), Mth.clamp(lightLevel, 0, 15));
    }

    public int getColor() {
        return getProperty(Registration.COLOR_PROPERTY.get());
    }

    public void setColor(int color) {
        setProperty(Registration.COLOR_PROPERTY.get(), color);
    }

    public float getHardness() {
        return getProperty(Registration.HARDNESS_PROPERTY.get());
    }

    public void setHardness(float hardness) {
        setProperty(Registration.HARDNESS_PROPERTY.get(), Mth.clamp(hardness, 0, Float.MAX_VALUE));
    }

    public float getResistance() {
        return getProperty(Registration.RESISTANCE_PROPERTY.get());
    }

    public void setResistance(float resistance) {
        setProperty(Registration.RESISTANCE_PROPERTY.get(), Mth.clamp(resistance, 0, Float.MAX_VALUE));
    }

    public List<TagKey<Block>> getToolType() {
        return getProperty(Registration.TOOL_TYPE_PROPERTY.get());
    }

    public void setToolType(TagKey<Block> toolType) {
        setProperty(Registration.TOOL_TYPE_PROPERTY.get(), Collections.singletonList(toolType));
    }

    public TagKey<Block> getMiningLevel() {
        return getProperty(Registration.MINING_LEVEL_PROPERTY.get());
    }

    public void setMiningLevel(TagKey<Block> miningLevel) {
        setProperty(Registration.MINING_LEVEL_PROPERTY.get(), miningLevel);
    }

    public MachineShape getShape() {
        return getProperty(Registration.SHAPE_PROPERTY.get());
    }

    public void setShape(MachineShape shape) {
        setProperty(Registration.SHAPE_PROPERTY.get(), shape);
    }

    public MachineAppearance build() {
        return new MachineAppearance(ImmutableMap.copyOf(this.properties));
    }
}
