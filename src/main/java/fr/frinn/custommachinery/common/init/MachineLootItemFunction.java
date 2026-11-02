package fr.frinn.custommachinery.common.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class MachineLootItemFunction implements LootItemFunction {

    public static final MapCodec<MachineLootItemFunction> CODEC = MapCodec.unit(new MachineLootItemFunction());

    @Override
    public MapCodec<MachineLootItemFunction> codec() {
        return CMRegistration.MACHINE_LOOT_ITEM_FUNCTION.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        if(lootContext.getParameter(LootContextParams.BLOCK_ENTITY) instanceof CustomMachineTile machine) {
            //Set machine id to nbt
            if(stack.getItem() == CMRegistration.CUSTOM_MACHINE_ITEM.get())
                stack.set(CMRegistration.MACHINE_DATA, machine.getId());

            //Save machine inventory to nbt
            if(itemStack.getItem() instanceof CustomMachineItem && machine.getAppearance().shouldKeepInventory()) {
                TagValueOutput componentMManagerOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lootContext.getLevel().registryAccess());
                machine.getComponentManager().serialize(componentMManagerOutput);
                stack.set(CMRegistration.MACHINE_INVENTORY_DATA, componentMManagerOutput.buildResult());
            }

            //Save machine durability to nbt
            machine.getComponentManager().getComponent(Registration.DURABILITY_MACHINE_COMPONENT.get()).ifPresent(component -> {
                if(component.shouldShowItemDurability()) {
                    stack.set(DataComponents.MAX_DAMAGE, component.getMaxDurability());
                    stack.set(DataComponents.DAMAGE, component.getMaxDurability() - component.getDurability());
                }
            });
        }
        return stack;
    }
}
