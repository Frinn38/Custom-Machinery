package fr.frinn.custommachinery.common.integration.kubejs;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.util.MachineModelLocation.MachineBlockModelLocation;
import fr.frinn.custommachinery.common.util.MachineModelLocation.MachineItemModelLocation;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.sound.AmbientSound;
import fr.frinn.custommachinery.common.util.sound.CMSoundType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MachineAppearanceBuilderJS {

    private final Map<MachineAppearanceProperty<?>, Object> properties = MachineAppearance.defaultProperties();

    public MachineAppearanceBuilderJS() {

    }

    public MachineAppearanceBuilderJS block(Identifier id) {
        this.put(CMRegistration.BLOCK_MODEL_PROPERTY.get(), new MachineBlockModelLocation(Either.right(id)));
        return this;
    }

    public MachineAppearanceBuilderJS block(BlockState blockState) {
        this.put(CMRegistration.BLOCK_MODEL_PROPERTY.get(), new MachineBlockModelLocation(Either.left(blockState)));
        return this;
    }

    public MachineAppearanceBuilderJS item(Identifier id) {
        this.put(CMRegistration.ITEM_MODEL_PROPERTY.get(), new MachineItemModelLocation(Either.right(id)));
        return this;
    }

    public MachineAppearanceBuilderJS item(ItemStack item) {
        this.put(CMRegistration.ITEM_MODEL_PROPERTY.get(), new MachineItemModelLocation(Either.left(item)));
        return this;
    }

    public MachineAppearanceBuilderJS ambientSound(SoundEvent sound) {
        return ambientSound(sound, 1, 1);
    }

    public MachineAppearanceBuilderJS ambientSound(SoundEvent sound, float volume, float pitch) {
        this.put(CMRegistration.AMBIENT_SOUND_PROPERTY.get(), new AmbientSound(sound, volume, pitch, SoundSource.BLOCKS, true, true, 0, false));
        return this;
    }

    public MachineAppearanceBuilderJS ambientSound(SoundEvent sound, float volume, float pitch, SoundSource source, boolean loop, boolean attenuation, int delay, boolean relative) {
        this.put(CMRegistration.AMBIENT_SOUND_PROPERTY.get(), new AmbientSound(sound, volume, pitch, source, loop, attenuation, delay, relative));
        return this;
    }

    public MachineAppearanceBuilderJS interactionSound(Block sound) {
        this.put(CMRegistration.INTERACTION_SOUND_PROPERTY.get(), new CMSoundType(new PartialBlockState(sound)));
        return this;
    }

    public MachineAppearanceBuilderJS light(int light) {
        this.put(CMRegistration.LIGHT_PROPERTY.get(), Mth.clamp(light, 0, 15));
        return this;
    }

    public MachineAppearanceBuilderJS color(int color) {
        this.put(CMRegistration.COLOR_PROPERTY.get(), color);
        return this;
    }

    public MachineAppearanceBuilderJS hardness(float hardness) {
        this.put(CMRegistration.HARDNESS_PROPERTY.get(), hardness);
        return this;
    }

    public MachineAppearanceBuilderJS resistance(float resistance) {
        this.put(CMRegistration.RESISTANCE_PROPERTY.get(), resistance);
        return this;
    }

    public MachineAppearanceBuilderJS toolType(Identifier[] tools) {
        List<TagKey<Block>> list = Arrays.stream(tools).map(key -> TagKey.create(Registries.BLOCK, key)).toList();
        this.put(CMRegistration.TOOL_TYPE_PROPERTY.get(), list);
        return this;
    }

    public MachineAppearanceBuilderJS miningLevel(Identifier key) {
        TagKey<Block> level = TagKey.create(Registries.BLOCK, key);
        this.put(CMRegistration.MINING_LEVEL_PROPERTY.get(), level);
        return this;
    }

    public MachineAppearanceBuilderJS requiresTool(boolean requires) {
        this.put(CMRegistration.REQUIRES_TOOL.get(), requires);
        return this;
    }

    private <T> void put(MachineAppearanceProperty<T> property, T value) {
        this.properties.put(property, value);
    }

    public MachineAppearance build() {
        return new MachineAppearance(this.properties);
    }
}
