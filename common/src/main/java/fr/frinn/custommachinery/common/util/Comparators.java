package fr.frinn.custommachinery.common.util;

import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.UpgradedCustomMachine;

import java.util.Comparator;

public class Comparators {

    public static final Comparator<IGuiElement> GUI_ELEMENTS_COMPARATOR = Comparator.comparingInt(IGuiElement::getPriority);

    public static final Comparator<IMachineRecipe> RECIPE_PRIORITY_COMPARATOR = Comparator.comparingInt(IMachineRecipe::getPriority);

    public static final Comparator<IMachineRecipe> JEI_PRIORITY_COMPARATOR = Comparator.comparingInt(IMachineRecipe::getJeiPriority);

    public static final Comparator<IRequirement<?>> REQUIREMENT_COMPARATOR = Comparator.comparing(requirement -> requirement.getType() == Registration.COMMAND_REQUIREMENT.get() ? 1 : -1);

    public static final Comparator<CustomMachine> PARENT_MACHINE_FIRST = (machine1, machine2) -> {

        if(machine1 instanceof UpgradedCustomMachine upgraded1 && machine2 instanceof UpgradedCustomMachine upgraded2) {
            if(upgraded1.getParentId().equals(machine2.getId()))
                return 1; //machine2 is parent of machine1
            else if(upgraded2.getParentId().equals(machine1.getId()))
                return -1; //machine1 is parent of machine2
            else
                return 0; //Both are upgraded machines but not related
        }
        else if(machine1 instanceof UpgradedCustomMachine)
            return 1; //machine1 is upgraded but not machine2
        else if(machine2 instanceof UpgradedCustomMachine)
            return -1; //machine2 is upgraded but not machine1
        else
            return 0; //Both are normal machines
    };
}
