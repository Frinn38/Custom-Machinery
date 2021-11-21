package fr.frinn.custommachinery.common.integration.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.CustomMachinery;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public enum RecipeTransferError implements IRecipeTransferError {
    INVALID_RECIPE("Invalid recipe"),
    NOT_ENOUGH_ITEM("Not enough items");

    private final String error;

    RecipeTransferError(String error) {
        this.error = error;
    }


    @Override
    public Type getType() {
        return Type.USER_FACING;
    }

    @Override
    public void showError(MatrixStack matrixStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX, int recipeY) {
        CustomMachinery.LOGGER.warn(this.error);
    }
}
