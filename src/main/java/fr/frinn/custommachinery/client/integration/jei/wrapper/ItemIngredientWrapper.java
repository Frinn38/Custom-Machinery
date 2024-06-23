package fr.frinn.custommachinery.client.integration.jei.wrapper;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final RequirementIOMode mode;
    private final Ingredient item;
    private final int amount;
    private final double chance;
    private final boolean useDurability;
    private final String slot;
    private final boolean showRequireSlot;

    public ItemIngredientWrapper(RequirementIOMode mode, Ingredient item, int amount, double chance, boolean useDurability, String slot, boolean showRequireSlot) {
        this.mode = mode;
        this.item = item;
        this.amount = amount;
        this.chance = chance;
        this.useDurability = useDurability;
        this.slot = slot;
        this.showRequireSlot = showRequireSlot;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement slotElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        List<ItemStack> ingredients = Arrays.stream(this.item.getItems()).map(item -> item.copyWithCount(this.amount)).collect(Collectors.toCollection(ArrayList::new));
        if (ingredients.isEmpty()) {
            ItemStack itemStack = new ItemStack(Blocks.BARRIER);
            ingredients.add(itemStack);
        }
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        if(slotElement.getComponentId().equals(this.slot) || template.map(t -> t.canAccept(ingredients, this.mode == RequirementIOMode.INPUT, helper.getDummyManager()) && (this.slot.isEmpty() || t.getId().equals(this.slot))).orElse(false)) {
            int slotX = element.getX() + (element.getWidth() - 16) / 2;
            int slotY = element.getY() + (element.getHeight() - 16) / 2;
            builder.addSlot(roleFromMode(this.mode), slotX - xOffset, slotY - yOffset)
                    .addIngredients(VanillaTypes.ITEM_STACK, ingredients)
                    .addTooltipCallback((view, tooltips) -> {
                        if(this.useDurability && this.mode == RequirementIOMode.INPUT)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.item.durability.consume", this.amount));
                        else if(this.useDurability && this.mode == RequirementIOMode.OUTPUT)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.item.durability.repair", this.amount));

                        if(this.chance == 0)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance.0").withStyle(ChatFormatting.DARK_RED));
                        else if(this.chance != 1){
                            double percentage = this.chance * 100;
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
                        if(!this.slot.isEmpty() && this.showRequireSlot && Minecraft.getInstance().options.advancedItemTooltips)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.item.specificSlot").withStyle(ChatFormatting.DARK_RED));
                    });
            return true;
        }
        return false;
    }
}
