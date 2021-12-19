package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.api.utils.RegistryCodec;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.DropMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class DropRequirement extends AbstractTickableRequirement<DropMachineComponent> implements IDelayedRequirement<DropMachineComponent>, IDisplayInfoRequirement<DropMachineComponent> {

    public static final Codec<DropRequirement> CODEC = RecordCodecBuilder.create(dropRequirementInstance ->
            dropRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractTickableRequirement::getMode),
                    Codecs.DROP_REQUIREMENT_ACTION_CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    CodecLogger.loggedOptional(Codecs.list(IIngredient.ITEM), "input", Collections.emptyList()).forGetter(requirement -> requirement.input),
                    CodecLogger.loggedOptional(Codec.BOOL, "whitelist", true).forGetter(requirement -> requirement.whitelist),
                    CodecLogger.loggedOptional(RegistryCodec.ITEM, "output", Items.AIR).forGetter(requirement -> requirement.output),
                    CodecLogger.loggedOptional(Codecs.COMPOUND_NBT_CODEC, "nbt").forGetter(requirement -> Optional.ofNullable(requirement.nbt)),
                    CodecLogger.loggedOptional(Codec.intRange(1, Integer.MAX_VALUE), "amount", 1).forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codec.intRange(1, Integer.MAX_VALUE), "radius", 1).forGetter(requirement -> requirement.radius),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0), "delay", 0.0).forGetter(IDelayedRequirement::getDelay)
            ).apply(dropRequirementInstance, (mode, action, input, whitelist, output, nbt, amount, radius, delay) -> {
                DropRequirement requirement = new DropRequirement(mode, action, input, whitelist, output, nbt.orElse(null), amount, radius);
                requirement.setDelay(delay);
                return requirement;
            })
    );

    private final Action action;
    private final List<IIngredient<Item>> input;
    private final boolean whitelist;
    private final Item output;
    @Nullable
    private final CompoundNBT nbt;
    private final int amount;
    private final int radius;
    private double delay;

    public DropRequirement(MODE mode, Action action, List<IIngredient<Item>> input, boolean whitelist, Item output, @Nullable CompoundNBT nbt, int amount, int radius) {
        super(mode);
        this.action = action;
        if(mode == MODE.INPUT && input.isEmpty())
            throw new IllegalArgumentException("Drop requirement in input mode MUST have at least one input item ingredient !");
        this.input = input;
        this.whitelist = whitelist;
        if(mode == MODE.OUTPUT && output == Items.AIR)
            throw new IllegalArgumentException("Drop requirement in output mode MUST have an output item specified !");
        this.output = output;
        this.nbt = nbt;
        this.amount = amount;
        this.radius = radius;
    }

    @Override
    public RequirementType<DropRequirement> getType() {
        return Registration.DROP_REQUIREMENT.get();
    }

    @Override
    public boolean test(DropMachineComponent component, CraftingContext context) {
        int amount = (int) context.getModifiedvalue(this.amount, this, null);
        if(this.action == Action.CHECK || this.action == Action.CONSUME)
            return component.getItemAmount(this.input, this.radius, this.whitelist) >= amount;
        return true;
    }

    @Override
    public CraftingResult processStart(DropMachineComponent component, CraftingContext context) {
        if(delay != 0.0 || getMode() != MODE.INPUT)
            return CraftingResult.pass();
        int amount = (int) context.getModifiedvalue(this.amount, this, null);
        double radius = context.getModifiedvalue(this.radius, this, "radius");
        switch (this.action) {
            case CONSUME:
                int found = component.getItemAmount(this.input, radius, this.whitelist);
                if(found >= amount) {
                    component.consumeItem(this.input, amount, radius, this.whitelist);
                    return CraftingResult.success();
                }
                else
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.drop.error.input", amount, found));
            case PRODUCE:
                ItemStack stack = Utils.makeItemStack(this.output, amount, null);
                if(component.produceItem(stack))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.drop.error.input", new StringTextComponent(amount + "x").appendSibling(new TranslationTextComponent(this.output.getTranslationKey(stack)))));
            default:
                return CraftingResult.pass();
        }
    }

    @Override
    public CraftingResult processEnd(DropMachineComponent component, CraftingContext context) {
        if(delay != 0.0 || getMode() != MODE.OUTPUT)
            return CraftingResult.pass();
        int amount = (int) context.getModifiedvalue(this.amount, this, null);
        double radius = context.getModifiedvalue(this.radius, this, "radius");
        switch (this.action) {
            case CONSUME:
                int found = component.getItemAmount(this.input, radius, this.whitelist);
                if(found > amount) {
                    component.consumeItem(this.input, amount, radius, this.whitelist);
                    return CraftingResult.success();
                }
                else
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.drop.error.input", amount, found));
            case PRODUCE:
                ItemStack stack = Utils.makeItemStack(this.output, amount, null);
                if(component.produceItem(stack))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.drop.error.input", new StringTextComponent(amount + "x").appendSibling(new TranslationTextComponent(this.output.getTranslationKey(stack)))));
            default:
                return CraftingResult.pass();
        }
    }

    @Override
    public MachineComponentType<DropMachineComponent> getComponentType() {
        return Registration.DROP_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(DropMachineComponent component, CraftingContext context) {
        if(this.action == Action.CHECK) {
            int amount = (int) context.getModifiedvalue(this.amount, this, null);
            double radius = context.getModifiedvalue(this.radius, this, "radius");
            int found = component.getItemAmount(this.input, radius, this.whitelist);
            if(found >= amount)
                return CraftingResult.success();
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.drop.error.input", amount, found));
        }
        return CraftingResult.pass();
    }

    /** DELAY **/

    @Override
    public void setDelay(double delay) {
        this.delay = delay;
    }

    @Override
    public double getDelay() {
        return this.delay;
    }

    @Override
    public CraftingResult execute(DropMachineComponent component, CraftingContext context) {
        int amount = (int) context.getModifiedvalue(this.amount, this, null);
        double radius = context.getModifiedvalue(this.radius, this, "radius");
        switch (this.action) {
            case CONSUME:
                int found = component.getItemAmount(this.input, radius, this.whitelist);
                if(found > amount) {
                    component.consumeItem(this.input, amount, radius, this.whitelist);
                    return CraftingResult.success();
                }
                else
                    return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.drop.error.input", amount, found));
            case PRODUCE:
                ItemStack stack = Utils.makeItemStack(this.output, amount, null);
                if(component.produceItem(stack))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.drop.error.input", new StringTextComponent(amount + "x").appendSibling(new TranslationTextComponent(this.output.getTranslationKey(stack)))));
            default:
                return CraftingResult.pass();
        }
    }


    /** DISPLAY **/

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        RequirementDisplayInfo info = new RequirementDisplayInfo();
        info.setVisible(this.jeiVisible);
        switch (this.action) {
            case CHECK:
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.drop.info.check", this.amount, this.radius));
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.drop.info." + (this.whitelist ? "whitelist" : "blacklist")).mergeStyle(this.whitelist ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED));
                this.input.forEach(ingredient -> info.addTooltip(new StringTextComponent(ingredient.toString())));
                break;
            case CONSUME:
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.drop.info.consume", this.amount, this.radius));
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.drop.info." + (this.whitelist ? "whitelist" : "blacklist")).mergeStyle(this.whitelist ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED));
                this.input.forEach(ingredient -> info.addTooltip(new StringTextComponent("- " + ingredient.toString())));
                break;
            case PRODUCE:
                info.addTooltip(new TranslationTextComponent("custommachinery.requirements.drop.info.produce", new StringTextComponent(this.amount + "x ").appendSibling(new TranslationTextComponent(this.output.getTranslationKey())).mergeStyle(TextFormatting.GOLD)));
                break;
        }
        return info;
    }

    private boolean jeiVisible = true;
    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    public enum Action {
        CHECK,
        CONSUME,
        PRODUCE;

        public static Action value(String mode) {
            return valueOf(mode.toUpperCase(Locale.ENGLISH));
        }
    }
}
