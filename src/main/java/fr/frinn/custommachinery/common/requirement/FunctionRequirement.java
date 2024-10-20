package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.FunctionMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.kubejs.KubeJSIntegration;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public record FunctionRequirement(Phase phase, String id) implements IRequirement<FunctionMachineComponent> {

    public static final NamedCodec<FunctionRequirement> CODEC = NamedCodec.record(functionRequirementInstance ->
            functionRequirementInstance.group(
                    NamedCodec.enumCodec(Phase.class).fieldOf("phase").forGetter(FunctionRequirement::phase),
                    NamedCodec.STRING.fieldOf("id").forGetter(FunctionRequirement::id)
            ).apply(functionRequirementInstance, FunctionRequirement::new), "Function requirement"
    );
    public static final List<FunctionRequirement> errors = new ArrayList<>();

    @Override
    public RequirementType<FunctionRequirement> getType() {
        return Registration.FUNCTION_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<FunctionMachineComponent> getComponentType() {
        return Registration.FUNCTION_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(FunctionMachineComponent component, ICraftingContext context) {
        return this.phase != Phase.CHECK || this.execute(component, context).isSuccess();
    }

    @Override
    public void gatherRequirements(IRequirementList<FunctionMachineComponent> list) {
        switch(this.phase) {
            case START -> list.processOnStart(this::execute);
            case TICK -> list.processEachTick(this::execute);
            case END -> list.processOnEnd(this::execute);
        }
    }

    private CraftingResult execute(FunctionMachineComponent component, ICraftingContext context) {
        if(errors.contains(this))
            return CraftingResult.error(Component.translatable("custommachinery.requirements.function.error"));

        try {
            if(ModList.get().isLoaded("kubejs"))
                return KubeJSIntegration.sendFunctionRequirementEvent(this.id, context);
            else
                throw new IllegalStateException("Trying to process function requirement for id: " + this.id + " without KubeJS installed !");
        } catch (Throwable error) {
            errors.add(this);
            if(ModList.get().isLoaded("kubejs"))
                KubeJSIntegration.logError(error);
            return CraftingResult.error(Component.translatable("custommachinery.requirements.function.error"));
        }
    }

    public enum Phase {
        CHECK, START, TICK, END
    }
}
