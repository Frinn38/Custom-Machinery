package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.util.ID;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.MachineLootItemFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class CustomMachineBlockBuilderJS extends BuilderBase<Block> {

    private ResourceLocation machineID;
    private boolean occlusion;

    public CustomMachineBlockBuilderJS(ResourceLocation i) {
        super(i);
        this.machineID = id;
        this.occlusion = false;
    }

    public CustomMachineBlockBuilderJS machine(ResourceLocation machineID) {
        this.machineID = machineID;
        return this;
    }

    public CustomMachineBlockBuilderJS occlude() {
        this.occlusion = true;
        return this;
    }

    @Override
    public Block createObject() {
        CustomMachineBlock block = new CustomMachineBlock(this.occlusion);
        CustomMachinery.CUSTOM_BLOCK_MACHINES.put(this.machineID, block);
        return block;
    }

    @Override
    public void createAdditionalObjects(AdditionalObjectRegistry registry) {
        registry.add(Registries.ITEM, new ItemBuilder(this.id) {
            @Override
            public Item createObject() {
                return new CustomMachineItem(CustomMachineBlockBuilderJS.this.get(), new Item.Properties(), CustomMachineBlockBuilderJS.this.machineID);
            }

            @Override
            public void generateAssets(KubeAssetGenerator generator) {
                JsonObject json = new JsonObject();
                json.add("parent", new JsonPrimitive("custommachinery:block/custom_machine_block"));
                JsonObject defaultJson = new JsonObject();
                defaultJson.add("", new JsonPrimitive("custommachinery:default/custom_machine_default"));
                json.add("defaults", defaultJson);
                generator.json(this.id.withPath(ID.ITEM_MODEL), json);
            }
        });
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        generator.blockState(this.id, stateGenerator -> stateGenerator.simpleVariant("", CustomMachinery.rl("block/custom_machine_block")));
    }

    @Override
    public void generateData(KubeDataGenerator generator) {
        LootPool.Builder pool = LootPool.lootPool().when(ExplosionCondition.survivesExplosion()).setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(this.object.asItem()).apply(MachineLootItemFunction::new));
        LootTable table = LootTable.lootTable().withPool(pool).build();
        generator.json(id.withPath(ID.BLOCK_LOOT_TABLE), generator.getRegistries().json().withEncoder(LootTable.CODEC).apply(new Holder.Direct<>(table)).getOrThrow());
    }
}
