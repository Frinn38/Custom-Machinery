package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.PositionMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.PositionComparator;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PositionRequirement extends AbstractRequirement<PositionMachineComponent> implements IDisplayInfoRequirement<PositionMachineComponent> {

    public static final Codec<PositionRequirement> CODEC = RecordCodecBuilder.create(positionRequirementInstance ->
        positionRequirementInstance.group(
                Codecs.POSITION_COMPARATOR_CODEC.listOf().optionalFieldOf("positions", new ArrayList<>()).forGetter(requirement -> requirement.positions),
                ResourceLocation.CODEC.listOf().optionalFieldOf("biomes", new ArrayList<>()).forGetter(requirement -> requirement.biomes),
                Codec.BOOL.optionalFieldOf("biomesBlacklist", false).forGetter(requirement -> requirement.biomesBlacklist),
                ResourceLocation.CODEC.listOf().optionalFieldOf("dimensions", new ArrayList<>()).forGetter(requirement -> requirement.dimensions.stream().map(RegistryKey::getLocation).collect(Collectors.toList())),
                Codec.BOOL.optionalFieldOf("dimensionsBlacklist", false).forGetter(requirement -> requirement.dimensionsBlacklist)
        ).apply(positionRequirementInstance, PositionRequirement::new)
    );

    private List<PositionComparator> positions;
    private List<ResourceLocation> biomes;
    private boolean biomesBlacklist;
    private List<RegistryKey<World>> dimensions;
    private boolean dimensionsBlacklist;

    public PositionRequirement(List<PositionComparator> positions, List<ResourceLocation> biomes, boolean biomesBlacklist, List<ResourceLocation> dimensions, boolean dimensionsBlacklist) {
        super(MODE.INPUT);
        this.positions = positions;
        this.biomes = biomes;
        this.biomesBlacklist = biomesBlacklist;
        this.dimensions = dimensions.stream().map(location -> RegistryKey.getOrCreateKey(Registry.WORLD_KEY, location)).collect(Collectors.toList());
        this.dimensionsBlacklist = dimensionsBlacklist;
    }

    @Override
    public RequirementType<PositionRequirement> getType() {
        return Registration.POSITION_REQUIREMENT.get();
    }

    @Override
    public boolean test(PositionMachineComponent component, CraftingContext context) {
        boolean positionCheck = this.positions.isEmpty() || this.positions.stream().allMatch(comparator -> comparator.compare(component.getPosition()));
        boolean biomeCheck = (this.biomes.isEmpty() || this.biomesBlacklist) != this.biomes.contains(component.getBiome().getRegistryName());
        boolean dimensionCheck = this.dimensions.isEmpty() || this.dimensionsBlacklist != this.dimensions.contains(component.getDimension());
        return positionCheck && biomeCheck && dimensionCheck;
    }

    @Override
    public CraftingResult processStart(PositionMachineComponent component, CraftingContext context) {
        if(test(component, context))
            return CraftingResult.success();
        else
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.position.error"));
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
    public RequirementDisplayInfo getDisplayInfo() {
        RequirementDisplayInfo info =  new RequirementDisplayInfo();
        if(!this.positions.isEmpty()) {
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.position.info.pos").mergeStyle(TextFormatting.AQUA));
            this.positions.forEach(pos -> info.addTooltip(new StringTextComponent("* ").appendSibling(pos.getText())));
        }
        if(!this.biomes.isEmpty()) {
            if(this.biomesBlacklist)
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.position.info.biome.blacklist").mergeStyle(TextFormatting.AQUA));
            else
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.position.info.biome.whitelist").mergeStyle(TextFormatting.AQUA));
            this.biomes.forEach(biome -> info.addTooltip(new StringTextComponent("* ").appendSibling(new TranslationTextComponent("biome." + biome.getNamespace() + "." + biome.getPath()))));
        }
        if(!this.dimensions.isEmpty()) {
            if(this.dimensionsBlacklist)
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.position.info.dimension.blacklist").mergeStyle(TextFormatting.AQUA));
            else
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.position.info.dimension.whitelist").mergeStyle(TextFormatting.AQUA));
            this.biomes.forEach(dimension -> info.addTooltip(new StringTextComponent("* " + dimension)));
        }
        return info;
    }
}
