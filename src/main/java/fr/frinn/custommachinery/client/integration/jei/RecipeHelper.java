package fr.frinn.custommachinery.client.integration.jei;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.common.component.DummyComponentManager;
import fr.frinn.custommachinery.common.component.MachineComponentManager;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import mezz.jei.api.helpers.IJeiHelpers;
import net.minecraft.core.BlockPos;

import java.util.Optional;

public class RecipeHelper implements IRecipeHelper {

    private final CustomMachine machine;
    private final MachineComponentManager manager;
    private final IJeiHelpers jeiHelpers;

    public RecipeHelper(CustomMachine machine, IJeiHelpers jeiHelpers) {
        this.machine = machine;
        CustomMachineTile tile = new CustomMachineTile(BlockPos.ZERO, Registration.CUSTOM_MACHINE_BLOCK.get().defaultBlockState());
        tile.setId(machine.getId());
        this.manager = new DummyComponentManager(tile);
        this.jeiHelpers = jeiHelpers;
    }

    @Override
    public CustomMachine getMachine() {
        return machine;
    }

    @Override
    public Optional<IMachineComponentTemplate<?>> getComponentForElement(IComponentGuiElement<?> element) {
        return this.machine.getComponentTemplates().stream().filter(template -> {
            if(!template.getId().equals(element.getComponentId()))
                return false;
            //Special case for slot gui element because several components of different types (default, filter, fluid etc...) can map to it.
            if(element.getComponentType() == Registration.ITEM_MACHINE_COMPONENT.get())
                return template instanceof ItemMachineComponent.Template;
            return template.getType() == element.getComponentType();
        }).findFirst();
    }

    @Override
    public MachineComponentManager getDummyManager() {
        return this.manager;
    }

    @Override
    public IJeiHelpers getJeiHelpers() {
        return this.jeiHelpers;
    }
}
