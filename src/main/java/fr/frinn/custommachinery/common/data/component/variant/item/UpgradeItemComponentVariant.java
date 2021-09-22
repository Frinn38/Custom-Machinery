package fr.frinn.custommachinery.common.data.component.variant.item;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.components.IMachineComponentManager;
import fr.frinn.custommachinery.impl.component.variant.ItemComponentVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class UpgradeItemComponentVariant extends ItemComponentVariant {

    public static final UpgradeItemComponentVariant INSTANCE = new UpgradeItemComponentVariant();
    private static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "upgrade");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void tick(IMachineComponentManager manager) {

    }

    @Override
    public boolean isItemValid(IMachineComponentManager manager, ItemStack stack) {
        return CustomMachinery.UPGRADES.stream().anyMatch(upgrade -> upgrade.getItem() == stack.getItem() && upgrade.getMachines().contains(manager.getTile().getMachine().getId()));
    }
}
