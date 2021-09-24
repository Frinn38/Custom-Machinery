package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
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
import fr.frinn.custommachinery.common.util.ingredient.BlockIngredient;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BlockRequirement extends AbstractTickableRequirement<BlockMachineComponent> implements IDelayedRequirement<BlockMachineComponent>, IDisplayInfoRequirement<BlockMachineComponent> {

    public static final Codec<BlockRequirement> CODEC = RecordCodecBuilder.create(blockRequirementInstance ->
            blockRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractTickableRequirement::getMode),
                    Codecs.BLOCK_REQUIREMENT_ACTION_CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    Codecs.BOX_CODEC.fieldOf("pos").forGetter(requirement -> requirement.pos),
                    CodecLogger.loggedOptional(Codec.INT,"amount", 1).forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codecs.COMPARATOR_MODE_CODEC,"comparator", ComparatorMode.GREATER_OR_EQUALS).forGetter(requirement -> requirement.comparator),
                    CodecLogger.loggedOptional(Codecs.PARTIAL_BLOCK_STATE_CODEC, "block", PartialBlockState.AIR).forGetter(requirement -> requirement.block),
                    CodecLogger.loggedOptional(Codecs.list(IIngredient.BLOCK), "filter", Collections.emptyList()).forGetter(requirement -> requirement.filter),
                    CodecLogger.loggedOptional(Codec.BOOL, "whitelist", false).forGetter(requirement -> requirement.whitelist),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0D, 1.0D), "delay", 0.0D).forGetter(requirement -> requirement.delay),
                    CodecLogger.loggedOptional(Codec.BOOL, "jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(blockRequirementInstance, (mode, action, pos, amount, comparator, block, filter, whitelist, delay, jei) -> {
                    BlockRequirement requirement = new BlockRequirement(mode, action, pos, amount, comparator, block, filter, whitelist);
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
    private List<IIngredient<PartialBlockState>> filter;
    private boolean whitelist;
    private double delay;
    private boolean jeiVisible = true;

    public BlockRequirement(MODE mode, ACTION action, AxisAlignedBB pos, int amount, ComparatorMode comparator, PartialBlockState block, List<IIngredient<PartialBlockState>> filter, boolean whitelist) {
        super(mode);
        this.action = action;
        this.pos = pos;
        this.amount = amount;
        this.comparator = comparator;
        this.block = block;
        this.filter = filter;
        this.whitelist = whitelist;
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
                return this.comparator.compare((int)component.getBlockAmount(this.pos, this.filter, this.whitelist), amount);
            case PLACE:
                return  this.delay != 0 || (int)component.getBlockAmount(this.pos, Collections.singletonList(BlockIngredient.AIR), true) >= amount;
            case BREAK:
            case DESTROY:
            case REPLACE_BREAK:
            case REPLACE_DESTROY:
                return this.delay != 0 || (int)component.getBlockAmount(this.pos, this.filter, this.whitelist) >= amount;
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
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case REPLACE_BREAK:
                    if(component.replaceBlock(this.pos, this.block, amount, true, this.filter, this.whitelist))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case REPLACE_DESTROY:
                    if(component.replaceBlock(this.pos, this.block, amount, false, this.filter, this.whitelist))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case BREAK:
                    if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
                case DESTROY:
                    if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
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
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case REPLACE_BREAK:
                    if(component.replaceBlock(this.pos, this.block, amount, true, this.filter, this.whitelist))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case REPLACE_DESTROY:
                    if(component.replaceBlock(this.pos, this.block, amount, false, this.filter, this.whitelist))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
                case BREAK:
                    if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
                case DESTROY:
                    if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
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
            long found = component.getBlockAmount(this.pos, this.filter, this.whitelist);
            if(!this.comparator.compare((int)found, amount))
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.check.error", amount, this.pos.toString(), found));
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
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
            case REPLACE_BREAK:
                if(component.replaceBlock(this.pos, this.block, amount, true, this.filter, this.whitelist))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
            case REPLACE_DESTROY:
                if(component.replaceBlock(this.pos, this.block, amount, false, this.filter, this.whitelist))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
            case BREAK:
                if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, true))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
            case DESTROY:
                if(component.breakBlock(this.pos, this.filter, this.whitelist, amount, false))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
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
        IFormattableTextComponent action = null;
        switch (this.action) {
            case CHECK:
                action = new TranslationTextComponent("custommachinery.requirements.block.check.info");
                break;
            case BREAK:
                if(this.getMode() == MODE.INPUT)
                    action = new TranslationTextComponent("custommachinery.requirements.block.break.info.input");
                else
                    action = new TranslationTextComponent("custommachinery.requirements.block.break.info.output");
                break;
            case DESTROY:
                if(this.getMode() == MODE.INPUT)
                    action = new TranslationTextComponent("custommachinery.requirements.block.destroy.info.input");
                else
                    action = new TranslationTextComponent("custommachinery.requirements.block.destroy.info.output");
                break;
            case PLACE:
                if(this.getMode() == MODE.INPUT)
                    action = new TranslationTextComponent("custommachinery.requirements.block.place.info.input", this.amount, this.block.getName());
                else
                    action = new TranslationTextComponent("custommachinery.requirements.block.place.info.output", this.amount, this.block.getName());
                break;
            case REPLACE_BREAK:
            case REPLACE_DESTROY:
                if(this.getMode() == MODE.INPUT)
                    action = new TranslationTextComponent("custommachinery.requirements.block.replace.info.input", this.amount, this.block.getName());
                else
                    action = new TranslationTextComponent("custommachinery.requirements.block.replace.info.output", this.amount, this.block.getName());
                break;
        }
        if(action != null)
            info.addTooltip(action.mergeStyle(TextFormatting.AQUA));
        if(this.action != ACTION.PLACE) {
            if(this.action != ACTION.CHECK)
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block." + (this.whitelist ? "allowed" : "denied")).mergeStyle(this.whitelist ? TextFormatting.GREEN : TextFormatting.RED));
            if(this.whitelist && this.filter.isEmpty())
                info.addTooltip(new StringTextComponent("-").appendSibling(new TranslationTextComponent("custommachinery.requirements.block.none")));
            else if(!this.whitelist && this.filter.isEmpty())
                info.addTooltip(new StringTextComponent("-").appendSibling(new TranslationTextComponent("custommachinery.requirements.block.all")));
            else
                this.filter.forEach(block -> info.addTooltip(new StringTextComponent("- " + block.toString())));
        }
        info.addTooltip(new TranslationTextComponent("custommachinery.requirements.block.info.box").mergeStyle(TextFormatting.GOLD));
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
