package fr.frinn.custommachinery.common.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.PositionMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public class BiomeRequirement extends AbstractRequirement<PositionMachineComponent> implements IDisplayInfoRequirement {

    public static final Codec<BiomeRequirement> CODEC = RecordCodecBuilder.create(biomeRequirementInstance ->
            biomeRequirementInstance.group(
                    Codecs.list(ResourceLocation.CODEC).fieldOf("filter").forGetter(requirement -> requirement.filter),
                    CodecLogger.loggedOptional(Codec.BOOL, "blacklist", false).forGetter(requirement -> requirement.blacklist)
            ).apply(biomeRequirementInstance, BiomeRequirement::new)
    );

    private final List<ResourceLocation> filter;
    private final boolean blacklist;

    public BiomeRequirement(List<ResourceLocation> filter, boolean blacklist) {
        super(RequirementIOMode.INPUT);
        this.filter = filter;
        this.blacklist = blacklist;
    }

    @Override
    public RequirementType<BiomeRequirement> getType() {
        return Registration.BIOME_REQUIREMENT.get();
    }

    @Override
    public boolean test(PositionMachineComponent component, ICraftingContext context) {
        Registry<Biome> biomeRegistry = component.getManager().getLevel().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        return this.filter.stream().anyMatch(biome -> biomeRegistry.get(biome) == component.getBiome()) != this.blacklist;
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
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.position.info.biome.blacklist").withStyle(ChatFormatting.AQUA));
            else
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.position.info.biome.whitelist").withStyle(ChatFormatting.AQUA));
            this.filter.forEach(biome -> info.addTooltip(new TextComponent("* ").append(new TranslatableComponent("biome." + biome.getNamespace() + "." + biome.getPath()))));
        }
        info.setItemIcon(Items.MAP);
    }
}
