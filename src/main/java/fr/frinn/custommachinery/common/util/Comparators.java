package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import fr.frinn.custommachinery.common.init.Registration;

import java.util.Comparator;

public class Comparators {

    public static final Comparator<IGuiElement> GUI_ELEMENTS_COMPARATOR = Comparator.comparingInt(IGuiElement::getPriority);

    public static final Comparator<CustomMachineRecipe> CUSTOM_MACHINE_RECIPE_COMPARATOR = Comparator.comparingInt(CustomMachineRecipe::getPriority);

    public static final Comparator<IRequirement<?>> REQUIREMENT_COMPARATOR = Comparator.comparing(requirement -> requirement.getType() == Registration.COMMAND_REQUIREMENT.get() ? 1 : -1);
}
