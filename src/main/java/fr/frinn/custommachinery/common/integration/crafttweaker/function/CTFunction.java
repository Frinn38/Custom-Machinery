package fr.frinn.custommachinery.common.integration.crafttweaker.function;

import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import org.openzen.zencode.java.ZenCodeType;

import java.util.function.Function;

public class CTFunction implements Function<ICraftingContext, CraftingResult> {

    private final Function<Context, CraftingResult> function;

    public CTFunction(Function<Context, CraftingResult> function) {
        this.function = function;
    }

    @ZenCodeType.Method
    @Override
    public CraftingResult apply(ICraftingContext context) {
        return this.function.apply(new Context(context));
    }
}
