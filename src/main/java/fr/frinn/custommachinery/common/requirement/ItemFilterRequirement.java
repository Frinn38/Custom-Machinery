package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.ItemFilterIngredientWrapper;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.List;

public record ItemFilterRequirement(Ingredient ingredient, String slot) implements IRequirement<ItemComponentHandler>, IJEIIngredientRequirement<ItemStack> {

    public static final NamedCodec<ItemFilterRequirement> CODEC = NamedCodec.record(itemFilterRequirementInstance ->
            itemFilterRequirementInstance.group(
                    DefaultCodecs.INGREDIENT.fieldOf("ingredient").aliases("item").forGetter(requirement -> requirement.ingredient),
                    NamedCodec.STRING.optionalFieldOf("slot", "").forGetter(requirement -> requirement.slot)
            ).apply(itemFilterRequirementInstance, ItemFilterRequirement::new), "Item filter requirement"
    );

    @Override
    public RequirementType<ItemFilterRequirement> getType() {
        return Registration.ITEM_FILTER_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(ItemComponentHandler handler, ICraftingContext context) {
        return handler.getComponents().stream()
                .filter(component -> component.getType() == Registration.ITEM_FILTER_MACHINE_COMPONENT.get())
                .anyMatch(component -> this.ingredient.test(component.getItemStack()));
    }

    @Override
    public void gatherRequirements(IRequirementList<ItemComponentHandler> list) {
        list.inventoryCondition(this::condition);
    }

    public CraftingResult condition(ItemComponentHandler component, ICraftingContext context) {
        if(test(component, context))
            return CraftingResult.success();
        return CraftingResult.error(Component.translatable("custommachinery.requirements.item_filter.error"));
    }

    @Override
    public List<IJEIIngredientWrapper<ItemStack>> getJEIIngredientWrappers(IMachineRecipe recipe, RecipeRequirement<?, ?> requirement) {
        return Collections.singletonList(new ItemFilterIngredientWrapper(this.ingredient, this.slot));
    }
}
