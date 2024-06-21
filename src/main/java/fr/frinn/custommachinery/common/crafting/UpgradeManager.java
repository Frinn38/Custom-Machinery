package fr.frinn.custommachinery.common.crafting;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.upgrade.IMachineUpgradeManager;
import fr.frinn.custommachinery.api.upgrade.IRecipeModifier;
import fr.frinn.custommachinery.common.component.variant.item.UpgradeItemComponentVariant;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.UpgradedCustomMachine;

import java.util.ArrayList;
import java.util.List;

public class UpgradeManager implements IMachineUpgradeManager {

    private final CustomMachineTile tile;
    private final List<Pair<IRecipeModifier, Integer>> activeModifiers = new ArrayList<>();

    private boolean isDirty = true;

    public UpgradeManager(CustomMachineTile tile) {
        this.tile = tile;
    }

    @Override
    public void markDirty() {
        this.isDirty = true;
    }

    private void refreshUpgrades() {
        this.isDirty = false;
        this.activeModifiers.clear();
        if(this.tile.getMachine() instanceof UpgradedCustomMachine upgradedMachine)
            upgradedMachine.getModifiers().forEach(modifier -> this.activeModifiers.add(Pair.of(modifier, 1)));
        this.activeModifiers.addAll(tile.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .stream()
                .flatMap(handler -> handler.getComponents().stream())
                .filter(slot -> slot.getVariant() == UpgradeItemComponentVariant.INSTANCE && !slot.getItemStack().isEmpty())
                .flatMap(slot -> CustomMachinery.UPGRADES.getUpgradesForItemAndMachine(slot.getItemStack().getItem(), this.tile.getMachine().getId())
                        .stream()
                        .flatMap(upgrade -> upgrade.getModifiers().stream().map(modifier -> Pair.of((IRecipeModifier)modifier, Math.min(slot.getItemStack().getCount(), upgrade.getMaxAmount()))))
                ).toList());
    }

    @Override
    public List<Pair<IRecipeModifier, Integer>> getAllModifiers() {
        if(this.isDirty)
            refreshUpgrades();
        return this.activeModifiers;
    }
}
