package fr.frinn.custommachinery.client.integration.jei.wrapper;

import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.integration.jei.IRecipeHelper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.CMRegistration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final RequirementIOMode mode;
    @Nullable
    private final SizedIngredient ingredient;
    @Nullable
    private final ItemStack output;
    private final double chance;
    private final boolean useDurability;
    private final List<String> slots;
    private final boolean showRequireSlot;

    public ItemIngredientWrapper(RequirementIOMode mode, SizedIngredient ingredient, double chance, boolean useDurability, List<String> slots, boolean showRequireSlot) {
        this.mode = mode;
        this.ingredient = ingredient;
        this.output = null;
        this.chance = chance;
        this.useDurability = useDurability;
        this.slots = slots;
        this.showRequireSlot = showRequireSlot;
    }

    public ItemIngredientWrapper(RequirementIOMode mode, ItemStack output, double chance, boolean useDurability, List<String> slots, boolean showRequireSlot) {
        this.mode = mode;
        this.ingredient = null;
        this.output = output;
        this.chance = chance;
        this.useDurability = useDurability;
        this.slots = slots;
        this.showRequireSlot = showRequireSlot;
    }

    @Override
    public boolean setupRecipe(IRecipeLayoutBuilder builder, int xOffset, int yOffset, IGuiElement element, IRecipeHelper helper) {
        if(!(element instanceof SlotGuiElement slotElement) || element.getType() != CMRegistration.SLOT_GUI_ELEMENT.get())
            return false;

        List<ItemStack> ingredients;
        if(this.ingredient != null)
            ingredients = this.ingredient.ingredient().display().resolveForStacks(ContextMap.EMPTY).stream().map(item -> item.copyWithCount(this.ingredient.count())).collect(Collectors.toCollection(ArrayList::new));
        else if(this.output != null)
            ingredients = List.of(this.output.copy());
        else
            ingredients = Collections.emptyList();
        Optional<IMachineComponentTemplate<?>> template = helper.getComponentForElement(slotElement);
        if(this.slots.contains(slotElement.getComponentId().uniqueId()) || template.map(t -> t.getType() != CMRegistration.ITEM_FILTER_MACHINE_COMPONENT.get() && t.canAccept(ingredients, this.mode == RequirementIOMode.INPUT, helper.getDummyManager()) && (this.slots.isEmpty() || this.slots.contains(t.getId()))).orElse(false)) {
            int slotX = element.getX() + (element.getWidth() - 16) / 2;
            int slotY = element.getY() + (element.getHeight() - 16) / 2;
            builder.addSlot(roleFromMode(this.mode), slotX - xOffset, slotY - yOffset)
                    .addIngredients(VanillaTypes.ITEM_STACK, ingredients)
                    .setSlotName(slotElement.getComponentId().uniqueId())
                    .addRichTooltipCallback((_, tooltips) -> {
                        int count = 0;
                        if(this.ingredient != null)
                            count = this.ingredient.count();
                        else if(this.output != null)
                            count = this.output.count();
                        if(this.useDurability && this.mode == RequirementIOMode.INPUT)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.item.durability.consume", count));
                        else if(this.useDurability && this.mode == RequirementIOMode.OUTPUT)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.item.durability.repair", count));

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
                        if(!this.slots.isEmpty() && this.slots.contains(slotElement.getComponentId().uniqueId()) && this.showRequireSlot && Minecraft.getInstance().options.advancedItemTooltips)
                            tooltips.add(Component.translatable("custommachinery.jei.ingredient.item.specificSlot").withStyle(ChatFormatting.DARK_RED));
                    });
            return true;
        }
        return false;
    }
}
