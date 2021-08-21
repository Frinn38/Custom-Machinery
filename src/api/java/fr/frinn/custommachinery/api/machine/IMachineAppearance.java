package fr.frinn.custommachinery.api.machine;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

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
}
