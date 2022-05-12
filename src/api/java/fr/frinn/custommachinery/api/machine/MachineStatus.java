package fr.frinn.custommachinery.api.machine;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * An enum of all possible states the machine can be.
 * The machine status is hold by the CraftingManager attached to the MachineTile and is synced automatically when changed.
 */
public enum MachineStatus implements StringRepresentable {
    /**
     * The machine search for a recipe it can process.
     */
    IDLE,
    /**
     * The machine is processing a recipe.
     */
    RUNNING,
    /**
     * The machine encountered an error when processing a recipe, usually either a missing input or not space for output.
     */
    ERRORED,
    /**
     * The machine is paused.
     */
    PAUSED;

    public static MachineStatus value(String string) {
        return valueOf(string.toUpperCase(Locale.ENGLISH));
    }

    public static MachineStatus getValueOrNull(String value) {
        try {
            return value(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return toString();
    }

    public TranslatableComponent getTranslatedName() {
        return new TranslatableComponent("custommachinery.craftingstatus." + this);
    }
}
