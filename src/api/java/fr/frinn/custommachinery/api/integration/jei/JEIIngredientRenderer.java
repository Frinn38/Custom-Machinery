package fr.frinn.custommachinery.api.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public abstract class JEIIngredientRenderer<T, E extends IGuiElement> implements IIngredientRenderer<T> {

    private final E element;

    public JEIIngredientRenderer(E element) {
        this.element = element;
    }

    public abstract IIngredientType<T> getType();

    @Override
    public void render(PoseStack matrix, @Nullable T ingredient) {
        this.render(matrix, this.element, ingredient);
    }

    @Override
    public List<Component> getTooltip(T ingredient, TooltipFlag flag) {
        return getTooltip(ingredient, this.element, flag);
    }

    public abstract void render(PoseStack matrix, E element, @Nullable T ingredient);

    public abstract List<Component> getTooltip(T ingredient, E element, TooltipFlag flag);
}
