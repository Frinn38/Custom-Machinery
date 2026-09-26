package fr.frinn.custommachinery.common.component.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.util.Filter;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class UpgradeItemMachineComponent extends ItemMachineComponent {

    public UpgradeItemMachineComponent(IMachineComponentManager manager, ComponentIOMode mode, String id, int capacity, int maxInput, int maxOutput, Filter<Item> filter, IOSideConfig.Template configTemplate, boolean locked) {
        super(manager, mode, id, capacity, maxInput, maxOutput, filter, configTemplate, locked);
    }

    @Override
    public MachineComponentType<ItemMachineComponent> getType() {
        return CMRegistration.ITEM_UPGRADE_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return super.isValid(index, resource) && !CustomMachinery.UPGRADES.getUpgradesForItemAndMachine(resource.getItem(), this.getManager().getTile().getMachine().getId()).isEmpty();
    }

    @Override
    public void setItemStack(ItemStack stack) {
        ItemStack prevStack = this.getItemStack();
        super.setItemStack(stack);
        if(!ItemStack.isSameItemSameComponents(prevStack, stack) || prevStack.getCount() != stack.getCount())
            this.getManager().getTile().getUpgradeManager().refresh();
    }

    public static class Template extends ItemMachineComponent.Template {

        public static final NamedCodec<Template> CODEC = defaultCodec(Template::new, "Upgrade item machine component");

        public Template(String id, ComponentIOMode mode, int capacity, int maxInput, int maxOutput, Filter<Item> filter, IOSideConfig.Template config, boolean locked) {
            super(id, mode, capacity, maxInput, maxOutput, filter, config, locked);
        }

        @Override
        public MachineComponentType<ItemMachineComponent> getType() {
            return CMRegistration.ITEM_UPGRADE_MACHINE_COMPONENT.get();
        }

        @Override
        public boolean isItemValid(IMachineComponentManager manager, ItemStack stack) {
            return !CustomMachinery.UPGRADES.getUpgradesForItemAndMachine(stack.getItem(), manager.getTile().getMachine().getId()).isEmpty();
        }

        @Override
        public ItemMachineComponent build(IMachineComponentManager manager) {
            return new UpgradeItemMachineComponent(manager, this.mode, this.id, this.capacity, this.maxInput, this.maxOutput, this.filter, this.config, this.locked);
        }
    }
}
