package fr.frinn.custommachinery.common.integration.jei.wrapper;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class LootTableIngredientWrapper implements IJEIIngredientWrapper<ItemStack> {

    private final ResourceLocation lootTable;

    public LootTableIngredientWrapper(ResourceLocation lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public IIngredientType<ItemStack> getJEIIngredientType() {
        return VanillaTypes.ITEM;
    }

    @Override
    public Object asJEIIngredient() {
        return LootTableHelper.getLootsForTable(this.lootTable).stream().map(pair -> {
            ItemStack stack = pair.getFirst().copy();
            stack.getOrCreateChildTag(CustomMachinery.MODID).putDouble("chance", pair.getSecond());
            return stack;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemStack> getJeiIngredients() {
        return LootTableHelper.getLootsForTable(this.lootTable).stream().map(Pair::getFirst).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public String getComponentID() {
        return "";
    }
}
