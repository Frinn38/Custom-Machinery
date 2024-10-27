package fr.frinn.custommachinery.client.integration.jei.wrapper;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import fr.frinn.custommachinery.common.util.LootTableHelper.LootData;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class LootTableIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final ResourceLocation lootTable;

    public LootTableIngredientWrapper(ResourceLocation lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement slotElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        List<LootData> loots = LootTableHelper.getLootsForTable(this.lootTable);
        List<ItemStack> ingredients = Lists.newArrayList(loots.stream().map(LootData::stack).toList());
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        if(template.map(t -> t.canAccept(ingredients, false, helper.getDummyManager())).orElse(false)) {
            int slotX = element.getX() + (element.getWidth() - 16) / 2;
            int slotY = element.getY() + (element.getHeight() - 16) / 2;
            builder.addSlot(RecipeIngredientRole.OUTPUT, slotX - xOffset, slotY - yOffset)
                    .addIngredients(VanillaTypes.ITEM_STACK, ingredients)
                    .addRichTooltipCallback((view, tooltips) -> {
                        LootData data = view.getDisplayedIngredient()
                                .flatMap(ingredient -> loots.stream().filter(lootData -> ItemStack.isSameItemSameComponents(lootData.stack(), ingredient.getItemStack().get())).findFirst())
                                .orElse(null);
                        if(data == null)
                            return;
                        if(data.chance() != 1){
                            double percentage = data.chance() * 100;
                            if(percentage < 0.01F)
                                tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", "<0.01"));
                            else {
                                BigDecimal decimal = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP);
                                if(decimal.scale() <= 0 || decimal.signum() == 0 || decimal.stripTrailingZeros().scale() <= 0)
                                    tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", decimal.intValue()));
                                else
                                    tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", decimal.doubleValue()));
                            }
                            if(!data.rolls().isEmpty())
                                tooltips.add(Component.literal(data.rolls()));
                            if(!data.bonusRolls().isEmpty())
                                tooltips.add(Component.literal(data.bonusRolls()));
                        }
                    });
            return true;
        }
        return false;
    }
}
