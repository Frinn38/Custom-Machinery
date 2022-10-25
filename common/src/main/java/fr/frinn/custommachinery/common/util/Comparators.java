package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.init.Registration;

import java.util.Comparator;

public class Comparators {

    public static final Comparator<IGuiElement> GUI_ELEMENTS_COMPARATOR = Comparator.comparingInt(IGuiElement::getPriority);

    public static final Comparator<IMachineRecipe> RECIPE_PRIORITY_COMPARATOR = Comparator.comparingInt(IMachineRecipe::getPriority);

    public static final Comparator<IMachineRecipe> JEI_PRIORITY_COMPARATOR = Comparator.comparingInt(IMachineRecipe::getJeiPriority);

    public static final Comparator<IRequirement<?>> REQUIREMENT_COMPARATOR = Comparator.comparing(requirement -> requirement.getType() == Registration.COMMAND_REQUIREMENT.get() ? 1 : -1);
}
