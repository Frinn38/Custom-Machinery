package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private IRequirement.MODE mode;
    private Item item;
    private int amount;
    private ITag<Item> tag;
    private double chance;
    private boolean useDurability;
    private CompoundNBT nbt;
    private String slot;

    public ItemIngredientWrapper(IRequirement.MODE mode, Item item, int amount, ITag<Item> tag, double chance, boolean useDurability, @Nullable CompoundNBT nbt, String slot) {
        this.mode = mode;
        this.item = item;
        this.amount = amount;
        this.tag = tag;
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
        if(this.item != null && this.item != Items.AIR) {
            ItemStack stack = new ItemStack(this.item);
            stack.setTag(this.nbt.copy());
            if(this.useDurability) {
                if(this.mode == IRequirement.MODE.INPUT)
                    stack.getOrCreateChildTag(CustomMachinery.MODID).putInt("consumeDurability", this.amount);
                else if(this.mode == IRequirement.MODE.OUTPUT)
                    stack.getOrCreateChildTag(CustomMachinery.MODID).putInt("repairDurability", this.amount);
            }
            if(this.chance != 1.0D)
                stack.getOrCreateChildTag(CustomMachinery.MODID).putDouble("chance", this.chance);
            if(!this.slot.isEmpty())
                stack.getOrCreateChildTag(CustomMachinery.MODID).putBoolean("specificSlot", true);
            return stack;
        }
        else if(this.tag != null && this.mode == IRequirement.MODE.INPUT) {
            List<ItemStack> stacks = this.tag.getAllElements().stream().map(item -> new ItemStack(item, this.amount)).collect(Collectors.toList());
            stacks.forEach(stack -> {
                stack.setTag(this.nbt.copy());
                if(this.useDurability) {
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
        else throw new IllegalStateException("Using Item Requirement with null item and/or tag");
    }

    @Override
    public List<ItemStack> getJeiIngredients() {
        if(this.useDurability && this.item != null && this.item != Items.AIR) {
            ItemStack stack = new ItemStack(this.item);
            stack.setTag(this.nbt.copy());
            return Collections.singletonList(stack);
        }
        else if(this.item != null && this.item != Items.AIR) {
            ItemStack stack = new ItemStack(this.item, this.amount);
            stack.setTag(this.nbt.copy());
            return Collections.singletonList(stack);
        }
        else if(this.tag != null && this.mode == IRequirement.MODE.INPUT)
            return this.tag.getAllElements().stream().map(item -> {
                ItemStack stack = new ItemStack(item, this.amount);
                stack.setTag(this.nbt.copy());
                return stack;
            }).collect(Collectors.toList());
        else throw new IllegalStateException("Using Item Requirement with null item and/or tag");
    }

    @Nonnull
    @Override
    public String getComponentID() {
        return this.slot;
    }
}
