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
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.List;

public record DimensionRequirement(List<ResourceLocation> filter, boolean blacklist) implements IRequirement<PositionMachineComponent> {

    public static final NamedCodec<DimensionRequirement> CODEC = NamedCodec.record(dimensionRequirementInstance ->
            dimensionRequirementInstance.group(
                    DefaultCodecs.RESOURCE_LOCATION.listOf().fieldOf("filter").forGetter(requirement -> requirement.filter),
                    NamedCodec.BOOL.optionalFieldOf("blacklist", false).forGetter(requirement -> requirement.blacklist)
            ).apply(dimensionRequirementInstance, DimensionRequirement::new), "Dimension requirement"
    );

    @Override
    public RequirementType<DimensionRequirement> getType() {
        return Registration.DIMENSION_REQUIREMENT.get();
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
        return this.filter.contains(component.getDimension().location()) != this.blacklist;
    }

    @Override
    public void gatherRequirements(IRequirementList<PositionMachineComponent> list) {
        //The machine won't move, and we can assume the dimension won't change (at least not during a recipe) so not checking per tick, only at the beginning.
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        if(!this.filter.isEmpty()) {
            if(this.blacklist)
                info.addTooltip(Component.translatable("custommachinery.requirements.position.info.dimension.blacklist").withStyle(ChatFormatting.DARK_RED));
            else
                info.addTooltip(Component.translatable("custommachinery.requirements.position.info.dimension.whitelist").withStyle(ChatFormatting.DARK_GREEN));
            this.filter.forEach(dimension -> info.addTooltip(Component.literal("* " + dimension)));
        }
        info.setSpriteIcon(InventoryMenu.BLOCK_ATLAS, ResourceLocation.withDefaultNamespace("block/nether_portal"));
    }
}
