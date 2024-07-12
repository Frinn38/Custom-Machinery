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
import fr.frinn.custommachinery.common.component.LightMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.util.IntRange;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public record LightRequirement(IntRange light, boolean sky) implements IRequirement<LightMachineComponent> {

    public static final NamedCodec<LightRequirement> CODEC = NamedCodec.record(lightRequirementInstance ->
            lightRequirementInstance.group(
                    IntRange.CODEC.fieldOf("light").forGetter(requirement -> requirement.light),
                    NamedCodec.BOOL.optionalFieldOf("sky", false).forGetter(requirement -> requirement.sky)
            ).apply(lightRequirementInstance, LightRequirement::new), "Light requirement"
    );

    @Override
    public RequirementType<LightRequirement> getType() {
        return Registration.LIGHT_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<LightMachineComponent> getComponentType() {
        return Registration.LIGHT_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(LightMachineComponent component, ICraftingContext context) {
        if(this.sky)
            return this.light.contains(component.getSkyLight());
        return this.light.contains(component.getBlockLight());
    }

    @Override
    public void gatherRequirements(IRequirementList<LightMachineComponent> list) {
        list.worldCondition(this::check);
    }

    private CraftingResult check(LightMachineComponent component, ICraftingContext context) {
        if(this.test(component, context))
            return CraftingResult.success();
        if(this.sky)
            return CraftingResult.error(Component.translatable("custommachinery.requirements.light.sky.error", this.light.toFormattedString(), component.getSkyLight()));
        else
            return CraftingResult.error(Component.translatable("custommachinery.requirements.light.block.error", this.light, component.getBlockLight()));
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        if(this.sky)
            info.addTooltip(Component.translatable("custommachinery.requirements.light.sky.info", this.light.toFormattedString()));
        else
            info.addTooltip(Component.translatable("custommachinery.requirements.light.block.info", this.light.toFormattedString()));
        info.setItemIcon(Items.TORCH);
    }
}
