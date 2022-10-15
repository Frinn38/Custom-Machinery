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
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.List;

public class DimensionRequirement extends AbstractRequirement<PositionMachineComponent> implements IDisplayInfoRequirement {

    public static final Codec<DimensionRequirement> CODEC = RecordCodecBuilder.create(dimensionRequirementInstance ->
            dimensionRequirementInstance.group(
                    Codecs.list(ResourceLocation.CODEC).fieldOf("filter").forGetter(requirement -> requirement.filter),
                    CodecLogger.loggedOptional(Codec.BOOL, "blacklist", false).forGetter(requirement -> requirement.blacklist)
            ).apply(dimensionRequirementInstance, DimensionRequirement::new)
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
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.position.info.dimension.blacklist").withStyle(ChatFormatting.DARK_RED));
            else
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.position.info.dimension.whitelist").withStyle(ChatFormatting.DARK_GREEN));
            this.filter.forEach(dimension -> info.addTooltip(new TextComponent("* " + dimension)));
        }
        info.setSpriteIcon(Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("block/nether_portal")));
    }
}
