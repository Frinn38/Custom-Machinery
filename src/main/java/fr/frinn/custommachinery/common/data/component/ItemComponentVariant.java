package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;

import java.util.HashMap;
import java.util.Map;

public abstract class ItemComponentVariant {

    private static Map<ResourceLocation, ItemComponentVariant> variants = new HashMap<>();

    public static final ItemComponentVariant DEFAULT = register(new ItemComponentVariant.Default());
    public static final ItemComponentVariant FUEL = register(new ItemComponentVariant.Fuel());
    public static final ItemComponentVariant UPGRADE = register(new ItemComponentVariant.Upgrade());

    public static ItemComponentVariant register(ItemComponentVariant variant) {
        variants.put(variant.getId(), variant);
        return variant;
    }

    public static ItemComponentVariant getVariant(ResourceLocation id) {
        return variants.get(id);
    }

    public abstract ResourceLocation getId();

    public void tick(ItemMachineComponent component) {}

    public boolean isItemValid(ItemMachineComponent component, ItemStack stack) {return true;}

    private static class Default extends ItemComponentVariant {

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(CustomMachinery.MODID, "default");
        }

    }

    private static class Fuel extends ItemComponentVariant {

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(CustomMachinery.MODID, "fuel");
        }

        @Override
        public boolean isItemValid(ItemMachineComponent component, ItemStack stack) {
            return ForgeHooks.getBurnTime(stack, IRecipeType.SMELTING) > 0;
        }
    }

    private static class Upgrade extends ItemComponentVariant {

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(CustomMachinery.MODID, "upgrade");
        }

        @Override
        public boolean isItemValid(ItemMachineComponent component, ItemStack stack) {
            return CustomMachinery.UPGRADES.stream().anyMatch(upgrade -> upgrade.getItem() == stack.getItem());
        }
    }
}
