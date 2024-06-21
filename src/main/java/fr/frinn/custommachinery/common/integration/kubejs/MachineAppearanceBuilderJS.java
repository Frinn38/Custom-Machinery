package fr.frinn.custommachinery.common.integration.kubejs;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.util.CMSoundType;
import fr.frinn.custommachinery.common.util.MachineModelLocation;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MachineAppearanceBuilderJS {

    private final Map<MachineAppearanceProperty<?>, Object> properties = MachineAppearance.defaultProperties();

    public MachineAppearanceBuilderJS() {

    }

    public MachineAppearanceBuilderJS block(ResourceLocation block) {
        this.put(Registration.BLOCK_MODEL_PROPERTY.get(), MachineModelLocation.of(block.toString()));
        return this;
    }

    public MachineAppearanceBuilderJS item(ResourceLocation item) {
        this.put(Registration.ITEM_MODEL_PROPERTY.get(), MachineModelLocation.of(item.toString()));
        return this;
    }

    public MachineAppearanceBuilderJS ambientSound(SoundEvent sound) {
        this.put(Registration.AMBIENT_SOUND_PROPERTY.get(), sound);
        return this;
    }

    public MachineAppearanceBuilderJS interactionSound(Block sound) {
        this.put(Registration.INTERACTION_SOUND_PROPERTY.get(), new CMSoundType(new PartialBlockState(sound)));
        return this;
    }

    public MachineAppearanceBuilderJS light(int light) {
        this.put(Registration.LIGHT_PROPERTY.get(), Mth.clamp(light, 0, 15));
        return this;
    }

    public MachineAppearanceBuilderJS color(int color) {
        this.put(Registration.COLOR_PROPERTY.get(), color);
        return this;
    }

    public MachineAppearanceBuilderJS hardness(float hardness) {
        this.put(Registration.HARDNESS_PROPERTY.get(), hardness);
        return this;
    }

    public MachineAppearanceBuilderJS resistance(float resistance) {
        this.put(Registration.RESISTANCE_PROPERTY.get(), resistance);
        return this;
    }

    public MachineAppearanceBuilderJS toolType(ResourceLocation[] tools) {
        List<TagKey<Block>> list = Arrays.stream(tools).map(key -> TagKey.create(Registries.BLOCK, key)).toList();
        this.put(Registration.TOOL_TYPE_PROPERTY.get(), list);
        return this;
    }

    public MachineAppearanceBuilderJS miningLevel(ResourceLocation key) {
        TagKey<Block> level = TagKey.create(Registries.BLOCK, key);
        this.put(Registration.MINING_LEVEL_PROPERTY.get(), level);
        return this;
    }

    public MachineAppearanceBuilderJS requiresTool(boolean requires) {
        this.put(Registration.REQUIRES_TOOL.get(), requires);
        return this;
    }

    private <T> void put(MachineAppearanceProperty<T> property, T value) {
        this.properties.put(property, value);
    }

    public MachineAppearance build() {
        return new MachineAppearance(this.properties);
    }
}
