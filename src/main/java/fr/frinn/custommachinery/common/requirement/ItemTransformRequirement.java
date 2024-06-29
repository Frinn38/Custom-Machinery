package fr.frinn.custommachinery.common.requirement;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.requirement.AbstractChanceableRequirement;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class ItemTransformRequirement extends AbstractChanceableRequirement<ItemComponentHandler> implements IJEIIngredientRequirement<ItemStack> {

    public static final NamedCodec<ItemTransformRequirement> CODEC = NamedCodec.record(itemTransformRequirementInstance ->
            itemTransformRequirementInstance.group(
                    NamedCodec.of(CraftingHelper.makeIngredientCodec(false)).fieldOf("input").forGetter(requirement -> requirement.input),
                    NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("input_amount", 1).forGetter(requirement -> requirement.inputAmount),
                    NamedCodec.STRING.optionalFieldOf("input_slot", "").forGetter(requirement -> requirement.inputSlot),
                    RegistrarCodec.ITEM.optionalFieldOf("output", Items.AIR).forGetter(requirement -> requirement.output),
                    NamedCodec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("output_amount", 1).forGetter(requirement -> requirement.outputAmount),
                    NamedCodec.STRING.optionalFieldOf("output_slot", "").forGetter(requirement -> requirement.outputSlot),
                    NamedCodec.BOOL.optionalFieldOf("copy_nbt", true).forGetter(requirement -> requirement.copyNBT),
                    NamedCodec.doubleRange(0.0D, 1.0D).optionalFieldOf("chance", 1.0D).forGetter(AbstractChanceableRequirement::getChance)
            ).apply(itemTransformRequirementInstance, (input, inputAmount, inputSlot, output, outputAmount, outputSlot, copyNBT, chance) -> {
                    ItemTransformRequirement requirement = new ItemTransformRequirement(input, inputAmount, inputSlot, output, outputAmount, outputSlot, copyNBT, null);
                    requirement.setChance(chance);
                    return requirement;
            }), "Item transform requirement"
    );

    private final Ingredient input;
    private final int inputAmount;
    private final String inputSlot;
    private final Item output;
    private final int outputAmount;
    private final String outputSlot;
    private final boolean copyNBT;
    @Nullable
    private final Function<ItemStack, ItemStack> function;

    public ItemTransformRequirement(Ingredient input, int inputAmount, String inputSlot, Item output, int outputAmount, String outputSlot, boolean copyNBT, @Nullable Function<ItemStack, ItemStack> function) {
        super(RequirementIOMode.OUTPUT);
        this.input = input;
        this.inputAmount = inputAmount;
        this.inputSlot = inputSlot;
        this.output = output;
        this.outputAmount = outputAmount;
        this.outputSlot = outputSlot;
        this.copyNBT = copyNBT;
        this.function = function;
    }

    @Override
    public RequirementType<ItemTransformRequirement> getType() {
        return Registration.ITEM_TRANSFORM_REQUIREMENT.get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component, ICraftingContext context) {
        return Arrays.stream(this.input.getItems()).anyMatch(item -> {
            if(component.getItemAmount(this.inputSlot, item) < this.inputAmount)
                return false;
            ItemStack input = component.getComponents().stream().filter(slot -> slot.getItemStack().getItem() == item.getItem()).findFirst().map(ItemMachineComponent::getItemStack).orElse(ItemStack.EMPTY);
            ItemStack output = null;
            if(this.function != null)
                output = this.function.apply(input.copy());
            else if(this.copyNBT && this.output != Items.AIR)
                output = new ItemStack(Holder.direct(this.output), 1, input.getComponentsPatch());
            return component.getSpaceForItem(this.outputSlot, output) >= this.outputAmount;
        });
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, ICraftingContext context) {
        for(ItemStack item : this.input.getItems()) {
            if(component.getItemAmount(this.inputSlot, item) < this.inputAmount)
                continue;
            ItemStack input = component.getComponents().stream().filter(slot -> slot.getItemStack().getItem() == item.getItem()).findFirst().map(ItemMachineComponent::getItemStack).orElse(ItemStack.EMPTY);
            ItemStack output = null;
            if(this.function != null)
                output = this.function.apply(input.copy());
            else if(this.copyNBT && this.output != Items.AIR)
                output = new ItemStack(Holder.direct(this.output), 1, input.getComponentsPatch());
            if(component.getSpaceForItem(this.outputSlot, output) < this.outputAmount)
                continue;
            component.removeFromInputs(this.inputSlot, item, this.inputAmount);
            component.addToOutputs(this.outputSlot, output, this.outputAmount);
            return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.item_transform.error", this.input.toString(), this.inputAmount));
    }

    @Override
    public List<IJEIIngredientWrapper<ItemStack>> getJEIIngredientWrappers(IMachineRecipe recipe) {
        return Lists.newArrayList(
                new ItemIngredientWrapper(RequirementIOMode.INPUT, new SizedIngredient(this.input, this.inputAmount), getChance(), false, this.inputSlot, true),
                new ItemIngredientWrapper(RequirementIOMode.OUTPUT, this.output == Items.AIR ? new SizedIngredient(this.input, this.inputAmount) : new SizedIngredient(Ingredient.of(this.output), this.outputAmount), getChance(), false, this.outputSlot, true)
        );
    }
}
