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
import fr.frinn.custommachinery.client.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface ItemRequirement extends IRequirement<ItemComponentHandler>, IJEIIngredientRequirement<ItemStack> {

    NamedCodec<ItemRequirement> CODEC = NamedCodec.record(itemRequirementInstance ->
            itemRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(ItemRequirement::getMode),
                    NamedCodec.of(SizedIngredient.NESTED_CODEC).optionalFieldOf("ingredient").aliases("item").forGetter(requirement -> requirement instanceof Input input ? Optional.of(input.ingredient) : Optional.empty()),
                    DefaultCodecs.ITEM_OR_STACK.optionalFieldOf("output").forGetter(requirement -> requirement instanceof Output output ? Optional.of(output.output) : Optional.empty()),
                    NamedCodec.STRING.listOf().optionalFieldOf("slots", Collections.emptyList()).aliases("slot").forGetter(ItemRequirement::slots),
                    NamedCodec.BOOL.optionalFieldOf("consume_on_end", false).forGetter(requirement -> requirement instanceof Input input && input.consumeOnEnd)
            ).apply(itemRequirementInstance, (mode, ingredient, output, slots, end) -> {
                if(mode == RequirementIOMode.INPUT) {
                    if(ingredient.isPresent())
                        return new Input(ingredient.get(), slots, end);
                    throw new IllegalArgumentException("Item requirement in input mode must use ingredient");
                } else {
                    if(output.isPresent())
                        return new Output(output.get(), slots);
                    throw new IllegalArgumentException("Item requirement in output mode must use itemstack");
                }
            }), "Item requirement"
    );

    @Override
    default RequirementType<ItemRequirement> getType() {
        return CMRegistration.ITEM_REQUIREMENT.get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default MachineComponentType getComponentType() {
        return CMRegistration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    default RequirementIOMode getMode() {
        return this instanceof Input ? RequirementIOMode.INPUT : RequirementIOMode.OUTPUT;
    }

    List<String> slots();

    record Input(SizedIngredient ingredient, List<String> slots, boolean consumeOnEnd) implements ItemRequirement {

        @Override
        public boolean test(ItemComponentHandler component, ICraftingContext context) {
            int amount = (int)context.getIntegerModifiedValue(this.ingredient.count(), this, null);
            return component.getIngredientAmount(this.slots, this.ingredient.ingredient()) >= amount;
        }

        @Override
        public void gatherRequirements(IRequirementList<ItemComponentHandler> list) {
            if(this.consumeOnEnd) {
                list.inventoryCondition(this::checkInputs);
                list.processOnEnd(this::processInputs);
            } else
                list.processOnStart(this::processInputs);
        }

        private CraftingResult checkInputs(ItemComponentHandler component, ICraftingContext context) {
            int amount = (int)context.getIntegerModifiedValue(this.ingredient.count(), this, null);
            int maxExtract = component.getIngredientAmount(this.slots, this.ingredient.ingredient());
            if(maxExtract >= amount) {
                return CraftingResult.success();
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.item.error.input", Utils.itemIngredientName(this.ingredient), amount, maxExtract));
        }

        private CraftingResult processInputs(ItemComponentHandler component, ICraftingContext context) {
            int amount = (int)context.getIntegerModifiedValue(this.ingredient.count(), this, null);
            int maxExtract = component.getIngredientAmount(this.slots, this.ingredient.ingredient());
            if(maxExtract >= amount) {
                component.removeFromInputs(this.slots, this.ingredient.ingredient(), amount);
                return CraftingResult.success();
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.item.error.input", Utils.itemIngredientName(this.ingredient), amount, maxExtract));
        }

        @Override
        public List<IJEIIngredientWrapper<ItemStack>> getJEIIngredientWrappers(IMachineRecipe recipe, RecipeRequirement<?, ?> requirement) {
            return Collections.singletonList(new ItemIngredientWrapper(this.getMode(), this.ingredient, requirement.chance(), false, this.slots, true));
        }
    }

    record Output(ItemStack output, List<String> slots) implements ItemRequirement {

        @Override
        public boolean test(ItemComponentHandler component, ICraftingContext context) {
            int amount = (int)context.getIntegerModifiedValue(this.output.count(), this, null);
            return component.getSpaceForItem(this.slots, this.output) >= amount;
        }

        @Override
        public void gatherRequirements(IRequirementList<ItemComponentHandler> list) {
            list.processOnEnd(((component, context) -> {
                int amount = (int)context.getIntegerModifiedValue(this.output.count(), this, null);
                int canInsert = component.getSpaceForItem(this.slots, this.output);
                if(canInsert >= amount) {
                    component.addToOutputs(this.slots, this.output.copy(), amount);
                    return CraftingResult.success();
                }
                return CraftingResult.error(Component.translatable("custommachinery.requirements.item.error.output", amount, this.output.getHoverName()));
            }));
        }

        @Override
        public List<IJEIIngredientWrapper<ItemStack>> getJEIIngredientWrappers(IMachineRecipe recipe, RecipeRequirement<?, ?> requirement) {
            return Collections.singletonList(new ItemIngredientWrapper(this.getMode(), this.output, requirement.chance(), false, this.slots, true));
        }
    }
}
