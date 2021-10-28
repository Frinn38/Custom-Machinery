package fr.frinn.custommachinery.api.machine;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.ToolType;

/**
 * Define the appearance of the machine.
 * The appearance of the machine is depending on it's MachineStatus.
 */
public interface IMachineAppearance {

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
    SoundEvent getSound();

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
     * @return The tool that can effectively break the machine.
     */
    ToolType getTool();

    /**
     * @return The minimal mining level the tool need to be able to break effectively the machine.
     */
    int getMiningLevel();

    /**
     * @return An exact copy of this IMachineAppearance, this must create a new instance and copy all the properties from the copied IMachineAppearance.
     * Used to access the IMachineAppearance properties from the render thread, via the MachineTile IModelData.
     */
    IMachineAppearance copy();
}
