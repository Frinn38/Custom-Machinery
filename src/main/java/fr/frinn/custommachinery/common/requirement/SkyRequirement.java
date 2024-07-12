package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.SkyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class SkyRequirement implements IRequirement<SkyMachineComponent> {

    public static final NamedCodec<SkyRequirement> CODEC = NamedCodec.unit(SkyRequirement::new, "Sky requirement");

    @Override
    public RequirementType<SkyRequirement> getType() {
        return Registration.SKY_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<SkyMachineComponent> getComponentType() {
        return Registration.SKY_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(SkyMachineComponent component, ICraftingContext context) {
        return component.canSeeSky();
    }

    @Override
    public void gatherRequirements(IRequirementList<SkyMachineComponent> list) {
        list.worldCondition(this::check);
    }

    private CraftingResult check(SkyMachineComponent component, ICraftingContext context) {
        if(component.canSeeSky())
            return CraftingResult.success();
        return CraftingResult.error(Component.translatable("custommachinery.requirements.sky.error"));
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        info.setItemIcon(Items.DAYLIGHT_DETECTOR);
        info.addTooltip(Component.translatable("custommachinery.requirements.sky.error"));
    }
}
