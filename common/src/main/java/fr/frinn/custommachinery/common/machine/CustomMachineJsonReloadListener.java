package fr.frinn.custommachinery.common.machine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.architectury.utils.GameInstance;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.common.util.CustomJsonReloadListener;
import fr.frinn.custommachinery.common.util.MachineList;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import oshi.util.tuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CustomMachineJsonReloadListener extends CustomJsonReloadListener {

    private static final String MAIN_PACKNAME = "main";

    public CustomMachineJsonReloadListener() {
        super("machines");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        ICustomMachineryAPI.INSTANCE.logger().info("Reading Custom Machinery Machines...");

        CustomMachinery.MACHINES.clear();

        //Keep upgraded machines here until all other machines finished loading
        //List<Triplet<ParentID, ID, MachineJson>>
        List<Triplet<ResourceLocation, ResourceLocation, JsonObject>> upgradedMachines = new ArrayList<>();

        map.forEach((id, json) -> {
            String packName = getPackName(resourceManager, id);

            ICustomMachineryAPI.INSTANCE.logger().info("Parsing machine json: {} in datapack: {}", id, packName);

            //Check if the content of the json file is a json object
            if(!json.isJsonObject()) {
                ICustomMachineryAPI.INSTANCE.logger().error("Bad machine JSON: {} must be a json object and not an array or primitive, skipping...", id);
                return;
            }

            JsonObject jsonObject = (JsonObject) json;

            //If there is already a machine with same id: error and skip
            if(CustomMachinery.MACHINES.containsKey(id)) {
                ICustomMachineryAPI.INSTANCE.logger().error("A machine with id: {} already exists, skipping...", id);
                return;
            }

            //Check if the file is an upgraded machine
            if(jsonObject.has("parent") && jsonObject.get("parent").isJsonPrimitive() && jsonObject.getAsJsonPrimitive("parent").isString()) {
                String parent = jsonObject.getAsJsonPrimitive("parent").getAsString();
                try {
                    ResourceLocation parentID = new ResourceLocation(parent);
                    if(map.containsKey(parentID))
                        upgradedMachines.add(new Triplet<>(parentID, id, jsonObject));
                    else
                        ICustomMachineryAPI.INSTANCE.logger().error("Upgraded machine '{}' reference parent machine '{}' which doesn't exist, skipping", id, parentID);
                    return;
                } catch (ResourceLocationException e) {
                    ICustomMachineryAPI.INSTANCE.logger().error("Invalid parent ID '{}' in machine json '{}', skipping...\n{}", parent, id, e.getMessage());
                    return;
                }
            }

            //Read the file as a CustomMachine
            DataResult<CustomMachine> result = CustomMachine.CODEC.read(JsonOps.INSTANCE, json);
            if(result.result().isPresent()) {
                CustomMachine machine = result.result().get();
                if(packName.equals(MAIN_PACKNAME))
                    machine.setLocation(MachineLocation.fromDefault(id));
                else
                    machine.setLocation(MachineLocation.fromDatapack(id, packName));
                CustomMachinery.MACHINES.put(id, machine);
                ICustomMachineryAPI.INSTANCE.logger().info("Successfully parsed machine json: {}", id);
                return;
            } else if(result.error().isPresent()) {
                ICustomMachineryAPI.INSTANCE.logger().error("Error while parsing machine json: {}, skipping...\n{}", id, result.error().get().message());
                return;
            }
            throw new IllegalStateException("No success nor error when parsing machine json: " + id + ". This can't happen.");
        });

        //Process upgraded machines
        while(!upgradedMachines.isEmpty()) {
            Iterator<Triplet<ResourceLocation, ResourceLocation, JsonObject>> iterator = upgradedMachines.iterator();
            while(iterator.hasNext()) {
                Triplet<ResourceLocation, ResourceLocation, JsonObject> triplet = iterator.next();
                CustomMachine parent = CustomMachinery.MACHINES.get(triplet.getA());
                if(parent == null) {
                    //Check if the parent is in our waiting list
                    upgradedMachines.stream().filter(t -> t.getB().equals(triplet.getA())).findFirst().ifPresentOrElse(t -> {
                        if(t.getA().equals(triplet.getB())) {
                            ICustomMachineryAPI.INSTANCE.logger().error("Circular reference in upgraded machines '{}' and '{}' both referencing each other as parent", triplet.getB(), t.getA());
                            iterator.remove();
                        }
                    }, () -> {
                        ICustomMachineryAPI.INSTANCE.logger().error("Upgraded machine '{}' reference parent machine '{}' which doesn't exist, skipping", triplet.getB(), triplet.getA());
                        iterator.remove();
                    });
                    //Parent not already loaded
                    continue;
                }

                ResourceLocation id = triplet.getB();
                DataResult<UpgradedCustomMachine> result = UpgradedCustomMachine.makeCodec(parent).read(JsonOps.INSTANCE, triplet.getC());
                if(result.result().isPresent()) {
                    CustomMachine machine = result.result().get();
                    String packName = getPackName(resourceManager, id);
                    if(packName.equals(MAIN_PACKNAME))
                        machine.setLocation(MachineLocation.fromDefault(id));
                    else
                        machine.setLocation(MachineLocation.fromDatapack(id, packName));
                    CustomMachinery.MACHINES.put(id, machine);
                    ICustomMachineryAPI.INSTANCE.logger().info("Successfully parsed machine json: {}", id);
                } else if(result.error().isPresent())
                    ICustomMachineryAPI.INSTANCE.logger().error("Error while parsing machine json: {}, skipping...\n{}", id, result.error().get().message());

                iterator.remove();
            }
        }


        ICustomMachineryAPI.INSTANCE.logger().info("Finished creating {} custom machines.", CustomMachinery.MACHINES.keySet().size());

        //Refresh existing loaded machines
        if(GameInstance.getServer() != null)
            MachineList.setNeedRefresh();
    }

    private String getPackName(ResourceManager resourceManager, ResourceLocation id) {
        try {
            return resourceManager.getResourceOrThrow(new ResourceLocation(id.getNamespace(), "machines/" + id.getPath() + ".json")).sourcePackId();
        } catch (IOException e) {
            return MAIN_PACKNAME;
        }
    }
}