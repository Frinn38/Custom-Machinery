package fr.frinn.custommachinery.common.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MachineUpgradeBuilder {

    private Item item;
    private final List<ResourceLocation> machines;
    private final List<RecipeModifier> recipeModifiers;
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
        this.recipeModifiers = new ArrayList<>(upgrade.recipeModifiers());
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

    public List<RecipeModifier> getRecipeModifiers() {
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
                Collections.unmodifiableList(this.recipeModifiers),
                Collections.unmodifiableList(this.componentModifiers),
                Optional.ofNullable(this.coreModifier),
                Collections.unmodifiableList(this.tooltips),
                this.max);
    }
}
