package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.common.component.BlockMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.BlockIngredient;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public record BlockRequirement(RequirementIOMode mode, Action action, AABB pos, int amount, ComparatorMode comparator, PartialBlockState block, List<IIngredient<PartialBlockState>> filter, boolean whitelist) implements IRequirement<BlockMachineComponent> {

    public static final NamedCodec<BlockRequirement> CODEC = NamedCodec.record(blockRequirementInstance ->
            blockRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(BlockRequirement::getMode),
                    Action.CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    DefaultCodecs.BOX.fieldOf("pos").forGetter(requirement -> requirement.pos),
                    NamedCodec.INT.optionalFieldOf("amount", 1).forGetter(requirement -> requirement.amount),
                    ComparatorMode.CODEC.optionalFieldOf("comparator", ComparatorMode.GREATER_OR_EQUALS).forGetter(requirement -> requirement.comparator),
                    PartialBlockState.CODEC.optionalFieldOf("block", PartialBlockState.AIR).forGetter(requirement -> requirement.block),
                    IIngredient.BLOCK.listOf().optionalFieldOf("filter", Collections.emptyList()).forGetter(requirement -> requirement.filter),
                    NamedCodec.BOOL.optionalFieldOf("whitelist", false).forGetter(requirement -> requirement.whitelist)
            ).apply(blockRequirementInstance, BlockRequirement::new), "Block requirement"
    );

    @Override
    public RequirementType<?> getType() {
        return Registration.BLOCK_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<BlockMachineComponent> getComponentType() {
        return Registration.BLOCK_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return this.mode;
    }

    @Override
    public boolean test(BlockMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        return switch (this.action) {
            case CHECK ->
                    this.comparator.compare((int) component.getBlockAmount(this.pos, this.filter, this.whitelist), amount);
            case PLACE -> (int) component.getBlockAmount(this.pos, Collections.singletonList(BlockIngredient.AIR), true) >= amount;
            case BREAK, DESTROY, REPLACE_BREAK, REPLACE_DESTROY -> (int) component.getBlockAmount(this.pos, this.filter, this.whitelist) >= amount;
        };
    }

    @Override
    public void gatherRequirements(IRequirementList<BlockMachineComponent> list) {
        if(this.action == Action.CHECK)
            list.worldCondition(this::check);
        else
            list.process(this.mode, this::process);
    }

    private CraftingResult check(BlockMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        long found = component.getBlockAmount(this.pos, this.filter, this.whitelist);
        if(!this.comparator.compare((int)found, amount))
            return CraftingResult.error(Component.translatable("custommachinery.requirements.block.check.error", amount, this.pos.toString(), found));
        return CraftingResult.success();
    }

    private CraftingResult process(BlockMachineComponent component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        switch (this.action) {
            case PLACE -> {
                if (component.placeBlock(this.pos, this.block, amount))
                    return CraftingResult.success();
                return CraftingResult.error(Component.translatable("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
            }
            case REPLACE_BREAK -> {
                if (component.replaceBlock(this.pos, this.block, amount, true, this.filter, this.whitelist))
                    return CraftingResult.success();
                return CraftingResult.error(Component.translatable("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
            }
            case REPLACE_DESTROY -> {
                if (component.replaceBlock(this.pos, this.block, amount, false, this.filter, this.whitelist))
                    return CraftingResult.success();
                return CraftingResult.error(Component.translatable("custommachinery.requirements.block.place.error", amount, this.block.getName(), this.pos.toString()));
            }
            case BREAK -> {
                if (component.breakBlock(this.pos, this.filter, this.whitelist, amount, true))
                    return CraftingResult.success();
                return CraftingResult.error(Component.translatable("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
            }
            case DESTROY -> {
                if (component.breakBlock(this.pos, this.filter, this.whitelist, amount, false))
                    return CraftingResult.success();
                return CraftingResult.error(Component.translatable("custommachinery.requirements.block.break.error", amount, this.pos.toString()));
            }
        }
        return CraftingResult.pass();
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        MutableComponent action = null;
        switch (this.action) {
            case CHECK -> action = Component.translatable("custommachinery.requirements.block.check.info");
            case BREAK -> {
                if (this.getMode() == RequirementIOMode.INPUT)
                    action = Component.translatable("custommachinery.requirements.block.break.info.input");
                else
                    action = Component.translatable("custommachinery.requirements.block.break.info.output");
            }
            case DESTROY -> {
                if (this.getMode() == RequirementIOMode.INPUT)
                    action = Component.translatable("custommachinery.requirements.block.destroy.info.input");
                else
                    action = Component.translatable("custommachinery.requirements.block.destroy.info.output");
            }
            case PLACE -> {
                if (this.getMode() == RequirementIOMode.INPUT)
                    action = Component.translatable("custommachinery.requirements.block.place.info.input", this.amount, this.block.getName());
                else
                    action = Component.translatable("custommachinery.requirements.block.place.info.output", this.amount, this.block.getName());
            }
            case REPLACE_BREAK, REPLACE_DESTROY -> {
                if (this.getMode() == RequirementIOMode.INPUT)
                    action = Component.translatable("custommachinery.requirements.block.replace.info.input", this.amount, this.block.getName());
                else
                    action = Component.translatable("custommachinery.requirements.block.replace.info.output", this.amount, this.block.getName());
            }
        }
        if(action != null)
            info.addTooltip(action.withStyle(ChatFormatting.AQUA));
        if(this.action != Action.PLACE) {
            if(this.action != Action.CHECK)
                info.addTooltip(Component.translatable("custommachinery.requirements.block." + (this.whitelist ? "allowed" : "denied")).withStyle(this.whitelist ? ChatFormatting.GREEN : ChatFormatting.RED));
            if(this.whitelist && this.filter.isEmpty())
                info.addTooltip(Component.literal("-").append(Component.translatable("custommachinery.requirements.block.none")));
            else if(!this.whitelist && this.filter.isEmpty())
                info.addTooltip(Component.literal("-").append(Component.translatable("custommachinery.requirements.block.all")));
            else
                this.filter.forEach(block -> info.addTooltip(Component.literal("- ").append(Utils.getBlockName(block))));
        }
        info.addTooltip(Component.translatable("custommachinery.requirements.block.info.box").withStyle(ChatFormatting.GOLD));
        info.setClickAction((machine, recipe, mouseButton) -> CustomMachineRenderer.addRenderBox(machine.getId(), this.pos));
        info.setItemIcon(Items.GRASS_BLOCK);
    }

    public enum Action {
        CHECK,
        BREAK,
        DESTROY,
        PLACE,
        REPLACE_BREAK,
        REPLACE_DESTROY;

        public static final NamedCodec<Action> CODEC = NamedCodec.enumCodec(Action.class);

        public static Action value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }
    }
}
