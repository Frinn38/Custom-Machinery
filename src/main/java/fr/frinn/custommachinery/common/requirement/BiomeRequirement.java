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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public record BiomeRequirement(List<ResourceLocation> filter, boolean blacklist) implements IRequirement<PositionMachineComponent> {

    public static final NamedCodec<BiomeRequirement> CODEC = NamedCodec.record(biomeRequirementInstance ->
            biomeRequirementInstance.group(
                    DefaultCodecs.RESOURCE_LOCATION.listOf().fieldOf("filter").forGetter(requirement -> requirement.filter),
                    NamedCodec.BOOL.optionalFieldOf("blacklist", false).forGetter(requirement -> requirement.blacklist)
            ).apply(biomeRequirementInstance, BiomeRequirement::new), "Biome requirement"
    );

    @Override
    public RequirementType<BiomeRequirement> getType() {
        return Registration.BIOME_REQUIREMENT.get();
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
        Registry<Biome> biomeRegistry = component.getManager().getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        return this.filter.stream().anyMatch(biome -> biomeRegistry.get(biome) == component.getBiome()) != this.blacklist;
    }

    @Override
    public void gatherRequirements(IRequirementList<PositionMachineComponent> list) {
        //The machine won't move, and we can assume the biome won't change (at least not during a recipe) so not checking per tick, only at the beginning.
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        if(!this.filter.isEmpty()) {
            if(this.blacklist)
                info.addTooltip(Component.translatable("custommachinery.requirements.position.info.biome.blacklist").withStyle(ChatFormatting.AQUA));
            else
                info.addTooltip(Component.translatable("custommachinery.requirements.position.info.biome.whitelist").withStyle(ChatFormatting.AQUA));
            this.filter.forEach(biome -> info.addTooltip(Component.literal("* ").append(Component.translatable("biome." + biome.getNamespace() + "." + biome.getPath()))));
        }
        info.setItemIcon(Items.MAP);
    }
}
