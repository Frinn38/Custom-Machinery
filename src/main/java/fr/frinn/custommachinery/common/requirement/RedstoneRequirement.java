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
import fr.frinn.custommachinery.common.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.util.IntRange;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public record RedstoneRequirement(IntRange power) implements IRequirement<RedstoneMachineComponent> {

    public static final NamedCodec<RedstoneRequirement> CODEC = NamedCodec.record(redstoneRequirementInstance ->
            redstoneRequirementInstance.group(
                    IntRange.CODEC.fieldOf("power").forGetter(requirement -> requirement.power)
            ).apply(redstoneRequirementInstance, RedstoneRequirement::new), "Redstone requirement"
    );

    @Override
    public RequirementType<RedstoneRequirement> getType() {
        return Registration.REDSTONE_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<RedstoneMachineComponent> getComponentType() {
        return Registration.REDSTONE_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(RedstoneMachineComponent component, ICraftingContext context) {
        return this.power.contains(component.getMachinePower());
    }

    @Override
    public void gatherRequirements(IRequirementList<RedstoneMachineComponent> list) {
        list.worldCondition(this::check);
    }

    private CraftingResult check(RedstoneMachineComponent component, ICraftingContext context) {
        if(this.test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(Component.translatable("custommachinery.requirements.redstone.error", this.power.toFormattedString()));
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        info.addTooltip(Component.translatable("custommachinery.requirements.redstone.info", this.power.toFormattedString()))
                .setItemIcon(Items.REDSTONE);
    }
}
