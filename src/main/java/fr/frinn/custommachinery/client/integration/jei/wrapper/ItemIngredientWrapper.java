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
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final RequirementIOMode mode;
    private final SizedIngredient ingredient;
    private final double chance;
    private final boolean useDurability;
    private final String slot;
    private final boolean showRequireSlot;

    public ItemIngredientWrapper(RequirementIOMode mode, SizedIngredient ingredient, double chance, boolean useDurability, String slot, boolean showRequireSlot) {
        this.mode = mode;
        this.ingredient = ingredient;
        this.chance = chance;
        this.useDurability = useDurability;
        this.slot = slot;
        this.showRequireSlot = showRequireSlot;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement slotElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        List<ItemStack> ingredients = Arrays.stream(this.ingredient.ingredient().getItems()).map(item -> item.copyWithCount(this.ingredient.count())).collect(Collectors.toCollection(ArrayList::new));
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        if(slotElement.getComponentId().equals(this.slot) || template.map(t -> t.canAccept(ingredients, this.mode == RequirementIOMode.INPUT, helper.getDummyManager()) && (this.slot.isEmpty() || t.getId().equals(this.slot))).orElse(false)) {
            int slotX = element.getX() + (element.getWidth() - 16) / 2;
            int slotY = element.getY() + (element.getHeight() - 16) / 2;
            builder.addSlot(roleFromMode(this.mode), slotX - xOffset, slotY - yOffset)
                    .addIngredients(VanillaTypes.ITEM_STACK, ingredients)
                    .setSlotName(slotElement.getComponentId())
                    .addTooltipCallback((view, tooltips) -> {
                        if(this.useDurability && this.mode == RequirementIOMode.INPUT)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.item.durability.consume", this.ingredient.count()));
                        else if(this.useDurability && this.mode == RequirementIOMode.OUTPUT)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.item.durability.repair", this.ingredient.count()));

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
