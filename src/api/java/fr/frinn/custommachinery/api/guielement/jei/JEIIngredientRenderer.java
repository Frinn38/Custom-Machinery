package fr.frinn.custommachinery.api.guielement.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import mezz.jei.api.MethodsReturnNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class JEIIngredientRenderer<T, E extends IGuiElement> implements IIngredientRenderer<T> {

    private final E element;

    public JEIIngredientRenderer(E element) {
        this.element = element;
    }

    public abstract IIngredientType<T> getType();

    @Override
    public void render(MatrixStack matrix, int x, int y, @Nullable T ingredient) {
        matrix.push();
        matrix.translate(x, y, 0);
        this.render(matrix, 0, 0, this.element, ingredient);
        matrix.pop();
    }

    @Override
    public List<ITextComponent> getTooltip(T ingredient, ITooltipFlag iTooltipFlag) {
        return getTooltip(ingredient, this.element, iTooltipFlag);
    }

    public abstract void render(MatrixStack matrix, int x, int y, E element, @Nullable T ingredient);

    public abstract List<ITextComponent> getTooltip(T ingredient, E element, ITooltipFlag iTooltipFlag);
}
