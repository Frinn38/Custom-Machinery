package fr.frinn.custommachinery.common.integration.crafttweaker.function;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.util.text.MCTextComponent;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import net.minecraft.util.text.TranslationTextComponent;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.custommachinery.Result")
public class Result {

    @ZenCodeType.Method
    public static CraftingResult success() {
        return CraftingResult.success();
    }

    @ZenCodeType.Method
    public static CraftingResult error(MCTextComponent error) {
        return CraftingResult.error(error.getInternal());
    }

    @ZenCodeType.Method
    public static CraftingResult error(String error) {
        return CraftingResult.error(new TranslationTextComponent(error));
    }
}
