package fr.frinn.custommachinery.common.integration.kubejs.function;

import dev.latvian.kubejs.text.Text;
import fr.frinn.custommachinery.api.crafting.CraftingResult;

public class Result {

    private final CraftingResult internal;

    private Result(CraftingResult internal) {
        this.internal = internal;
    }

    public static Result success() {
        return new Result(CraftingResult.success());
    }

    public static Result error(Text error) {
        return new Result(CraftingResult.error(error.component()));
    }

    protected CraftingResult getInternal() {
        return this.internal;
    }
}
