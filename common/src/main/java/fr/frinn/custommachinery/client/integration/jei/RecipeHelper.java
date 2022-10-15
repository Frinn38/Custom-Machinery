package fr.frinn.custommachinery.client.integration.jei;

import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.common.component.DummyComponentManager;
import fr.frinn.custommachinery.common.component.MachineComponentManager;
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
        CustomMachineTile tile = PlatformHelper.createMachineTile(BlockPos.ZERO, Registration.CUSTOM_MACHINE_BLOCK.get().defaultBlockState());
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
        return machine.getComponentTemplates().stream().filter(template -> template.getType() == element.getComponentType() && template.getId().equals(element.getID())).findFirst();
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
