package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.BlockMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Locale;
import java.util.function.Consumer;

public class BlockRequirement extends AbstractTickableRequirement<BlockMachineComponent> implements IDelayedRequirement<BlockMachineComponent>, IDisplayInfoRequirement<BlockMachineComponent> {

    public static final Codec<BlockRequirement> CODEC = RecordCodecBuilder.create(blockRequirementInstance ->
            blockRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractTickableRequirement::getMode),
                    Codecs.BLOCK_REQUIREMENT_ACTION_CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    Codecs.BOX_CODEC.fieldOf("pos").forGetter(requirement -> requirement.pos),
                    Codec.INT.optionalFieldOf("amount", 1).forGetter(requirement -> requirement.amount),
                    Codecs.COMPARATOR_MODE_CODEC.optionalFieldOf("comparator", ComparatorMode.GREATER_OR_EQUALS).forGetter(requirement -> requirement.comparator),
                    Codecs.PARTIAL_BLOCK_STATE_CODEC.fieldOf("block").orElse((Consumer<String>) CustomMachinery.LOGGER::error, PartialBlockState.AIR).forGetter(requirement -> requirement.block),
                    Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("delay", 0.0D).forGetter(requirement -> requirement.delay),
                    Codec.BOOL.optionalFieldOf("jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(blockRequirementInstance, (mode, action, pos, amount, comparator, block, delay, jei) -> {
                    BlockRequirement requirement = new BlockRequirement(mode, action, pos, amount, comparator, block);
                    requirement.setJeiVisible(jei);
                    requirement.setDelay(delay);
                    return requirement;
            })
    );

    private ACTION action;
    private AxisAlignedBB pos;
    private int amount;
    private ComparatorMode comparator;
    private PartialBlockState block;
    private double delay;
    private boolean jeiVisible;

    public BlockRequirement(MODE mode, ACTION action, AxisAlignedBB pos, int amount, ComparatorMode comparator, PartialBlockState block) {
        super(mode);
        this.action = action;
        this.pos = pos;
        this.amount = amount;
        this.comparator = comparator;
        this.block = block;
    }

    @Override
    public RequirementType<?> getType() {
        return Registration.BLOCK_REQUIREMENT.get();
    }

    @Override
    public boolean test(BlockMachineComponent component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        switch (this.action) {
            case CHECK:
                return this.comparator.compare((int)component.getBlockAmount(this.pos, this.block), amount);
            case BREAK:
            case DESTROY:
                return this.delay != 0 || (int)component.getBlockAmount(this.pos, this.block) >= amount;
            case PLACE:
                return  this.delay != 0 || (int)component.getBlockAmount(this.pos, PartialBlockState.AIR) >= amount;
            default:
                return true;
        }
    }

    @Override
    public CraftingResult processStart(BlockMachineComponent component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(this.getMode() == MODE.INPUT && this.delay == 0) {
            switch (this.action) {
                case PLACE:
                    if(component.placeBlock(this.pos, this.block, amount))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
                case REPLACE_BREAK:
                    if(component.replaceBlock(this.pos, this.block, amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
                case REPLACE_DESTROY:
                    if(component.replaceBlock(this.pos, this.block, amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
                case BREAK:
                    if(component.breakBlock(this.pos, this.block, amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
                case DESTROY:
                    if(component.breakBlock(this.pos, this.block, amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(BlockMachineComponent component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(this.getMode() == MODE.OUTPUT && this.delay == 0) {
            switch (this.action) {
                case PLACE:
                    if(component.placeBlock(this.pos, this.block, amount))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
                case REPLACE_BREAK:
                    if(component.replaceBlock(this.pos, this.block, amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
                case REPLACE_DESTROY:
                    if(component.replaceBlock(this.pos, this.block, amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
                case BREAK:
                    if(component.breakBlock(this.pos, this.block, amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
                case DESTROY:
                    if(component.breakBlock(this.pos, this.block, amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<BlockMachineComponent> getComponentType() {
        return Registration.BLOCK_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(BlockMachineComponent component, CraftingContext context) {
        int amount = (int)context.getPerTickModifiedValue(this.amount, this, null);
        if(this.action == ACTION.CHECK) {
            long found = component.getBlockAmount(this.pos, this.block);
            if(!this.comparator.compare((int)found, amount))
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.check.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString(), found));
            return CraftingResult.success();
        }
        return CraftingResult.pass();
    }

    @Override
    public void setDelay(double delay) {
        this.delay = MathHelper.clamp(delay, 0.0, 1.0);
    }

    @Override
    public double getDelay() {
        return this.delay;
    }

    @Override
    public CraftingResult execute(BlockMachineComponent component, CraftingContext context) {
        switch (this.action) {
            case PLACE:
                if(component.placeBlock(this.pos, this.block, amount))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
            case REPLACE_BREAK:
                if(component.replaceBlock(this.pos, this.block, amount, true))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
            case REPLACE_DESTROY:
                if(component.replaceBlock(this.pos, this.block, amount, false))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
            case BREAK:
                if(component.breakBlock(this.pos, this.block, amount, true))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
            case DESTROY:
                if(component.breakBlock(this.pos, this.block, amount, false))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey()), this.pos.toString()));
        }
        return CraftingResult.pass();
    }

    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        RequirementDisplayInfo info = new RequirementDisplayInfo();
        switch (this.action) {
            case CHECK:
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.check.info"));
                break;
            case BREAK:
                if(this.getMode() == MODE.INPUT)
                    info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.break.info.input"));
                else
                    info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.break.info.output"));
                break;
            case DESTROY:
                if(this.getMode() == MODE.INPUT)
                    info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.destroy.info.input"));
                else
                    info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.destroy.info.output"));
                break;
            case PLACE:
                if(this.getMode() == MODE.INPUT)
                    info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.place.info.input"));
                else
                    info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.place.info.output"));
                break;
            case REPLACE_BREAK:
            case REPLACE_DESTROY:
                if(this.getMode() == MODE.INPUT)
                    info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.replace.info.input"));
                else
                    info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.replace.info.output"));
                break;
        }
        info.addTooltip(new StringTextComponent(this.amount + "x ").appendSibling(new TranslationTextComponent(this.block.getBlockState().getBlock().getTranslationKey())));
        this.block.getProperties().forEach(property -> info.addTooltip(new StringTextComponent("* " + property)));
        info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.info.box"));
        info.setClickAction((machine, mouseButton) -> CustomMachineRenderer.addRenderBox(machine.getId(), this.pos));
        info.setVisible(this.jeiVisible);
        return info;
    }

    public enum ACTION {
        CHECK,
        BREAK,
        DESTROY,
        PLACE,
        REPLACE_BREAK,
        REPLACE_DESTROY;

        public static ACTION value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }
    }
}
