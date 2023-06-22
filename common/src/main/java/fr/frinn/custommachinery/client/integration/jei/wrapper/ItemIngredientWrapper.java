package fr.frinn.custommachinery.client.integration.jei.wrapper;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class ItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final RequirementIOMode mode;
    private final IIngredient<Item> item;
    private final int amount;
    private final double chance;
    private final boolean useDurability;
    @Nullable
    private final CompoundTag nbt;
    private final String slot;

    public ItemIngredientWrapper(RequirementIOMode mode, IIngredient<Item> item, int amount, double chance, boolean useDurability, @Nullable CompoundTag nbt, String slot) {
        this.mode = mode;
        this.item = item;
        this.amount = amount;
        this.chance = chance;
        this.useDurability = useDurability;
        this.nbt = nbt;
        this.slot = slot;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement slotElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        List<ItemStack> ingredients = this.item.getAll().stream().map(item -> Utils.makeItemStack(item, this.useDurability ? 1 : this.amount, this.nbt)).toList();
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        if(slotElement.getID().equals(this.slot) || template.map(t -> t.canAccept(ingredients, this.mode == RequirementIOMode.INPUT, helper.getDummyManager()) && (this.slot.isEmpty() || t.getId().equals(this.slot))).orElse(false)) {
            builder.addSlot(roleFromMode(this.mode), element.getX() - xOffset, element.getY() - yOffset)
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
                        if(!this.slot.isEmpty() && Minecraft.getInstance().options.advancedItemTooltips)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.item.specificSlot").withStyle(ChatFormatting.DARK_RED));
                    });
            return true;
        }
        return false;
    }
}
