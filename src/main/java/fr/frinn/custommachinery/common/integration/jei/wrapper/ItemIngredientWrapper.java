package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private IRequirement.MODE mode;
    private IIngredient<Item> item;
    private int amount;
    private double chance;
    private boolean useDurability;
    private CompoundNBT nbt;
    private String slot;

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

    @Override
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
    public List<ItemStack> getJeiIngredients() {
        return this.item.getAll().stream().map(item -> {
            ItemStack stack = new ItemStack(item, this.amount);
            stack.setTag(this.nbt.copy());
            return stack;
        }).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public String getComponentID() {
        return this.slot;
    }
}
