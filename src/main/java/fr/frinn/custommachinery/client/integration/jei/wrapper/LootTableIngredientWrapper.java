package fr.frinn.custommachinery.client.integration.jei.wrapper;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LootTableIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final ResourceLocation lootTable;

    public LootTableIngredientWrapper(ResourceLocation lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement slotElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        Map<ItemStack, Double> table = LootTableHelper.getLootsForTable(this.lootTable).stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        List<ItemStack> ingredients = Lists.newArrayList(table.keySet());
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        if(template.map(t -> t.canAccept(ingredients, false, helper.getDummyManager())).orElse(false)) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, element.getX() - xOffset, element.getY() - yOffset)
                    .addIngredients(VanillaTypes.ITEM_STACK, ingredients)
                    .addTooltipCallback((view, tooltips) -> {
                        double chance = view.getDisplayedIngredient(VanillaTypes.ITEM_STACK).map(table::get).orElse(1.0D);
                        if(chance != 1){
                            double percentage = chance * 100;
                            if(percentage < 0.01F)
                                tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", "<0.01"));
                            else {
                                BigDecimal decimal = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP);
                                if(decimal.scale() <= 0 || decimal.signum() == 0 || decimal.stripTrailingZeros().scale() <= 0)
                                    tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", decimal.intValue()));
                                else
                                    tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", decimal.doubleValue()));
                            }
                        }
                    });
            return true;
        }
        return false;
    }
}
