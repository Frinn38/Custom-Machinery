package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.PositionMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.PositionComparator;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PositionRequirement extends AbstractRequirement<PositionMachineComponent> {

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
    public RequirementType getType() {
        return Registration.POSITION_REQUIREMENT.get();
    }

    @Override
    public boolean test(PositionMachineComponent component) {
        boolean positionCheck = this.positions.isEmpty() || this.positions.stream().allMatch(comparator -> comparator.compare(component.getPosition()));
        boolean biomeCheck = (this.biomes.isEmpty() || this.biomesBlacklist) != this.biomes.contains(component.getBiome().getRegistryName());
        boolean dimensionCheck = this.dimensions.isEmpty() || this.dimensionsBlacklist != this.dimensions.contains(component.getDimension());
        return positionCheck && biomeCheck && dimensionCheck;
    }

    @Override
    public CraftingResult processStart(PositionMachineComponent component) {
        if(test(component))
            return CraftingResult.success();
        else
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.position.error"));
    }

    @Override
    public CraftingResult processEnd(PositionMachineComponent component) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<PositionMachineComponent> getComponentType() {
        return Registration.POSITION_MACHINE_COMPONENT.get();
    }

    @Override
    public IIngredientType<?> getJEIIngredientType() {
        return null;
    }

    @Override
    public Object asJEIIngredient() {
        return null;
    }

    @Override
    public void addJeiIngredients(IIngredients ingredients) {

    }
}
