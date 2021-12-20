package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.PositionMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class DimensionRequirement extends AbstractRequirement<PositionMachineComponent> implements IDisplayInfoRequirement {

    public static final Codec<DimensionRequirement> CODEC = RecordCodecBuilder.create(dimensionRequirementInstance ->
            dimensionRequirementInstance.group(
                    Codecs.list(ResourceLocation.CODEC).fieldOf("filter").forGetter(requirement -> requirement.filter),
                    CodecLogger.loggedOptional(Codec.BOOL, "blacklist", false).forGetter(requirement -> requirement.blacklist),
                    CodecLogger.loggedOptional(Codec.BOOL, "jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(dimensionRequirementInstance, (filter, blacklist, jei) -> {
                DimensionRequirement requirement = new DimensionRequirement(filter, blacklist);
                requirement.setJeiVisible(jei);
                return requirement;
            })
    );

    private final List<ResourceLocation> filter;
    private final boolean blacklist;
    private boolean jeiVisible = true;

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
        return this.filter.contains(component.getDimension().getLocation()) != this.blacklist;
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
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        if(!this.filter.isEmpty()) {
            if(this.blacklist)
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.position.info.dimension.blacklist").mergeStyle(TextFormatting.AQUA));
            else
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.position.info.dimension.whitelist").mergeStyle(TextFormatting.AQUA));
            this.filter.forEach(dimension -> info.addTooltip(new StringTextComponent("* " + dimension)));
        }
        info.setVisible(this.jeiVisible);
    }
}
