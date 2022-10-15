package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.common.collect.ImmutableList;
import dev.latvian.mods.kubejs.event.EventJS;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.RecipeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomMachineJSUpgradeBuilder {

    private final Item item;
    private List<Component> tooltips;
    private final List<ResourceLocation> machines;
    private final List<RecipeModifier> modifiers;
    private final int maxAmount;

    public CustomMachineJSUpgradeBuilder(Item item, int maxAmount) {
        this.item = item;
        this.tooltips = Collections.singletonList(new TranslatableComponent("custommachinery.upgrade.tooltip").withStyle(ChatFormatting.AQUA));
        this.maxAmount = maxAmount;
        this.machines = new ArrayList<>();
        this.modifiers = new ArrayList<>();
    }

    public MachineUpgrade build() {
        if(this.machines.isEmpty())
            throw new IllegalArgumentException("You must specify at least 1 machine for machine upgrade item: " + Registry.ITEM.getKey(this.item));
        if(this.modifiers.isEmpty())
            throw new IllegalArgumentException("You must specify at least 1 recipe modifier for machine upgrade item: " + Registry.ITEM.getKey(this.item));
        return new MachineUpgrade(this.item, this.machines, this.modifiers, this.tooltips, this.maxAmount);
    }

    public CustomMachineJSUpgradeBuilder machine(String... string) {
        for(String s : string) {
            final ResourceLocation machine;
            try {
                machine = new ResourceLocation(s);
            } catch (ResourceLocationException e) {
                throw new IllegalArgumentException("Invalid Machine ID: " + s + "\n" + e.getMessage());
            }
            this.machines.add(machine);
        }
        return this;
    }

    public CustomMachineJSUpgradeBuilder tooltip(Component... components) {
        this.tooltips = ImmutableList.copyOf(components);
        return this;
    }

    public CustomMachineJSUpgradeBuilder modifier(JSRecipeModifierBuilder builder) {
        this.modifiers.add(builder.build());
        return this;
    }

    public static class JSRecipeModifierBuilder {

        private final RequirementType<?> requirementType;
        private final RequirementIOMode mode;
        private final RecipeModifier.OPERATION operation;
        private final double modifier;
        private String target = "";
        private double chance = 1.0D;
        private double max = Double.POSITIVE_INFINITY;
        private double min = Double.NEGATIVE_INFINITY;
        private Component tooltip = null;

        private JSRecipeModifierBuilder(RequirementType<?> type, RequirementIOMode mode, RecipeModifier.OPERATION operation, double modifier) {
            this.requirementType = type;
            this.mode = mode;
            this.operation = operation;
            this.modifier = modifier;
        }

        private static RequirementType<?> getType(ResourceLocation id) {
            RequirementType<?> type = Registration.REQUIREMENT_TYPE_REGISTRY.get(id);
            if(type != null)
                return type;
            throw new IllegalArgumentException("Invalid requirement type: " + id);
        }

        public static JSRecipeModifierBuilder addInput(ResourceLocation type, double modifier) {
            return new JSRecipeModifierBuilder(getType(type), RequirementIOMode.INPUT, RecipeModifier.OPERATION.ADDITION, modifier);
        }

        public static JSRecipeModifierBuilder mulInput(ResourceLocation type, double modifier) {
            return new JSRecipeModifierBuilder(getType(type), RequirementIOMode.INPUT, RecipeModifier.OPERATION.MULTIPLICATION, modifier);
        }

        public static JSRecipeModifierBuilder addOutput(ResourceLocation type, double modifier) {
            return new JSRecipeModifierBuilder(getType(type), RequirementIOMode.OUTPUT, RecipeModifier.OPERATION.ADDITION, modifier);
        }

        public static JSRecipeModifierBuilder mulOutput(ResourceLocation type, double modifier) {
            return new JSRecipeModifierBuilder(getType(type), RequirementIOMode.OUTPUT, RecipeModifier.OPERATION.MULTIPLICATION, modifier);
        }

        public JSRecipeModifierBuilder target(String target) {
            this.target = target;
            return this;
        }

        public JSRecipeModifierBuilder chance(double chance) {
            this.chance = chance;
            return this;
        }

        public JSRecipeModifierBuilder max(double max) {
            this.max = max;
            return this;
        }

        public JSRecipeModifierBuilder min(double min) {
            this.min = min;
            return this;
        }

        public JSRecipeModifierBuilder tooltip(Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        private RecipeModifier build() {
            return new RecipeModifier(this.requirementType, this.mode, this.operation, this.modifier, this.target, this.chance, this.max, this.min, this.tooltip);
        }
    }

    public static class UpgradeEvent extends EventJS {

        private final List<CustomMachineJSUpgradeBuilder> builders = new ArrayList<>();

        public CustomMachineJSUpgradeBuilder create(Item item) {
            CustomMachineJSUpgradeBuilder builder = new CustomMachineJSUpgradeBuilder(item, 64);
            this.builders.add(builder);
            return builder;
        }

        public CustomMachineJSUpgradeBuilder create(Item item, int maxAmount) {
            CustomMachineJSUpgradeBuilder builder = new CustomMachineJSUpgradeBuilder(item, maxAmount);
            this.builders.add(builder);
            return builder;
        }

        public List<CustomMachineJSUpgradeBuilder> getBuilders() {
            return this.builders;
        }
    }
}
