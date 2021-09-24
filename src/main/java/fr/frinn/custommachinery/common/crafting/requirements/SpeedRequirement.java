package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;

public class SpeedRequirement extends AbstractRequirement<AbstractMachineComponent> {

    public static final Codec<SpeedRequirement> CODEC = Codec.unit(SpeedRequirement::new).stable();

    public SpeedRequirement() {
        super(MODE.INPUT);
    }

    @Override
    public RequirementType<SpeedRequirement> getType() {
        return Registration.SPEED_REQUIREMENT.get();
    }

    @Override
    public boolean test(AbstractMachineComponent component, CraftingContext context) {
        return false;
    }

    @Override
    public CraftingResult processStart(AbstractMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(AbstractMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<AbstractMachineComponent> getComponentType() {
        return null;
    }
}
