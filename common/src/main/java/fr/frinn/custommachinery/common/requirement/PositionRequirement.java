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
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.impl.util.IntRange;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class PositionRequirement extends AbstractRequirement<PositionMachineComponent> implements IDisplayInfoRequirement {

    public static final Codec<PositionRequirement> CODEC = RecordCodecBuilder.create(positionRequirementInstance ->
        positionRequirementInstance.group(
                CodecLogger.loggedOptional(IntRange.CODEC,"x", IntRange.ALL).forGetter(requirement -> requirement.x),
                CodecLogger.loggedOptional(IntRange.CODEC,"y", IntRange.ALL).forGetter(requirement -> requirement.y),
                CodecLogger.loggedOptional(IntRange.CODEC,"z", IntRange.ALL).forGetter(requirement -> requirement.z)
        ).apply(positionRequirementInstance, PositionRequirement::new)
    );

    private final IntRange x;
    private final IntRange y;
    private final IntRange z;

    public PositionRequirement(IntRange x, IntRange y, IntRange z) {
        super(RequirementIOMode.INPUT);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public RequirementType<PositionRequirement> getType() {
        return Registration.POSITION_REQUIREMENT.get();
    }

    @Override
    public boolean test(PositionMachineComponent component, ICraftingContext context) {
        BlockPos pos = component.getPosition();
        return this.x.contains(pos.getX()) && this.y.contains(pos.getY()) && this.z.contains(pos.getZ());
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
        info.addTooltip(new TranslatableComponent("custommachinery.requirements.position.info.pos").withStyle(ChatFormatting.AQUA));
        info.addTooltip(new TextComponent("X: ").append(new TextComponent(this.x.toFormattedString())));
        info.addTooltip(new TextComponent("Y: ").append(new TextComponent(this.y.toFormattedString())));
        info.addTooltip(new TextComponent("Z: ").append(new TextComponent(this.z.toFormattedString())));
        info.setItemIcon(Items.COMPASS);
    }
}
