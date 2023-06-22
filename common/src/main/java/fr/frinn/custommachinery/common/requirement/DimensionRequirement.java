package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.PositionMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.List;

public class DimensionRequirement extends AbstractRequirement<PositionMachineComponent> implements IDisplayInfoRequirement {

    public static final NamedCodec<DimensionRequirement> CODEC = NamedCodec.record(dimensionRequirementInstance ->
            dimensionRequirementInstance.group(
                    DefaultCodecs.RESOURCE_LOCATION.listOf().fieldOf("filter").forGetter(requirement -> requirement.filter),
                    NamedCodec.BOOL.optionalFieldOf("blacklist", false).forGetter(requirement -> requirement.blacklist)
            ).apply(dimensionRequirementInstance, DimensionRequirement::new), "Dimension requirement"
    );

    private final List<ResourceLocation> filter;
    private final boolean blacklist;

    public DimensionRequirement(List<ResourceLocation> filter, boolean blacklist) {
        super(RequirementIOMode.INPUT);
        this.filter = filter;
        this.blacklist = blacklist;
    }

    @Override
    public RequirementType<DimensionRequirement> getType() {
        return Registration.DIMENSION_REQUIREMENT.get();
    }

    @Override
    public boolean test(PositionMachineComponent component, ICraftingContext context) {
        return this.filter.contains(component.getDimension().location()) != this.blacklist;
    }

    @Override
    public CraftingResult processStart(PositionMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(PositionMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<PositionMachineComponent> getComponentType() {
        return Registration.POSITION_MACHINE_COMPONENT.get();
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        if(!this.filter.isEmpty()) {
            if(this.blacklist)
                info.addTooltip(Component.translatable("custommachinery.requirements.position.info.dimension.blacklist").withStyle(ChatFormatting.DARK_RED));
            else
                info.addTooltip(Component.translatable("custommachinery.requirements.position.info.dimension.whitelist").withStyle(ChatFormatting.DARK_GREEN));
            this.filter.forEach(dimension -> info.addTooltip(Component.literal("* " + dimension)));
        }
        info.setSpriteIcon(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("block/nether_portal")));
    }
}
