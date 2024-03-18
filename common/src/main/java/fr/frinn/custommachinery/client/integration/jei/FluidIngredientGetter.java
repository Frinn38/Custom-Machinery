package fr.frinn.custommachinery.client.integration.jei;

import fr.frinn.custommachinery.common.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElementWidget;
import fr.frinn.custommachinery.impl.integration.jei.WidgetToJeiIngredientRegistry.IngredientGetter;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;

public class FluidIngredientGetter implements IngredientGetter<FluidGuiElement> {

    @Override
    @Nullable
    public <T> IClickableIngredient<T> getIngredient(AbstractGuiElementWidget<FluidGuiElement> widget, double mouseX, double mouseY, IJeiHelpers helpers) {
        FluidMachineComponent component = widget.getScreen().getTile().getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get()).flatMap(fluidHandler -> fluidHandler.getComponentForID(widget.getElement().getID())).orElse(null);
        if (component == null)
            return null;
        IIngredientManager manager = helpers.getIngredientManager();
        IPlatformFluidHelper fluidHelper = helpers.getPlatformFluidHelper();
        ITypedIngredient ingredient = (ITypedIngredient) manager.createTypedIngredient(fluidHelper.getFluidIngredientType(), fluidHelper.create(component.getFluidStack().getFluid(), component.getFluidStack().getAmount(), component.getFluidStack().getTag())).orElse(null);
        if (ingredient == null)
            return null;
        return new IClickableIngredient<T>() {
            @Override
            public ITypedIngredient<T> getTypedIngredient() {
                return ingredient;
            }

            @Override
            public Rect2i getArea() {
                return new Rect2i(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
            }
        };
    }
}