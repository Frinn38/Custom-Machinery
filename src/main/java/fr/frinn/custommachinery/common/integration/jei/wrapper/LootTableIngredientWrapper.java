package fr.frinn.custommachinery.common.integration.jei.wrapper;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.apiimpl.integration.jei.Ingredients;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
    public IIngredientType<ItemStack> getJEIIngredientType() {
        return VanillaTypes.ITEM;
    }

    @Override
    public void setIngredient(Ingredients ingredients) {
        List<ItemStack> items = LootTableHelper.getLootsForTable(this.lootTable).stream().map(Pair::getFirst).collect(Collectors.toList());
        ingredients.addOutputs(VanillaTypes.ITEM, items);
    }

    @Override
    public boolean setupRecipe(int index, IRecipeLayout layout, int xOffset, int yOffset, IGuiElement element, IIngredientRenderer<ItemStack> renderer, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        Map<ItemStack, Double> ingredients = LootTableHelper.getLootsForTable(this.lootTable).stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        SlotGuiElement slotElement = (SlotGuiElement)element;
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        if(template.map(t -> t.canAccept(new ArrayList<>(ingredients.keySet()), false, helper.getDummyManager())).orElse(false)) {
            layout.getIngredientsGroup(getJEIIngredientType()).init(index, false, renderer, element.getX() - xOffset, element.getY() - yOffset, element.getWidth() - 2, element.getHeight() - 2, 0, 0);
            IGuiIngredientGroup<ItemStack> group = layout.getIngredientsGroup(VanillaTypes.ITEM);
            group.set(index, new ArrayList<>(ingredients.keySet()));
            group.addTooltipCallback(((slotIndex, input, ingredient, tooltips) -> {
                if(slotIndex != index)
                    return;

                double chance = ingredients.get(ingredient);
                if(chance != 1){
                    double percentage = chance * 100;
                    if(percentage < 0.01F)
                        tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance", "<0.01"));
                    else {
                        BigDecimal decimal = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP);
                        if(decimal.scale() <= 0 || decimal.signum() == 0 || decimal.stripTrailingZeros().scale() <= 0)
                            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance", decimal.intValue()));
                        else
                            tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance", decimal.doubleValue()));
                    }
                }
            }));
            return true;
        }
        return false;
    }
}
