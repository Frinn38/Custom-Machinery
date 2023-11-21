package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.util.CMSoundType;
import fr.frinn.custommachinery.common.util.MachineModelLocation;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ZenRegister
@Name(CTConstants.APPEARANCE_BUILDER)
public class MachineAppearanceBuilderCT {

    private final Map<MachineAppearanceProperty<?>, Object> properties = MachineAppearance.defaultProperties();

    public MachineAppearanceBuilderCT() {

    }

    @Method
    @Setter("block")
    public MachineAppearanceBuilderCT block(String block) {
        this.put(Registration.BLOCK_MODEL_PROPERTY.get(), MachineModelLocation.of(block));
        return this;
    }

    @Method
    @Setter("item")
    public MachineAppearanceBuilderCT item(String item) {
        this.put(Registration.ITEM_MODEL_PROPERTY.get(), MachineModelLocation.of(item));
        return this;
    }

    @Method
    @Setter("ambient_sound")
    public MachineAppearanceBuilderCT ambientSound(SoundEvent sound) {
        this.put(Registration.AMBIENT_SOUND_PROPERTY.get(), sound);
        return this;
    }

    @Method
    @Setter("interaction_sound")
    public MachineAppearanceBuilderCT interactionSound(Block sound) {
        this.put(Registration.INTERACTION_SOUND_PROPERTY.get(), new CMSoundType(new PartialBlockState(sound)));
        return this;
    }

    @Method
    @Setter("light")
    public MachineAppearanceBuilderCT light(int light) {
        this.put(Registration.LIGHT_PROPERTY.get(), Mth.clamp(light, 0, 15));
        return this;
    }

    @Method
    @Setter("color")
    public MachineAppearanceBuilderCT color(int color) {
        this.put(Registration.COLOR_PROPERTY.get(), color);
        return this;
    }

    @Method
    @Setter("hardness")
    public MachineAppearanceBuilderCT hardness(float hardness) {
        this.put(Registration.HARDNESS_PROPERTY.get(), hardness);
        return this;
    }

    @Method
    @Setter("resistance")
    public MachineAppearanceBuilderCT resistance(float resistance) {
        this.put(Registration.RESISTANCE_PROPERTY.get(), resistance);
        return this;
    }

    @Method
    @Setter("tool_type")
    public MachineAppearanceBuilderCT toolType(String[] tools) {
        List<TagKey<Block>> list = Arrays.stream(tools).map(key -> TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(key))).toList();
        this.put(Registration.TOOL_TYPE_PROPERTY.get(), list);
        return this;
    }

    @Method
    @Setter("mining_level")
    public MachineAppearanceBuilderCT miningLevel(String key) {
        TagKey<Block> level = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(key));
        this.put(Registration.MINING_LEVEL_PROPERTY.get(), level);
        return this;
    }

    @Method
    @Setter("requires_tool")
    public MachineAppearanceBuilderCT requiresTool(boolean requires) {
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
