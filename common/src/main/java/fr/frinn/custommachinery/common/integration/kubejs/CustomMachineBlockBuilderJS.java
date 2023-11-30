package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class CustomMachineBlockBuilderJS extends BuilderBase<Block> {

    public static final List<String> VALID_RENDER_TYPES = List.of("solid", "cutout", "translucent");
    private ResourceLocation machineID;
    private String renderType;

    public CustomMachineBlockBuilderJS(ResourceLocation i) {
        super(i);
        this.machineID = id;
        this.renderType = "translucent";
    }

    public CustomMachineBlockBuilderJS machine(ResourceLocation machineID) {
        this.machineID = machineID;
        return this;
    }

    public CustomMachineBlockBuilderJS renderType(String renderType) {
        if(!VALID_RENDER_TYPES.contains(renderType))
            throw new IllegalArgumentException("Render type: '" + renderType + "' is not supported, must be one of " + VALID_RENDER_TYPES);
        this.renderType = renderType;
        return this;
    }

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.BLOCK;
    }

    @Override
    public Block createObject() {
        CustomMachineBlock block = new CustomMachineBlock(this.renderType);
        CustomMachinery.CUSTOM_BLOCK_MACHINES.put(this.machineID, block);
        if(Platform.isFabric())
            Registration.CUSTOM_MACHINE_TILE.get().validBlocks.add(block);
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
                JsonObject json = new JsonObject();
                json.add("parent", new JsonPrimitive("custommachinery:block/custom_machine_block"));
                JsonObject defaultJson = new JsonObject();
                defaultJson.add("", new JsonPrimitive("custommachinery:default/custom_machine_default"));
                json.add("defaults", defaultJson);
                generator.json(AssetJsonGenerator.asItemModelLocation(this.id), json);
            }
        });
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        generator.blockState(this.id, stateGenerator -> stateGenerator.variant("", "custommachinery:block/custom_machine_block"));
    }
}
