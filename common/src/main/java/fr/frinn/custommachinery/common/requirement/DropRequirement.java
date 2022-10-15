package fr.frinn.custommachinery.common.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.IDelayedRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.DropMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedChanceableRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class DropRequirement extends AbstractDelayedChanceableRequirement<DropMachineComponent> implements ITickableRequirement<DropMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<DropRequirement> CODEC = RecordCodecBuilder.create(dropRequirementInstance ->
            dropRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(IRequirement::getMode),
                    Codecs.DROP_REQUIREMENT_ACTION_CODEC.fieldOf("action").forGetter(requirement -> requirement.action),
                    CodecLogger.loggedOptional(Codecs.list(IIngredient.ITEM), "input", Collections.emptyList()).forGetter(requirement -> requirement.input),
                    CodecLogger.loggedOptional(Codec.BOOL, "whitelist", true).forGetter(requirement -> requirement.whitelist),
                    CodecLogger.loggedOptional(RegistrarCodec.ITEM, "output", Items.AIR).forGetter(requirement -> requirement.output),
                    CodecLogger.loggedOptional(Codecs.COMPOUND_NBT_CODEC, "nbt").forGetter(requirement -> Optional.ofNullable(requirement.nbt)),
                    CodecLogger.loggedOptional(Codec.intRange(1, Integer.MAX_VALUE), "amount", 1).forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codec.intRange(1, Integer.MAX_VALUE), "radius", 1).forGetter(requirement -> requirement.radius),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0), "chance", 1.0).forGetter(AbstractDelayedChanceableRequirement::getChance),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0), "delay", 0.0).forGetter(IDelayedRequirement::getDelay)
            ).apply(dropRequirementInstance, (mode, action, input, whitelist, output, nbt, amount, radius, chance, delay) -> {
                DropRequirement requirement = new DropRequirement(mode, action, input, whitelist, output, nbt.orElse(null), amount, radius);
                requirement.setChance(chance);
                requirement.setDelay(delay);
                return requirement;
            })
    );

    private final Action action;
    private final List<IIngredient<Item>> input;
    private final boolean whitelist;
    private final Item output;
    @Nullable
    private final CompoundTag nbt;
    private final int amount;
    private final int radius;

    public DropRequirement(RequirementIOMode mode, Action action, List<IIngredient<Item>> input, boolean whitelist, Item output, @Nullable CompoundTag nbt, int amount, int radius) {
        super(mode);
        this.action = action;
        if((action == Action.CHECK || action == Action.CONSUME) && input.isEmpty())
            throw new IllegalArgumentException("Drop requirement in" + action + "  mode MUST have at least one input item ingredient !");
        this.input = input;
        this.whitelist = whitelist;
        if(action == Action.PRODUCE && output == Items.AIR)
            throw new IllegalArgumentException("Drop requirement in " + action + " mode MUST have an output item specified !");
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
    public boolean test(DropMachineComponent component, ICraftingContext context) {
        int amount = (int) context.getModifiedValue(this.amount, this, null);
        if(this.action == Action.CHECK || this.action == Action.CONSUME)
            return component.getItemAmount(this.input, this.radius, this.whitelist) >= amount;
        return true;
    }

    @Override
    public CraftingResult processStart(DropMachineComponent component, ICraftingContext context) {
        if(getDelay() != 0.0 || getMode() != RequirementIOMode.INPUT)
            return CraftingResult.pass();
        int amount = (int) context.getModifiedValue(this.amount, this, null);
        double radius = context.getModifiedValue(this.radius, this, "radius");
        switch (this.action) {
            case CONSUME:
                int found = component.getItemAmount(this.input, radius, this.whitelist);
                if(found >= amount) {
                    component.consumeItem(this.input, amount, radius, this.whitelist);
                    return CraftingResult.success();
                }
                else
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.drop.error.input", amount, found));
            case PRODUCE:
                ItemStack stack = Utils.makeItemStack(this.output, amount, null);
                if(component.produceItem(stack))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.drop.error.input", new TextComponent(amount + "x").append(new TranslatableComponent(this.output.getDescriptionId(stack)))));
            default:
                return CraftingResult.pass();
        }
    }

    @Override
    public CraftingResult processEnd(DropMachineComponent component, ICraftingContext context) {
        if(getDelay() != 0.0 || getMode() != RequirementIOMode.OUTPUT)
            return CraftingResult.pass();
        int amount = (int) context.getModifiedValue(this.amount, this, null);
        double radius = context.getModifiedValue(this.radius, this, "radius");
        switch (this.action) {
            case CONSUME:
                int found = component.getItemAmount(this.input, radius, this.whitelist);
                if(found > amount) {
                    component.consumeItem(this.input, amount, radius, this.whitelist);
                    return CraftingResult.success();
                }
                else
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.drop.error.input", amount, found));
            case PRODUCE:
                ItemStack stack = Utils.makeItemStack(this.output, amount, null);
                if(component.produceItem(stack))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.drop.error.input", new TextComponent(amount + "x").append(new TranslatableComponent(this.output.getDescriptionId(stack)))));
            default:
                return CraftingResult.pass();
        }
    }

    @Override
    public MachineComponentType<DropMachineComponent> getComponentType() {
        return Registration.DROP_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(DropMachineComponent component, ICraftingContext context) {
        if(this.action == Action.CHECK) {
            int amount = (int) context.getModifiedValue(this.amount, this, null);
            double radius = context.getModifiedValue(this.radius, this, "radius");
            int found = component.getItemAmount(this.input, radius, this.whitelist);
            if(found >= amount)
                return CraftingResult.success();
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.drop.error.input", amount, found));
        }
        return CraftingResult.pass();
    }

    /** DELAY **/

    @Override
    public CraftingResult execute(DropMachineComponent component, ICraftingContext context) {
        int amount = (int) context.getModifiedValue(this.amount, this, null);
        double radius = context.getModifiedValue(this.radius, this, "radius");
        switch (this.action) {
            case CONSUME:
                int found = component.getItemAmount(this.input, radius, this.whitelist);
                if(found > amount) {
                    component.consumeItem(this.input, amount, radius, this.whitelist);
                    return CraftingResult.success();
                }
                else
                    return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.drop.error.input", amount, found));
            case PRODUCE:
                ItemStack stack = Utils.makeItemStack(this.output, amount, null);
                if(component.produceItem(stack))
                    return CraftingResult.success();
                return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.drop.error.input", new TextComponent(amount + "x").append(new TranslatableComponent(this.output.getDescriptionId(stack)))));
            default:
                return CraftingResult.pass();
        }
    }


    /** DISPLAY **/

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        switch (this.action) {
            case CHECK -> {
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.drop.info.check", this.amount, this.radius));
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.drop.info." + (this.whitelist ? "whitelist" : "blacklist")).withStyle(this.whitelist ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED));
                this.input.forEach(ingredient -> info.addTooltip(new TextComponent(ingredient.toString())));
                info.setItemIcon(Items.OAK_PRESSURE_PLATE);
            }
            case CONSUME -> {
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.drop.info.consume", this.amount, this.radius));
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.drop.info." + (this.whitelist ? "whitelist" : "blacklist")).withStyle(this.whitelist ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED));
                this.input.forEach(ingredient -> info.addTooltip(new TextComponent("- " + ingredient.toString())));
                info.setItemIcon(Items.HOPPER);
            }
            case PRODUCE -> {
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.drop.info.produce", new TextComponent(this.amount + "x ").append(new TranslatableComponent(this.output.getDescriptionId())).withStyle(ChatFormatting.GOLD)));
                info.setItemIcon(Items.DROPPER);
            }
        }
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
