package fr.frinn.custommachinery.common.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.RedstoneMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.impl.util.IntRange;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class RedstoneRequirement extends AbstractRequirement<RedstoneMachineComponent> implements ITickableRequirement<RedstoneMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<RedstoneRequirement> CODEC = RecordCodecBuilder.create(redstoneRequirementInstance ->
            redstoneRequirementInstance.group(
                    IntRange.CODEC.fieldOf("power").forGetter(requirement -> requirement.powerLevel)
            ).apply(redstoneRequirementInstance, RedstoneRequirement::new)
    );

    private final IntRange powerLevel;

    public RedstoneRequirement(IntRange powerLevel) {
        super(RequirementIOMode.INPUT);
        this.powerLevel = powerLevel;
    }

    @Override
    public RequirementType<RedstoneRequirement> getType() {
        return Registration.REDSTONE_REQUIREMENT.get();
    }

    @Override
    public boolean test(RedstoneMachineComponent component, ICraftingContext context) {
        return this.powerLevel.contains(component.getMachinePower());
    }

    @Override
    public CraftingResult processStart(RedstoneMachineComponent component, ICraftingContext context) {
        if(this.test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.redstone.error", this.powerLevel.toFormattedString()));
    }

    @Override
    public CraftingResult processEnd(RedstoneMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<RedstoneMachineComponent> getComponentType() {
        return Registration.REDSTONE_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(RedstoneMachineComponent component, ICraftingContext context) {
        if(this.test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.redstone.error", this.powerLevel.toFormattedString()));
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.addTooltip(new TranslatableComponent("custommachinery.requirements.redstone.info", this.powerLevel.toFormattedString()))
                .setItemIcon(Items.REDSTONE);
    }
}
