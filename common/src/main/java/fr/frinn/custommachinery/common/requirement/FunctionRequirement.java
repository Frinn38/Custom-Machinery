package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.FunctionMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedChanceableRequirement;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

public class FunctionRequirement extends AbstractDelayedChanceableRequirement<FunctionMachineComponent> implements ITickableRequirement<FunctionMachineComponent> {

    public static final NamedCodec<FunctionRequirement> CODEC = NamedCodec.unit(new FunctionRequirement(Phase.START, ctx -> CraftingResult.pass(), error -> {}));

    private Phase phase;
    private final Function<ICraftingContext, CraftingResult> function;
    private final Consumer<Throwable> logger;
    private boolean errored = false;

    public FunctionRequirement(Phase phase, Function<ICraftingContext, CraftingResult> function, Consumer<Throwable> logger) {
        super(RequirementIOMode.INPUT);
        this.phase = phase;
        this.function = function;
        this.logger = logger;
    }

    private CraftingResult processFunction(ICraftingContext context) {
        if(this.errored)
            return CraftingResult.error(Component.translatable("custommachinery.requirements.function.error"));

        try {
            return this.function.apply(context);
        } catch (Throwable error) {
            this.errored = true;
            this.logger.accept(error);
            return CraftingResult.error(Component.translatable("custommachinery.requirements.function.error"));
        }
    }

    @Override
    public RequirementType<FunctionRequirement> getType() {
        return Registration.FUNCTION_REQUIREMENT.get();
    }

    @Override
    public CraftingResult execute(FunctionMachineComponent component, ICraftingContext context) {
        if(this.phase != Phase.DELAY)
            return CraftingResult.pass();
        return processFunction(context);
    }

    @Override
    public MachineComponentType<FunctionMachineComponent> getComponentType() {
        return Registration.FUNCTION_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(FunctionMachineComponent component, ICraftingContext context) {
        if(this.phase != Phase.CHECK)
            return true;
        return processFunction(context).isSuccess();
    }

    @Override
    public CraftingResult processStart(FunctionMachineComponent component, ICraftingContext context) {
        if(this.phase != Phase.START)
            return CraftingResult.pass();
        return processFunction(context);
    }

    @Override
    public CraftingResult processEnd(FunctionMachineComponent component, ICraftingContext context) {
        if(this.phase != Phase.END)
            return CraftingResult.pass();
        return processFunction(context);
    }

    @Override
    public CraftingResult processTick(FunctionMachineComponent component, ICraftingContext context) {
        if(this.phase != Phase.TICK)
            return CraftingResult.pass();
        return processFunction(context);
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
