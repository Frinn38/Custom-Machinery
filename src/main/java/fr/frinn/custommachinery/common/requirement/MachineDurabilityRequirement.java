package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.DurabilityMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;

public record MachineDurabilityRequirement(RequirementIOMode mode, int amount) implements IRequirement<DurabilityMachineComponent> {

    public static final NamedCodec<MachineDurabilityRequirement> CODEC = NamedCodec.record(machineDurabilityRequirementInstance ->
            machineDurabilityRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(MachineDurabilityRequirement::mode),
                    NamedCodec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(MachineDurabilityRequirement::amount)
            ).apply(machineDurabilityRequirementInstance, MachineDurabilityRequirement::new), "Machine durability requirement"
    );

    @Override
    public RequirementType<MachineDurabilityRequirement> getType() {
        return Registration.MACHINE_DURABILITY_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<DurabilityMachineComponent> getComponentType() {
        return Registration.DURABILITY_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return this.mode;
    }

    @Override
    public boolean test(DurabilityMachineComponent component, ICraftingContext context) {
        double amount = context.getModifiedValue(this.amount, this, null);
        if(this.mode == RequirementIOMode.INPUT)
            return component.getDurability() >= amount;
        else
            return component.getMaxDurability() - component.getDurability() >= amount;
    }

    @Override
    public void gatherRequirements(IRequirementList<DurabilityMachineComponent> list) {
        if(this.mode == RequirementIOMode.INPUT)
            list.processOnStart(((component, context) -> {
                double amount = context.getModifiedValue(this.amount, this, null);
                component.damage((int)amount);
                return CraftingResult.success();
            }));
        else
            list.processOnEnd(((component, context) -> {
                double amount = context.getModifiedValue(this.amount, this, null);
                component.repair((int)amount);
                return CraftingResult.success();
            }));
    }
}
