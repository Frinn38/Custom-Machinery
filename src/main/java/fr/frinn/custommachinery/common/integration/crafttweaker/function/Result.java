package fr.frinn.custommachinery.common.integration.crafttweaker.function;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name("mods.custommachinery.Result")
public class Result {

    @Method
    public static CraftingResult success() {
        return CraftingResult.success();
    }

    @Method
    public static CraftingResult error(TextComponent error) {
        return CraftingResult.error(error);
    }

    @Method
    public static CraftingResult error(String error) {
        return CraftingResult.error(new TranslatableComponent(error));
    }
}
