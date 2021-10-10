package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.PositionMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class DimensionRequirement extends AbstractRequirement<PositionMachineComponent> implements IDisplayInfoRequirement<PositionMachineComponent> {

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
        super(MODE.INPUT);
        this.filter = filter;
        this.blacklist = blacklist;
    }

    @Override
    public RequirementType<DimensionRequirement> getType() {
        return Registration.DIMENSION_REQUIREMENT.get();
    }

    @Override
    public boolean test(PositionMachineComponent component, CraftingContext context) {
        return this.filter.contains(component.getDimension().getLocation()) != this.blacklist;
    }

    @Override
    public CraftingResult processStart(PositionMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(PositionMachineComponent component, CraftingContext context) {
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
    public RequirementDisplayInfo getDisplayInfo() {
        RequirementDisplayInfo info = new RequirementDisplayInfo();
        if(!this.filter.isEmpty()) {
            if(this.blacklist)
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.position.info.dimension.blacklist").mergeStyle(TextFormatting.AQUA));
            else
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.position.info.dimension.whitelist").mergeStyle(TextFormatting.AQUA));
            this.filter.forEach(dimension -> info.addTooltip(new StringTextComponent("* " + dimension)));
        }
        info.setVisible(this.jeiVisible);
        return info;
    }
}
