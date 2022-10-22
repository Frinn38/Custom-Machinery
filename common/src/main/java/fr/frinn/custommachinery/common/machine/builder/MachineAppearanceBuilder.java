package fr.frinn.custommachinery.common.machine.builder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.impl.util.ModelLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public class MachineAppearanceBuilder {

    private final Map<MachineAppearanceProperty<?>, Object> properties;

    public MachineAppearanceBuilder() {
        ImmutableMap.Builder<MachineAppearanceProperty<?>, Object> builder = ImmutableMap.builder();
        for(MachineAppearanceProperty<?> property : Registration.APPEARANCE_PROPERTY_REGISTRY)
            builder.put(property, property.getDefaultValue());
        this.properties = builder.build();
    }

    public MachineAppearanceBuilder(MachineAppearance appearance) {
        this.properties = Maps.newHashMap(appearance.getProperties());
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(MachineAppearanceProperty<T> property) {
        if(!this.properties.containsKey(property))
            throw new IllegalStateException("Can't get Machine Appearance property for: " + property.getId() + ", this property may not be registered");
        return (T)this.properties.get(property);
    }

    public <T> void setProperty(MachineAppearanceProperty<T> property, T value) {
        this.properties.put(property, value);
    }

    public ModelLocation getBlockModel() {
        return getProperty(Registration.BLOCK_MODEL_PROPERTY.get());
    }

    public void setBlockModel(ModelLocation blockModel) {
        setProperty(Registration.BLOCK_MODEL_PROPERTY.get(), blockModel);
    }

    public ModelLocation getItemModel() {
        return getProperty(Registration.ITEM_MODEL_PROPERTY.get());
    }

    public void setItemModel(ModelLocation itemModel) {
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

    public VoxelShape getShape() {
        return getProperty(Registration.SHAPE_PROPERTY.get());
    }

    public void setShape(VoxelShape shape) {
        setProperty(Registration.SHAPE_PROPERTY.get(), shape);
    }

    public MachineAppearance build() {
        return new MachineAppearance(ImmutableMap.copyOf(this.properties));
    }
}
