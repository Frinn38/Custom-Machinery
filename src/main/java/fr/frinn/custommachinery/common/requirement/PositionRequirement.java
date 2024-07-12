package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.PositionMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.util.IntRange;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public record PositionRequirement(IntRange x, IntRange y, IntRange z) implements IRequirement<PositionMachineComponent> {

    public static final NamedCodec<PositionRequirement> CODEC = NamedCodec.record(positionRequirementInstance ->
        positionRequirementInstance.group(
                IntRange.CODEC.optionalFieldOf("x", IntRange.ALL).forGetter(requirement -> requirement.x),
                IntRange.CODEC.optionalFieldOf("y", IntRange.ALL).forGetter(requirement -> requirement.y),
                IntRange.CODEC.optionalFieldOf("z", IntRange.ALL).forGetter(requirement -> requirement.z)
        ).apply(positionRequirementInstance, PositionRequirement::new), "Position requirement"
    );

    @Override
    public RequirementType<PositionRequirement> getType() {
        return Registration.POSITION_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<PositionMachineComponent> getComponentType() {
        return Registration.POSITION_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(PositionMachineComponent component, ICraftingContext context) {
        BlockPos pos = component.getPosition();
        return this.x.contains(pos.getX()) && this.y.contains(pos.getY()) && this.z.contains(pos.getZ());
    }

    @Override
    public void gatherRequirements(IRequirementList<PositionMachineComponent> list) {
        //The machine won't move so not checking per tick, only at the beginning.
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        info.addTooltip(Component.translatable("custommachinery.requirements.position.info.pos").withStyle(ChatFormatting.AQUA));
        info.addTooltip(Component.literal("X: ").append(Component.literal(this.x.toFormattedString())));
        info.addTooltip(Component.literal("Y: ").append(Component.literal(this.y.toFormattedString())));
        info.addTooltip(Component.literal("Z: ").append(Component.literal(this.z.toFormattedString())));
        info.setItemIcon(Items.COMPASS);
    }
}
