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
import fr.frinn.custommachinery.common.util.PositionComparator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.List;

public class PositionRequirement extends AbstractRequirement<PositionMachineComponent> implements IDisplayInfoRequirement {

    public static final Codec<PositionRequirement> CODEC = RecordCodecBuilder.create(positionRequirementInstance ->
        positionRequirementInstance.group(
                CodecLogger.loggedOptional(Codecs.list(Codecs.POSITION_COMPARATOR_CODEC),"positions", Collections.emptyList()).forGetter(requirement -> requirement.positions)
        ).apply(positionRequirementInstance, PositionRequirement::new)
    );

    private final List<PositionComparator> positions;

    public PositionRequirement(List<PositionComparator> positions) {
        super(RequirementIOMode.INPUT);
        this.positions = positions;
    }

    @Override
    public RequirementType<PositionRequirement> getType() {
        return Registration.POSITION_REQUIREMENT.get();
    }

    @Override
    public boolean test(PositionMachineComponent component, ICraftingContext context) {
        return this.positions.stream().allMatch(comparator -> comparator.compare(component.getPosition()));
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
        if(!this.positions.isEmpty()) {
            info.addTooltip(new TranslatableComponent("custommachinery.requirements.position.info.pos").withStyle(ChatFormatting.AQUA));
            this.positions.forEach(pos -> info.addTooltip(new TextComponent("* ").append(pos.getText())));
        }
        info.setItemIcon(Items.COMPASS);
    }
}
