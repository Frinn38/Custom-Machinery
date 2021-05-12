package fr.frinn.custommachinery.client.render.element.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.common.data.gui.IGuiElement;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public abstract class JEIIngredientRenderer<T, E extends IGuiElement> implements IIngredientRenderer<T> {

    private E element;

    public JEIIngredientRenderer(E element) {
        this.element = element;
    }

    public abstract IIngredientType<T> getType();

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int x, int y, @Nullable T ingredient) {
        matrix.push();
        matrix.scale(0.5F, 0.5F, 0);
        matrix.translate(x, y, 0);
        this.render(matrix, x, y, this.element, ingredient);
        matrix.pop();
    }

    @ParametersAreNonnullByDefault
    @Override
    public List<ITextComponent> getTooltip(T ingredient, ITooltipFlag iTooltipFlag) {
        return getTooltip(ingredient, this.element, iTooltipFlag);
    }

    public abstract void render(MatrixStack matrix, int x, int y, E element, @Nullable T ingredient);

    public abstract List<ITextComponent> getTooltip(T ingredient, E element, ITooltipFlag iTooltipFlag);
}
