package fr.frinn.custommachinery.common.crafting;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class CraftingResult {

    private static final CraftingResult SUCCES = new CraftingResult(RESULT.SUCCESS, new StringTextComponent("success"));
    private static final CraftingResult PASS = new CraftingResult(RESULT.PASS, new StringTextComponent("pass"));

    private final RESULT result;
    private final ITextComponent message;

    public CraftingResult(RESULT result, ITextComponent message) {
        this.result = result;
        this.message = message;
    }

    public static CraftingResult success() {
        return SUCCES;
    }

    public static CraftingResult pass() {
        return PASS;
    }

    public static CraftingResult error(ITextComponent message) {
        return new CraftingResult(RESULT.ERROR, message);
    }

    public boolean isSuccess() {
        return result != RESULT.ERROR;
    }

    public ITextComponent getMessage() {
        return this.message;
    }

    public enum RESULT {
        SUCCESS,
        PASS,
        ERROR
    }
}
