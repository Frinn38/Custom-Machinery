package fr.frinn.custommachinery.common.init;

import com.mojang.serialization.MapCodec;
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
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        if(lootContext.getParameter(LootContextParams.BLOCK_ENTITY) instanceof CustomMachineTile machine) {
            if(itemStack.getItem() == CMRegistration.CUSTOM_MACHINE_ITEM.get())
                itemStack.set(CMRegistration.MACHINE_DATA, machine.getId());
            if(itemStack.getItem() instanceof CustomMachineItem && machine.getAppearance().shouldKeepInventory()) {
                TagValueOutput componentMManagerOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lootContext.getLevel().registryAccess());
                machine.getComponentManager().serialize(componentMManagerOutput);
                itemStack.set(CMRegistration.MACHINE_INVENTORY_DATA, componentMManagerOutput.buildResult());
            }
        }
        return itemStack;
    }
}
