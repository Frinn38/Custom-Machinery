package fr.frinn.custommachinery.common.crafting.requirements;

import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;

import java.util.Locale;

public interface IRequirement<T extends IMachineComponent> {

    RequirementType<? extends IRequirement<?>> getType();

    boolean test(T component);

    CraftingResult processStart(T component);

    CraftingResult processEnd(T component);

    MODE getMode();

    MachineComponentType getComponentType();

    enum MODE {
        INPUT,
        OUTPUT;

        static MODE value(String mode) {
            return valueOf(mode.toUpperCase(Locale.ENGLISH));
        }
    }
}
