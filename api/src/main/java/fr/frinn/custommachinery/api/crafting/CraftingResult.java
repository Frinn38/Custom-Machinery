package fr.frinn.custommachinery.api.crafting;

import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import net.minecraft.network.chat.Component;

/**
 * This represents the result of any crafting operation.
 * When an {@link IRequirement} is executed during the process it must return an instance of this class.
 */
public final class CraftingResult {

    /**
     * Default instances of the success and pass result, to avoid creating tons of new objects.
     */
    private static final CraftingResult SUCCESS = new CraftingResult(RESULT.SUCCESS, Component.literal("success"));
    private static final CraftingResult PASS = new CraftingResult(RESULT.PASS, Component.literal("pass"));

    private final RESULT result;
    private final Component message;

    private CraftingResult(RESULT result, Component message) {
        this.result = result;
        this.message = message;
    }

    /**
     * Return this if the requirement successfully did its things.
     * @return A success.
     */
    public static CraftingResult success() {
        return SUCCESS;
    }

    /**
     * Return this if the {@link IRequirement} didn't do anything.
     * @return A pass.
     */
    public static CraftingResult pass() {
        return PASS;
    }

    /**
     * Return this if there was an error during the execution of this requirement.
     * @param message An error message to display to the player.
     * @return An error.
     */
    public static CraftingResult error(Component message) {
        return new CraftingResult(RESULT.ERROR, message);
    }

    /**
     * @return True if the crafting process can continue, false otherwise and the {@link MachineStatus} will be set to {@link MachineStatus#ERRORED}.
     */
    public boolean isSuccess() {
        return result != RESULT.ERROR;
    }

    /**
     * @return The error message to display to the player, localized messages are preferable but not mandatory.
     */
    public Component getMessage() {
        return this.message;
    }

    public enum RESULT {
        SUCCESS,
        PASS,
        ERROR
    }
}
