package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractDelayedChanceableRequirement;
import fr.frinn.custommachinery.common.data.component.FunctionMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;

import java.util.function.Function;

public class FunctionRequirement extends AbstractDelayedChanceableRequirement<FunctionMachineComponent> implements ITickableRequirement<FunctionMachineComponent> {

    public static final Codec<FunctionRequirement> CODEC = Codec.unit(new FunctionRequirement(Phase.START, ctx -> CraftingResult.pass()));

    private Phase phase;
    private final Function<ICraftingContext, CraftingResult> function;

    public FunctionRequirement(Phase phase, Function<ICraftingContext, CraftingResult> function) {
        super(RequirementIOMode.INPUT);
        this.phase = phase;
        this.function = function;
    }

    @Override
    public RequirementType<FunctionRequirement> getType() {
        return Registration.FUNCTION_REQUIREMENT.get();
    }

    @Override
    public CraftingResult execute(FunctionMachineComponent component, ICraftingContext context) {
        if(this.phase != Phase.DELAY)
            return CraftingResult.pass();
        return this.function.apply(context);
    }

    @Override
    public MachineComponentType<FunctionMachineComponent> getComponentType() {
        return Registration.FUNCTION_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(FunctionMachineComponent component, ICraftingContext context) {
        if(this.phase != Phase.CHECK)
            return true;
        return this.function.apply(context).isSuccess();
    }

    @Override
    public CraftingResult processStart(FunctionMachineComponent component, ICraftingContext context) {
        if(this.phase != Phase.START)
            return CraftingResult.pass();
        return this.function.apply(context);
    }

    @Override
    public CraftingResult processEnd(FunctionMachineComponent component, ICraftingContext context) {
        if(this.phase != Phase.END)
            return CraftingResult.pass();
        return this.function.apply(context);
    }

    @Override
    public CraftingResult processTick(FunctionMachineComponent component, ICraftingContext context) {
        if(this.phase != Phase.TICK)
            return CraftingResult.pass();
        return this.function.apply(context);
    }

    @Override
    public void setDelay(double delay) {
        super.setDelay(delay);
        this.phase = Phase.DELAY;
    }

    public enum Phase {
        CHECK, START, TICK, END, DELAY
    }
}
