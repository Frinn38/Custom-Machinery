package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.init.Registration;

import java.util.Comparator;

public class Comparators {

    public static final Comparator<IGuiElement> GUI_ELEMENTS_COMPARATOR = Comparator.comparingInt(IGuiElement::getPriority);

    public static final Comparator<CustomMachineRecipe> RECIPE_PRIORITY_COMPARATOR = Comparator.comparingInt(CustomMachineRecipe::getPriority);

    public static final Comparator<CustomMachineRecipe> JEI_PRIORITY_COMPARATOR = Comparator.comparingInt(CustomMachineRecipe::getJeiPriority);

    public static final Comparator<IRequirement<?>> REQUIREMENT_COMPARATOR = Comparator.comparing(requirement -> requirement.getType() == Registration.COMMAND_REQUIREMENT.get() ? 1 : -1);
}
