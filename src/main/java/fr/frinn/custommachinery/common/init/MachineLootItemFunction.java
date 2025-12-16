package fr.frinn.custommachinery.common.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class MachineLootItemFunction implements LootItemFunction {

    public static final MapCodec<MachineLootItemFunction> CODEC = MapCodec.unit(new MachineLootItemFunction());

    @Override
    public LootItemFunctionType<MachineLootItemFunction> getType() {
        return Registration.MACHINE_LOOT_ITEM_FUNCTION.get();
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        if(lootContext.getParam(LootContextParams.BLOCK_ENTITY) instanceof CustomMachineTile machine) {
            if(itemStack.getItem() == Registration.CUSTOM_MACHINE_ITEM.get())
                itemStack.set(Registration.MACHINE_DATA, machine.getId());
            if(itemStack.getItem() instanceof CustomMachineItem && machine.getAppearance().shouldKeepInventory())
                itemStack.set(Registration.MACHINE_INVENTORY_DATA, machine.getComponentManager().serializeNBT(lootContext.getLevel().registryAccess()));
        }
        return itemStack;
    }
}
