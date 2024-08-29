package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessorCore;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class WorkingCoreMachineComponent extends AbstractMachineComponent {

    public WorkingCoreMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<WorkingCoreMachineComponent> getType() {
        return Registration.WORKING_CORE_MACHINE_COMPONENT.get();
    }

    public CraftingResult isCoreWorking(int coreId, @Nullable ResourceLocation recipe, int currentCore) {
        if(this.getManager().getTile().getProcessor() instanceof MachineProcessor processor) {
            if(coreId == 0) {
                if(recipe == null && processor.getCores().stream().noneMatch(core -> processor.getCores().indexOf(core) != currentCore && core.getCurrentRecipe() != null && core.getError() == null))
                    return CraftingResult.error(Component.translatable("custommachinery.requirements.working_core.all_idle"));
                else if(recipe != null && processor.getCores().stream().noneMatch(core -> processor.getCores().indexOf(core) != currentCore && core.getCurrentRecipe() != null && core.getCurrentRecipe().id().equals(recipe)))
                    return CraftingResult.error(Component.translatable("custommachinery.requirements.working_core.all_bad", recipe.toString()));
                else
                    return CraftingResult.success();
            } else {
                if(coreId - 1 >= processor.getCores().size())
                    return CraftingResult.error(Component.translatable("custommachinery.requirements.working_core.bad_core", coreId, processor.getCores().size()));
                MachineProcessorCore core = processor.getCores().get(coreId - 1);
                if(core.getCurrentRecipe() == null || core.getError() != null)
                    return CraftingResult.error(Component.translatable("custommachinery.requirements.working_core.idle", coreId));
                else if(recipe != null && !core.getCurrentRecipe().id().equals(recipe))
                    return CraftingResult.error(Component.translatable("custommachinery.requirements.working_core.bad_recipe", coreId, recipe.toString()));
                else
                    return CraftingResult.success();
            }
        } else
            return CraftingResult.error(Component.translatable("custommachinery.requirements.working_core.bad_processor"));
    }
}
