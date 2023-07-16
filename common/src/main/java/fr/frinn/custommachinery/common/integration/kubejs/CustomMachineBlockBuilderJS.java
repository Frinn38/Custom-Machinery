package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.PlatformHelper;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class CustomMachineBlockBuilderJS extends BuilderBase<Block> {

    private ResourceLocation machineID;

    public CustomMachineBlockBuilderJS(ResourceLocation i) {
        super(i);
        this.machineID = id;
    }

    public CustomMachineBlockBuilderJS machine(ResourceLocation machineID) {
        this.machineID = machineID;
        return this;
    }

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.BLOCK;
    }

    @Override
    public Block createObject() {
        Block block = PlatformHelper.createMachineBlock();
        CustomMachinery.CUSTOM_BLOCK_MACHINES.put(this.machineID, block);
        return block;
    }

    @Override
    public void createAdditionalObjects() {
        RegistryInfo.ITEM.addBuilder(new ItemBuilder(this.id) {
            @Override
            public Item createObject() {
                return new CustomMachineItem(CustomMachineBlockBuilderJS.this.get(), new Item.Properties().tab(Registration.GROUP), CustomMachineBlockBuilderJS.this.machineID);
            }

            @Override
            public void generateAssetJsons(AssetJsonGenerator generator) {
                generator.itemModel(this.id, modelGenerator -> modelGenerator.parent("custommachinery:item/custom_machine_item"));
            }
        });
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        generator.blockState(this.id, stateGenerator -> stateGenerator.variant("", "custommachinery:block/custom_machine_block"));
    }
}
