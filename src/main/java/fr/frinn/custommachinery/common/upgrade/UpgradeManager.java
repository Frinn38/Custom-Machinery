package fr.frinn.custommachinery.common.upgrade;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.upgrade.IMachineUpgradeManager;
import fr.frinn.custommachinery.api.upgrade.IRecipeModifier;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.UpgradedCustomMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UpgradeManager implements IMachineUpgradeManager {

    private final CustomMachineTile tile;
    private final List<Pair<IRecipeModifier, Integer>> activeRecipeModifiers = new ArrayList<>();
    private final List<UpgradeableComponentValue> activeModifiedValues = new ArrayList<>();

    private boolean recipeModifiersDirty = true;

    public UpgradeManager(CustomMachineTile tile) {
        this.tile = tile;
    }

    @Override
    public void refresh() {
        //Refresh only on the server side
        if(this.tile.getLevel() == null || this.tile.getLevel().isClientSide())
            return;
        
        //Recipe modifiers can be refreshed only when a recipe need them
        this.recipeModifiersDirty = true;

        //But component modifiers must be refreshed immediately
        this.refreshComponentModifiers();
    }

    private void refreshComponentModifiers() {
        List<UpgradeableComponentValue> actives = new ArrayList<>(this.activeModifiedValues);
        this.activeModifiedValues.clear();
        this.getCurrentUpgrades().forEach(pair -> {
            pair.getFirst().components().forEach(modifier -> {
                this.tile.getComponentManager().getUpgradeableComponentValue(modifier.component(), modifier.id(), modifier.target())
                        .ifPresent(value -> {
                            actives.remove(value);
                            this.activeModifiedValues.add(value);
                            value.refresh(modifier, pair.getSecond());
                        });
            });
        });
        //Values that were active but now aren't must be reset.
        actives.forEach(value -> value.refresh(null, 0));

        //Then refresh processor core count
        if(this.tile.getProcessor() instanceof MachineProcessor machineProcessor)
            machineProcessor.refreshCoreCount(this.getCurrentUpgrades()
                    .flatMap(pair -> pair
                            .getFirst()
                            .coreModifier()
                            .stream()
                            .map(component -> Pair.of(component, pair.getSecond()))
                    ));
    }

    private void refreshRecipeModifiers() {
        this.recipeModifiersDirty = false;
        this.activeRecipeModifiers.clear();
        if(this.tile.getMachine() instanceof UpgradedCustomMachine upgradedMachine)
            upgradedMachine.getModifiers().forEach(modifier -> this.activeRecipeModifiers.add(Pair.of(modifier, 1)));
        this.activeRecipeModifiers.addAll(this.getCurrentUpgrades()
                .flatMap(pair -> pair.getFirst().recipeModifiers()
                        .stream()
                        .map(modifier -> Pair.of((IRecipeModifier)modifier, pair.getSecond()))
                ).toList());
    }

    @Override
    public Stream<Pair<MachineUpgrade, Integer>> getCurrentUpgrades() {
        return this.tile.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                .stream()
                .flatMap(handler -> handler.getComponents().stream())
                .filter(slot -> slot.getType() == Registration.ITEM_UPGRADE_MACHINE_COMPONENT.get() && !slot.getItemStack().isEmpty())
                .flatMap(slot -> CustomMachinery.UPGRADES.getUpgradesForItemAndMachine(slot.getItemStack().getItem(), this.tile.getMachine().getId())
                        .stream()
                        .map(upgrade -> Pair.of(upgrade, Math.min(slot.getItemStack().getCount(), upgrade.max())))
                );
    }

    @Override
    public List<Pair<IRecipeModifier, Integer>> getRecipeModifiers() {
        if(this.recipeModifiersDirty)
            refreshRecipeModifiers();
        return this.activeRecipeModifiers;
    }
}
