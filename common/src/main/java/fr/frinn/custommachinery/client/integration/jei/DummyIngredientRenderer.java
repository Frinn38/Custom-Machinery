package fr.frinn.custommachinery.client.integration.jei;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DummyIngredientRenderer<T> implements IIngredientRenderer<T> {

    @Override
    public void render(GuiGraphics graphics, @Nullable T t) {

    }

    @Override
    public List<Component> getTooltip(T t, TooltipFlag iTooltipFlag) {
        return new ArrayList<>();
    }
}
