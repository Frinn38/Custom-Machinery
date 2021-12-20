package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.init.Registration;

public class SpeedRequirement extends AbstractRequirement<AbstractMachineComponent> {

    public static final Codec<SpeedRequirement> CODEC = Codec.unit(SpeedRequirement::new).stable();

    public SpeedRequirement() {
        super(RequirementIOMode.INPUT);
    }

    @Override
    public RequirementType<SpeedRequirement> getType() {
        return Registration.SPEED_REQUIREMENT.get();
    }

    @Override
    public boolean test(AbstractMachineComponent component, ICraftingContext context) {
        return false;
    }

    @Override
    public CraftingResult processStart(AbstractMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(AbstractMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<AbstractMachineComponent> getComponentType() {
        return null;
    }
}
