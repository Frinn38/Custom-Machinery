package fr.frinn.custommachinery.common.integration.kubejs.function;

import fr.frinn.custommachinery.api.crafting.CraftingResult;
import net.minecraft.network.chat.Component;

//TODO: Remove in favor of Context methods
public class Result {

    private final CraftingResult internal;

    private Result(CraftingResult internal) {
        this.internal = internal;
    }

    public static Result success() {
        return new Result(CraftingResult.success());
    }

    public static Result error(Component error) {
        return new Result(CraftingResult.error(error));
    }

    protected CraftingResult getInternal() {
        return this.internal;
    }
}
