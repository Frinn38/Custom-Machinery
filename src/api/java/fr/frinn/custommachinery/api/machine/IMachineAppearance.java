package fr.frinn.custommachinery.api.machine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Define the appearance of the machine.
 * The appearance of the machine is depending on it's MachineStatus.
 * Each {@link MachineStatus} have their own instance of IMachineAppearance.
 */
public interface IMachineAppearance {

    /**
     * Default method to get a property of this machine appearance.
     * A machine appearance always contains one instance of each registered property.
     * For properties registered by Custom Machinery use the helpers methods below.
     * @param property The property to get the value from this appearance, MUST be registered in the forge registry.
     * @param <T> The type of property.
     * @return The value of the given property for this appearance.
     */
    <T> T getProperty(MachineAppearanceProperty<T> property);

    /**
     * @return The location of the model of the machine block.
     */
    ResourceLocation getBlockModel();

    /**
     * @return The location of the model of the machine item.
     */
    ResourceLocation getItemModel();

    /**
     * @return The sound that the machine will emmit.
     */
    SoundEvent getAmbientSound();

    /**
     * @return Which block the machine will copy the sounds when interacted by a player (place/break/step on...)
     */
    Block getInteractionSound();

    /**
     * @return The light level between 0 and 15 that the machine will emmit.
     */
    int getLightLevel();

    /**
     * @return A hex color that will be used when rendering the machine model for quads that have a tint index of 4.
     */
    int getColor();

    /**
     * @return A float that represent the block hardness of the machine, used to calculate the player breaking speed.
     */
    float getHardness();

    /**
     * @return A float that represent the Machine block explosion resistance, used to calculate the radius of an explosion that occurs near the machine.
     */
    float getResistance();

    /**
     * @return The tools that can effectively break the machine.
     */
    List<TagKey<Block>> getTool();

    /**
     * @return The minimal mining level the tool need to be able to break effectively the machine.
     */
    TagKey<Block> getMiningLevel();

    /**
     * @return True if the player need one of the tools returned by {@link #getTool()} to make the machine drop when broken, false otherwise.
     */
    boolean requiresCorrectToolForDrops();

    /**
     * @return The shape of the machine, used for collisions and block outline.
     */
    VoxelShape getShape();

    /**
     * @return An exact copy of this IMachineAppearance, this must create a new instance and copy all the properties from the copied IMachineAppearance.
     * Used to access the IMachineAppearance properties from the render thread, via the MachineTile IModelData.
     */
    IMachineAppearance copy();
}
