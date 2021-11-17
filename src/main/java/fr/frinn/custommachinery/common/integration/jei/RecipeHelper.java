package fr.frinn.custommachinery.common.integration.jei;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.component.DummyComponentManager;
import fr.frinn.custommachinery.common.data.component.MachineComponentManager;
import fr.frinn.custommachinery.common.data.gui.IComponentGuiElement;
import fr.frinn.custommachinery.common.init.CustomMachineTile;

import java.util.Optional;

public class RecipeHelper {

    private final CustomMachine machine;
    private final MachineComponentManager manager;

    public RecipeHelper(CustomMachine machine) {
        this.machine = machine;
        CustomMachineTile tile = new CustomMachineTile();
        tile.setId(machine.getId());
        this.manager = new DummyComponentManager(tile);
    }

    public CustomMachine getMachine() {
        return machine;
    }

    public Optional<IMachineComponentTemplate<?>> getComponentForElement(IComponentGuiElement<?> element) {
        return machine.getComponentTemplates().stream().filter(template -> template.getType() == element.getComponentType() && template.getId().equals(element.getID())).findFirst();
    }

    public MachineComponentManager getDummyManager() {
        return this.manager;
    }
}
