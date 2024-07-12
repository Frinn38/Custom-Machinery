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
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public record FunctionRequirement(Phase phase, Function<ICraftingContext, CraftingResult> function, Consumer<Throwable> logger) implements IRequirement<FunctionMachineComponent> {

    public static final NamedCodec<FunctionRequirement> CODEC = NamedCodec.unit(new FunctionRequirement(Phase.START, ctx -> CraftingResult.pass(), error -> {}));
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
            return this.function.apply(context);
        } catch (Throwable error) {
            errors.add(this);
            this.logger.accept(error);
            return CraftingResult.error(Component.translatable("custommachinery.requirements.function.error"));
        }
    }

    public enum Phase {
        CHECK, START, TICK, END
    }
}
