package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.requirement.ItemTransformRequirement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;
import org.openzen.zencode.java.ZenCodeType.OptionalInt;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

import java.util.function.Function;

@ZenRegister
@Name(CTConstants.REQUIREMENT_ITEM_TRANSFORM)
public interface ItemTransformRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T transformItem(IIngredient ingredient, @OptionalInt(1) int amount, @Optional IItemStack output, @OptionalString String inputSlot, @OptionalString String outputSlot, @Optional Function<IItemStack, IItemStack> nbt) {
        Item outputItem = output == null ? Items.AIR : output.getDefinition();
        int outputAmount = output == null ? 1 : output.amount();
        return addRequirement(new ItemTransformRequirement(ingredient.asVanillaIngredient(), amount, inputSlot, outputItem, outputAmount, outputSlot, true, new NbtTransformer(nbt)));
    }

    class NbtTransformer implements Function<ItemStack, ItemStack> {

        private final Function<IItemStack, IItemStack> function;

        public NbtTransformer(Function<IItemStack, IItemStack> function) {
            this.function = function;
        }

        @Override
        public ItemStack apply(@Nullable ItemStack stack) {
            return this.function.apply(IItemStack.of(stack)).getInternal();
        }
    }
}
