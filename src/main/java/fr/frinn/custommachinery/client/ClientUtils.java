package fr.frinn.custommachinery.client;

import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.stream.Stream;

public class ClientUtils {

    public static CustomMachineTile getClientSideCustomMachineTile(BlockPos pos) {
        if(Minecraft.getInstance().level != null) {
            BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile)
                return (CustomMachineTile)tile;
        }
        throw new IllegalStateException("Trying to open a Custom Machine container without clicking on a Custom Machine block");
    }

    public static void refreshMachineContainer() {
        if(Minecraft.getInstance().player instanceof Player player && player.containerMenu instanceof CustomMachineContainer container)
            container.init();
    }

    public static void addToast(Component title) {
        Minecraft.getInstance().getToastManager().addToast(new TutorialToast(Minecraft.getInstance().font, TutorialToast.Icons.MOUSE, title, null, false, 50));
    }

    public static Stream<ItemStack> getIngredientStacks(Ingredient ingredient) {
        Level level = Minecraft.getInstance().level;
        if(level == null)
            return Stream.empty();
        ContextMap context = SlotDisplayContext.fromLevel(level);
        return ingredient.display().resolveForStacks(context).stream();
    }

    public static Stream<ItemStack> getIngredientStacks(SizedIngredient ingredient) {
        return getIngredientStacks(ingredient.ingredient()).map(stack -> stack.copyWithCount(ingredient.count()));
    }
}
