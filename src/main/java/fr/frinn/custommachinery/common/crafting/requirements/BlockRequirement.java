package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.BlockMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Locale;
import java.util.function.Consumer;

public class BlockRequirement extends AbstractTickableRequirement<BlockMachineComponent> {

    public static final Codec<BlockRequirement> CODEC = RecordCodecBuilder.create(blockRequirementInstance ->
            blockRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractTickableRequirement::getMode),
                    Codecs.BLOCK_REQUIREMENT_ACTION_CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    Codecs.BOX_CODEC.fieldOf("pos").forGetter(requirement -> requirement.pos),
                    Codec.INT.optionalFieldOf("amount", 1).forGetter(requirement -> requirement.amount),
                    Codecs.COMPARATOR_MODE_CODEC.optionalFieldOf("comparator", ComparatorMode.GREATER_OR_EQUALS).forGetter(requirement -> requirement.comparator),
                    Codecs.BLOCK_STATE_CODEC.fieldOf("block").orElse((Consumer<String>) CustomMachinery.LOGGER::error, PartialBlockState.AIR).forGetter(requirement -> requirement.block)
            ).apply(blockRequirementInstance, BlockRequirement::new)
    );

    private ACTION action;
    private AxisAlignedBB pos;
    private int amount;
    private ComparatorMode comparator;
    private PartialBlockState block;

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
    public boolean test(BlockMachineComponent component) {
        switch (this.action) {
            case CHECK:
                return this.comparator.compare((int)component.getBlockAmount(this.pos, this.block), this.amount);
            case BREAK:
            case DESTROY:
                return (int)component.getBlockAmount(this.pos, this.block) >= this.amount;
            case PLACE:
                return  (int)component.getBlockAmount(this.pos, PartialBlockState.AIR) >= this.amount;
            default:
                return true;
        }
    }

    @Override
    public CraftingResult processStart(BlockMachineComponent component) {
        if(this.getMode() == MODE.INPUT) {
            switch (this.action) {
                case PLACE:
                    if(component.placeBlock(this.pos, this.block, this.amount))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString()));
                case REPLACE_BREAK:
                    if(component.replaceBlock(this.pos, this.block, this.amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString()));
                case REPLACE_DESTROY:
                    if(component.replaceBlock(this.pos, this.block, this.amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString()));
                case BREAK:
                    if(component.breakBlock(this.pos, this.block, this.amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString()));
                case DESTROY:
                    if(component.breakBlock(this.pos, this.block, this.amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString()));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(BlockMachineComponent component) {
        if(this.getMode() == MODE.OUTPUT) {
            switch (this.action) {
                case PLACE:
                    if(component.placeBlock(this.pos, this.block, this.amount))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString()));
                case REPLACE_BREAK:
                    if(component.replaceBlock(this.pos, this.block, this.amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString()));
                case REPLACE_DESTROY:
                    if(component.replaceBlock(this.pos, this.block, this.amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.place.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString()));
                case BREAK:
                    if(component.breakBlock(this.pos, this.block, this.amount, true))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString()));
                case DESTROY:
                    if(component.breakBlock(this.pos, this.block, this.amount, false))
                        return CraftingResult.success();
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.break.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString()));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<BlockMachineComponent> getComponentType() {
        return Registration.BLOCK_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(BlockMachineComponent component) {
        if(this.action == ACTION.CHECK) {
            long amount = component.getBlockAmount(this.pos, this.block);
            if(!this.comparator.compare((int)amount, this.amount))
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.block.check.error", this.amount, new TranslationTextComponent(this.block.getBlock().getTranslationKey()), this.pos.toString(), amount));
        }
        return CraftingResult.success();
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
