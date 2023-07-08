package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.SkyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import net.minecraft.network.chat.TranslatableComponent;

public class SkyRequirement extends AbstractRequirement<SkyMachineComponent> implements ITickableRequirement<SkyMachineComponent> {

    public static final NamedCodec<SkyRequirement> CODEC = NamedCodec.unit(SkyRequirement::new, "Sky requirement");

    public SkyRequirement() {
        super(RequirementIOMode.INPUT);
    }

    @Override
    public RequirementType<SkyRequirement> getType() {
        return Registration.SKY_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<SkyMachineComponent> getComponentType() {
        return Registration.SKY_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(SkyMachineComponent component, ICraftingContext context) {
        return component.canSeeSky();
    }

    @Override
    public CraftingResult processStart(SkyMachineComponent component, ICraftingContext context) {
        if(test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.sky.error"));
    }

    @Override
    public CraftingResult processEnd(SkyMachineComponent component, ICraftingContext context) {
        if(test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.sky.error"));
    }

    @Override
    public CraftingResult processTick(SkyMachineComponent component, ICraftingContext context) {
        if(test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.sky.error"));
    }
}
