package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.common.collect.ImmutableList;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.event.KubeEvent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.upgrade.Operation;
import fr.frinn.custommachinery.common.upgrade.ComponentModifier;
import fr.frinn.custommachinery.common.upgrade.CoreModifier;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.RecipeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CustomMachineUpgradeJSBuilder {

    private final Item item;
    private List<Component> tooltips;
    private final List<ResourceLocation> machines;
    private final List<RecipeModifier> recipeModifiers;
    private final List<ComponentModifier> componentModifiers;
    private CoreModifier coreModifier = null;
    private final int maxAmount;

    public CustomMachineUpgradeJSBuilder(Item item, int maxAmount) {
        this.item = item;
        this.tooltips = Collections.singletonList(Component.translatable("custommachinery.upgrade.tooltip").withStyle(ChatFormatting.AQUA));
        this.machines = new ArrayList<>();
        this.recipeModifiers = new ArrayList<>();
        this.componentModifiers = new ArrayList<>();
        this.maxAmount = maxAmount;
    }

    public MachineUpgrade build() {
        if(this.machines.isEmpty())
            throw new IllegalArgumentException("You must specify at least 1 machine for machine upgrade item: " + BuiltInRegistries.ITEM.getKey(this.item));
        if(this.recipeModifiers.isEmpty() && this.componentModifiers.isEmpty() && this.coreModifier == null)
            throw new IllegalArgumentException("You must specify at least 1 recipe/component/core modifier for machine upgrade item: " + BuiltInRegistries.ITEM.getKey(this.item));
        return new MachineUpgrade(this.item, this.machines, this.recipeModifiers, this.componentModifiers, Optional.ofNullable(this.coreModifier), this.tooltips, this.maxAmount);
    }

    public CustomMachineUpgradeJSBuilder machine(String... string) {
        for(String s : string) {
            final ResourceLocation machine;
            try {
                machine = ResourceLocation.parse(s);
            } catch (ResourceLocationException e) {
                throw new IllegalArgumentException("Invalid Machine ID: " + s + "\n" + e.getMessage());
            }
            this.machines.add(machine);
        }
        return this;
    }

    public CustomMachineUpgradeJSBuilder tooltip(Component... components) {
        this.tooltips = ImmutableList.copyOf(components);
        return this;
    }

    public CustomMachineUpgradeJSBuilder modifier(JSRecipeModifierBuilder builder) {
        this.recipeModifiers.add(builder.build());
        return this;
    }

    public CustomMachineUpgradeJSBuilder requirement(JSRecipeModifierBuilder builder) {
        this.recipeModifiers.add(builder.build());
        return this;
    }

    public CustomMachineUpgradeJSBuilder component(JSComponentModifierBuilder builder) {
        this.componentModifiers.add(builder.build());
        return this;
    }

    public CustomMachineUpgradeJSBuilder cores(JSCoreModifierBuilder builder) {
        this.coreModifier = builder.build();
        return this;
    }

    public static class JSRecipeModifierBuilder {

        private final RequirementType<?> requirement;
        private final RequirementIOMode mode;
        private final Operation operation;
        private final double modifier;
        private String target = "";
        private double chance = 1.0D;
        private double max = Double.POSITIVE_INFINITY;
        private double min = Double.NEGATIVE_INFINITY;
        private Component tooltip = Component.empty();

        private JSRecipeModifierBuilder(RequirementType<?> type, RequirementIOMode mode, Operation operation, double modifier) {
            this.requirement = type;
            this.mode = mode;
            this.operation = operation;
            this.modifier = modifier;
        }

        public static JSRecipeModifierBuilder addInput(RequirementType<?> type, double modifier) {
            return new JSRecipeModifierBuilder(type, RequirementIOMode.INPUT, Operation.ADDITION, modifier);
        }

        public static JSRecipeModifierBuilder mulInput(RequirementType<?> type, double modifier) {
            return new JSRecipeModifierBuilder(type, RequirementIOMode.INPUT, Operation.MULTIPLICATION, modifier);
        }

        public static JSRecipeModifierBuilder expInput(RequirementType<?> type, double modifier) {
            return new JSRecipeModifierBuilder(type, RequirementIOMode.INPUT, Operation.EXPONENTIAL, modifier);
        }

        public static JSRecipeModifierBuilder addOutput(RequirementType<?> type, double modifier) {
            return new JSRecipeModifierBuilder(type, RequirementIOMode.OUTPUT, Operation.ADDITION, modifier);
        }

        public static JSRecipeModifierBuilder mulOutput(RequirementType<?> type, double modifier) {
            return new JSRecipeModifierBuilder(type, RequirementIOMode.OUTPUT, Operation.MULTIPLICATION, modifier);
        }

        public static JSRecipeModifierBuilder expOutput(RequirementType<?> type, double modifier) {
            return new JSRecipeModifierBuilder(type, RequirementIOMode.OUTPUT, Operation.EXPONENTIAL, modifier);
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
            return new RecipeModifier(this.requirement, this.mode, this.target, this.operation, this.modifier, this.chance, this.max, this.min, this.tooltip);
        }
    }

    public static class JSComponentModifierBuilder {

        private final MachineComponentType<?> type;
        private final String id;
        private final Operation operation;
        private final double modifier;
        private String target = "";
        private double max = Double.POSITIVE_INFINITY;
        private double min = Double.NEGATIVE_INFINITY;
        private Component tooltip = Component.empty();

        private JSComponentModifierBuilder(MachineComponentType<?> type, String id, Operation operation, double modifier) {
            this.type = type;
            this.id = id;
            this.operation = operation;
            this.modifier = modifier;
        }

        public static JSComponentModifierBuilder add(MachineComponentType<?> type, String id, double modifier) {
            return new JSComponentModifierBuilder(type, id, Operation.ADDITION, modifier);
        }

        public static JSComponentModifierBuilder mul(MachineComponentType<?> type, String id, double modifier) {
            return new JSComponentModifierBuilder(type, id, Operation.MULTIPLICATION, modifier);
        }

        public static JSComponentModifierBuilder exp(MachineComponentType<?> type, String id, double modifier) {
            return new JSComponentModifierBuilder(type, id, Operation.EXPONENTIAL, modifier);
        }

        public JSComponentModifierBuilder target(String target) {
            this.target = target;
            return this;
        }

        public JSComponentModifierBuilder max(double max) {
            this.max = max;
            return this;
        }

        public JSComponentModifierBuilder min(double min) {
            this.min = min;
            return this;
        }

        public JSComponentModifierBuilder tooltip(Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        private ComponentModifier build() {
            if(this.target == null || this.target.isEmpty())
                throw new KubeRuntimeException("Invalid CM upgrade component modifier for component: " + this.type.getId() + " target not specified");
            return new ComponentModifier(this.type, this.id, this.target, this.operation, this.modifier, this.max, this.min, this.tooltip);
        }
    }

    public static class JSCoreModifierBuilder {

        private final Operation operation;
        private final double modifier;
        private int max = 32;
        private int min = 1;
        private Component tooltip = null;

        private JSCoreModifierBuilder(Operation operation, double modifier) {
            this.operation = operation;
            this.modifier = modifier;
        }

        public static JSCoreModifierBuilder add(double modifier) {
            return new JSCoreModifierBuilder(Operation.ADDITION, modifier);
        }

        public static JSCoreModifierBuilder mul(double modifier) {
            return new JSCoreModifierBuilder(Operation.MULTIPLICATION, modifier);
        }

        public static JSCoreModifierBuilder exp(double modifier) {
            return new JSCoreModifierBuilder(Operation.EXPONENTIAL, modifier);
        }

        public JSCoreModifierBuilder max(int max) {
            this.max = max;
            return this;
        }

        public JSCoreModifierBuilder min(int min) {
            this.min = min;
            return this;
        }

        public JSCoreModifierBuilder tooltip(Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        private CoreModifier build() {
            return new CoreModifier(this.operation, this.modifier, this.max, this.min, this.tooltip);
        }
    }

    public static class UpgradeKubeEvent implements KubeEvent {

        private final List<CustomMachineUpgradeJSBuilder> builders = new ArrayList<>();

        public CustomMachineUpgradeJSBuilder create(Item item) {
            CustomMachineUpgradeJSBuilder builder = new CustomMachineUpgradeJSBuilder(item, 64);
            this.builders.add(builder);
            return builder;
        }

        public CustomMachineUpgradeJSBuilder create(Item item, int maxAmount) {
            CustomMachineUpgradeJSBuilder builder = new CustomMachineUpgradeJSBuilder(item, maxAmount);
            this.builders.add(builder);
            return builder;
        }

        public List<CustomMachineUpgradeJSBuilder> getBuilders() {
            return this.builders;
        }
    }
}
