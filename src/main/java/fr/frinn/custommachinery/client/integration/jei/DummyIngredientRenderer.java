package fr.frinn.custommachinery.client.integration.jei;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DummyIngredientRenderer<T> implements IIngredientRenderer<T> {

    @Override
    public void render(GuiGraphicsExtractor graphics, @Nullable T t) {

    }

    //Safe to remove
    @SuppressWarnings("removal")
    @Override
    public List<Component> getTooltip(T t, TooltipFlag iTooltipFlag) {
        return new ArrayList<>();
    }

    @Override
    public List<Component> getTooltip(T ingredient, TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
        return Collections.emptyList();
    }
}
