package fr.frinn.custommachinery.api.integration.jei;

import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.machine.ICustomMachine;

import java.util.Optional;

public interface IRecipeHelper {

    ICustomMachine getMachine();

    Optional<IMachineComponentTemplate<?>> getComponentForElement(IComponentGuiElement<?> element);

    IMachineComponentManager getDummyManager();
}
