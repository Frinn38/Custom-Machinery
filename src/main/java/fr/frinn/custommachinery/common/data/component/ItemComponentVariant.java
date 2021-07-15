package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.item.ItemStack;
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

    public abstract void tick(ItemMachineComponent component);

    public abstract boolean isItemValid(ItemMachineComponent component, ItemStack stack);

    private static class Default extends ItemComponentVariant {

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(CustomMachinery.MODID, "default");
        }

        @Override
        public void tick(ItemMachineComponent component) {

        }

        @Override
        public boolean isItemValid(ItemMachineComponent component, ItemStack stack) {
            return true;
        }
    }

    private static class Fuel extends ItemComponentVariant {

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(CustomMachinery.MODID, "fuel");
        }

        @Override
        public void tick(ItemMachineComponent component) {
            if(component.getItemStack() != ItemStack.EMPTY && ForgeHooks.getBurnTime(component.getItemStack()) > 0) {
                if(component.getManager().getComponent(Registration.FUEL_MACHINE_COMPONENT.get()).map(FuelMachineComponent::getFuel).orElse(0) == 0 && ((CustomMachineTile)component.getManager().getTile()).craftingManager.getStatus() != CraftingManager.STATUS.IDLE) {
                    component.getManager().getComponent(Registration.FUEL_MACHINE_COMPONENT.get()).ifPresent(fuelComponent -> {
                        fuelComponent.addFuel(ForgeHooks.getBurnTime(component.getItemStack()));
                        component.extract(1);
                    });
                }
            }
        }

        @Override
        public boolean isItemValid(ItemMachineComponent component, ItemStack stack) {
            return ForgeHooks.getBurnTime(stack) > 0;
        }
    }

    private static class Upgrade extends ItemComponentVariant {

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(CustomMachinery.MODID, "upgrade");
        }

        @Override
        public void tick(ItemMachineComponent component) {

        }

        @Override
        public boolean isItemValid(ItemMachineComponent component, ItemStack stack) {
            return CustomMachinery.UPGRADES.stream().anyMatch(upgrade -> upgrade.getItem() == stack.getItem());
        }
    }
}
