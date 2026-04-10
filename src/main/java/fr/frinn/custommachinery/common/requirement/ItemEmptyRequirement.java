package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;

public record ItemEmptyRequirement(String slot) implements IRequirement<ItemComponentHandler> {

    public static final NamedCodec<ItemEmptyRequirement> CODEC = NamedCodec.record(itemEmptyRequirementInstance ->
            itemEmptyRequirementInstance.group(
                    NamedCodec.STRING.optionalFieldOf("slot", "").forGetter(ItemEmptyRequirement::slot)
            ).apply(itemEmptyRequirementInstance, ItemEmptyRequirement::new), "Empty item requirement"
    );

    @Override
    public RequirementType<ItemEmptyRequirement> getType() {
        return Registration.ITEM_EMPTY_REQUIREMENT.get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(ItemComponentHandler handler, ICraftingContext context) {
        return handler.isInputSlotEmpty(this.slot);
    }

    @Override
    public void gatherRequirements(IRequirementList<ItemComponentHandler> list) {

    }
}
