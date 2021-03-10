package fr.frinn.custommachinery.common.integration.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class DummyIngredientRenderer<T> implements IIngredientRenderer<T> {

    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int x, int y, @Nullable T t) {

    }

    @ParametersAreNonnullByDefault
    @Override
    public List<ITextComponent> getTooltip(T t, ITooltipFlag iTooltipFlag) {
        return new ArrayList<>();
    }
}
