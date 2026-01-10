package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.api.upgrade.Operation;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MachineUpgradeBuilder {

    private Item item;
    private final List<ResourceLocation> machines;
    private final List<RecipeModifierBuilder> recipeModifiers;
    private final List<ComponentModifier> componentModifiers;
    @Nullable
    private CoreModifier coreModifier;
    private int max;
    private final List<Component> tooltips;

    public MachineUpgradeBuilder() {
        this.item = Items.AIR;
        this.machines = new ArrayList<>();
        this.recipeModifiers = new ArrayList<>();
        this.componentModifiers = new ArrayList<>();
        this.coreModifier = null;
        this.max = 64;
        this.tooltips = new ArrayList<>();
        this.tooltips.add(MachineUpgrade.DEFAULT_TOOLTIP);
    }

    public MachineUpgradeBuilder(MachineUpgrade upgrade) {
        this.item = upgrade.item();
        this.machines = new ArrayList<>(upgrade.machines());
        this.recipeModifiers = upgrade.recipeModifiers().stream().map(RecipeModifierBuilder::new).collect(Collectors.toCollection(ArrayList::new));
        this.componentModifiers = new ArrayList<>(upgrade.components());
        this.coreModifier = upgrade.coreModifier().orElse(null);
        this.max = upgrade.max();
        this.tooltips = new ArrayList<>(upgrade.tooltips());
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<ResourceLocation> getMachines() {
        return this.machines;
    }

    public List<RecipeModifierBuilder> getRecipeModifiers() {
        return this.recipeModifiers;
    }

    public List<ComponentModifier> getComponentModifiers() {
        return this.componentModifiers;
    }

    @Nullable
    public CoreModifier getCoreModifier() {
        return this.coreModifier;
    }

    public void setCoreModifier(@Nullable CoreModifier coreModifier) {
        this.coreModifier = coreModifier;
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public List<Component> getTooltips() {
        return this.tooltips;
    }

    public MachineUpgrade build() {
        return new MachineUpgrade(this.item,
                Collections.unmodifiableList(this.machines),
                this.recipeModifiers.stream().map(RecipeModifierBuilder::build).toList(),
                Collections.unmodifiableList(this.componentModifiers),
                Optional.ofNullable(this.coreModifier),
                Collections.unmodifiableList(this.tooltips),
                this.max);
    }

    public static class RecipeModifierBuilder {

        private RequirementType<?> requirementType;
        private RequirementIOMode mode;
        private String target;
        private Operation operation;
        private double modifier;
        private double chance;
        private double max;
        private double min;
        private Component tooltip;

        public RecipeModifierBuilder() {
            this.requirementType = Registration.ENERGY_REQUIREMENT.get();
            this.mode = RequirementIOMode.INPUT;
            this.target = "";
            this.operation = Operation.ADDITION;
            this.modifier = 1;
            this.chance = 1;
            this.max = Double.POSITIVE_INFINITY;
            this.min = Double.NEGATIVE_INFINITY;
            this.tooltip = Component.empty();
        }

        public RecipeModifierBuilder(RecipeModifier modifier) {
            this.requirementType = modifier.requirementType();
            this.mode = modifier.mode();
            this.target = modifier.target();
            this.operation = modifier.operation();
            this.modifier = modifier.modifier();
            this.chance = modifier.chance();
            this.max = modifier.max();
            this.min = modifier.min();
            this.tooltip = modifier.tooltip();
        }

        public RequirementType<?> getRequirementType() {
            return this.requirementType;
        }

        public void setRequirementType(RequirementType<?> requirementType) {
            this.requirementType = requirementType;
        }

        public RequirementIOMode getMode() {
            return this.mode;
        }

        public void setMode(RequirementIOMode mode) {
            this.mode = mode;
        }

        public String getTarget() {
            return this.target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public Operation getOperation() {
            return this.operation;
        }

        public void setOperation(Operation operation) {
            this.operation = operation;
        }

        public double getModifier() {
            return this.modifier;
        }

        public void setModifier(double modifier) {
            this.modifier = modifier;
        }

        public double getChance() {
            return this.chance;
        }

        public void setChance(double chance) {
            this.chance = chance;
        }

        public double getMax() {
            return this.max;
        }

        public void setMax(double max) {
            this.max = max;
        }

        public double getMin() {
            return this.min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public Component getTooltip() {
            return this.tooltip;
        }

        public void setTooltip(Component tooltip) {
            this.tooltip = tooltip;
        }

        public RecipeModifier build() {
            return new RecipeModifier(this.requirementType, this.mode, this.target, this.operation, this.modifier, this.chance, this.max, this.min, this.tooltip);
        }
    }
}
