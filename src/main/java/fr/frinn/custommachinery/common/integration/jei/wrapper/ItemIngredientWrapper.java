package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.data.gui.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.RecipeHelper;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final IRequirement.MODE mode;
    private final IIngredient<Item> item;
    private final int amount;
    private final double chance;
    private final boolean useDurability;
    private final CompoundNBT nbt;
    private final String slot;

    public ItemIngredientWrapper(IRequirement.MODE mode, IIngredient<Item> item, int amount, double chance, boolean useDurability, @Nullable CompoundNBT nbt, String slot) {
        this.mode = mode;
        this.item = item;
        this.amount = amount;
        this.chance = chance;
        this.useDurability = useDurability;
        this.nbt = nbt == null ? new CompoundNBT() : nbt.copy();
        this.slot = slot;
    }

    @Override
    public IIngredientType<ItemStack> getJEIIngredientType() {
        return VanillaTypes.ITEM;
    }

    public Object asJEIIngredient() {
        List<ItemStack> stacks = this.item.getAll().stream().map(item -> new ItemStack(item, this.amount)).collect(Collectors.toList());
        stacks.forEach(stack -> {
            stack.setTag(this.nbt.copy());
            if(this.useDurability) {
                stack.setCount(1);
                if(this.mode == IRequirement.MODE.INPUT)
                    stack.getOrCreateChildTag(CustomMachinery.MODID).putInt("consumeDurability", this.amount);
                else if(this.mode == IRequirement.MODE.OUTPUT)
                    stack.getOrCreateChildTag(CustomMachinery.MODID).putInt("repairDurability", this.amount);
            }
            if(this.chance != 1.0D)
                stack.getOrCreateChildTag(CustomMachinery.MODID).putDouble("chance", this.chance);
            if(!this.slot.isEmpty())
                stack.getOrCreateChildTag(CustomMachinery.MODID).putBoolean("specificSlot", true);
        });
        return stacks;
    }

    @Override
    public void setIngredient(IIngredients ingredients) {
        List<ItemStack> items = this.item.getAll().stream().map(item -> Utils.makeItemStack(item, this.amount, this.nbt)).collect(Collectors.toList());
        if(this.mode == IRequirement.MODE.INPUT)
            ingredients.setInputs(VanillaTypes.ITEM, items);
        else
            ingredients.setOutputs(VanillaTypes.ITEM, items);
    }

    @Override
    public boolean setupRecipe(int index, IRecipeLayout layout, IGuiElement element, RecipeHelper helper) {
        if(!(element instanceof SlotGuiElement) || element.getType() != Registration.SLOT_GUI_ELEMENT.get())
            return false;

        List<ItemStack> ingredients = this.item.getAll().stream().map(item -> Utils.makeItemStack(item, this.useDurability ? 1 : this.amount, this.nbt)).collect(Collectors.toList());
        SlotGuiElement slotElement = (SlotGuiElement)element;
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        if(template.map(t -> t.canAccept(ingredients, this.mode == IRequirement.MODE.INPUT, helper.getDummyManager())).orElse(false)) {
            IGuiIngredientGroup<ItemStack> group = layout.getIngredientsGroup(VanillaTypes.ITEM);
            group.set(index, ingredients);
            group.addTooltipCallback(((slotIndex, input, ingredient, tooltips) -> {
                if(slotIndex != index)
                    return;
                if(this.useDurability && this.mode == IRequirement.MODE.INPUT)
                    tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.item.durability.consume", this.amount));
                else if(this.useDurability && this.mode == IRequirement.MODE.OUTPUT)
                    tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.item.durability.repair", this.amount));

                if(this.chance == 0)
                    tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.chance.0").mergeStyle(TextFormatting.DARK_RED));
                else if(this.chance != 1){
                    double percentage = this.chance * 100;
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
                if(!this.slot.isEmpty() && Minecraft.getInstance().gameSettings.advancedItemTooltips)
                    tooltips.add(new TranslationTextComponent("custommachinery.jei.ingredient.item.specificSlot").mergeStyle(TextFormatting.DARK_RED));
            }));
            return true;
        }
        return false;
    }
}
