package fr.frinn.custommachinery.common.integration.jei.wrapper;

import fr.frinn.custommachinery.common.crafting.requirements.IRequirement;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;

import java.util.List;
import java.util.stream.Collectors;

public class ItemIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private IRequirement.MODE mode;
    private Item item;
    private int amount;
    private ITag<Item> tag;

    public ItemIngredientWrapper(IRequirement.MODE mode, Item item, int amount, ITag<Item> tag) {
        this.mode = mode;
        this.item = item;
        this.amount = amount;
        this.tag = tag;
    }

    @Override
    public IIngredientType<ItemStack> getJEIIngredientType() {
        return VanillaTypes.ITEM;
    }

    @Override
    public Object asJEIIngredient() {
        if(this.item != null && this.item != Items.AIR)
            return new ItemStack(this.item, this.amount);
        else if(this.tag != null && this.mode == IRequirement.MODE.INPUT)
            return this.tag.getAllElements().stream().map(item -> new ItemStack(item, this.amount)).collect(Collectors.toList());
        else throw new IllegalStateException("Using Item Requirement with null item and/or tag");
    }

    @Override
    public void addJeiIngredients(IIngredients ingredients) {
        if(this.item != null && this.item != Items.AIR) {
            if(this.mode == IRequirement.MODE.INPUT)
                ingredients.setInput(VanillaTypes.ITEM, new ItemStack(this.item, this.amount));
            else
                ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(this.item, this.amount));
        } else if(this.tag != null && this.mode == IRequirement.MODE.INPUT) {
            List<ItemStack> inputs = this.tag.getAllElements().stream().map(item -> new ItemStack(item, this.amount)).collect(Collectors.toList());
            ingredients.setInputs(VanillaTypes.ITEM, inputs);
        } else throw new IllegalStateException("Using Item Requirement with null item and/or tag");
    }
}
