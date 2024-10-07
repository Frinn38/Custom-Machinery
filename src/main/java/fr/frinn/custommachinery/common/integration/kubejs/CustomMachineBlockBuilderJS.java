package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.util.ID;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class CustomMachineBlockBuilderJS extends BuilderBase<Block> {

    public static final List<String> VALID_RENDER_TYPES = List.of("solid", "cutout", "translucent");
    private ResourceLocation machineID;
    private String renderType;
    private boolean occlusion;

    public CustomMachineBlockBuilderJS(ResourceLocation i) {
        super(i);
        this.machineID = id;
        this.renderType = "translucent";
        this.occlusion = false;
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

    public CustomMachineBlockBuilderJS occlude() {
        this.occlusion = true;
        return this;
    }

    @Override
    public Block createObject() {
        CustomMachineBlock block = new CustomMachineBlock(this.renderType, this.occlusion);
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
}
