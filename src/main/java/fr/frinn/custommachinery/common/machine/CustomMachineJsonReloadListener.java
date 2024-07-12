package fr.frinn.custommachinery.common.machine;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.common.integration.kubejs.KubeJSIntegration;
import fr.frinn.custommachinery.common.requirement.FunctionRequirement;
import fr.frinn.custommachinery.common.util.CustomJsonReloadListener;
import fr.frinn.custommachinery.common.util.MachineList;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import oshi.util.tuples.Triplet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CustomMachineJsonReloadListener extends CustomJsonReloadListener {

    private static final String MAIN_PACKNAME = "main";

    public static IContext context;

    public CustomMachineJsonReloadListener() {
        super("machines");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        ICustomMachineryAPI.INSTANCE.logger().info("Reading Custom Machinery Machines...");

        CustomMachinery.MACHINES.clear();

        context = this.getContext();

        //Keep upgraded machines here until all other machines finished loading
        //List<Triplet<ParentID, ID, MachineJson>>
        List<Triplet<ResourceLocation, ResourceLocation, JsonObject>> upgradedMachines = new ArrayList<>();

        map.forEach((id, json) -> {
            MachineLocation location = getMachineLocation(resourceManager, id);

            ICustomMachineryAPI.INSTANCE.logger().info("Parsing machine json: {} in datapack: {}", id, location.getPackName());

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
                    ResourceLocation parentID = ResourceLocation.parse(parent);
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
                machine.setLocation(location);
                CustomMachinery.MACHINES.put(id, machine);
                ICustomMachineryAPI.INSTANCE.logger().info("Successfully parsed machine json: {}", id);
            } else if(result.error().isPresent())
                ICustomMachineryAPI.INSTANCE.logger().error("Error while parsing machine json: {}, skipping...\n{}", id, result.error().get().message());
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
                    machine.setLocation(getMachineLocation(resourceManager, id));
                    CustomMachinery.MACHINES.put(id, machine);
                    ICustomMachineryAPI.INSTANCE.logger().info("Successfully parsed machine json: {}", id);
                } else if(result.error().isPresent())
                    ICustomMachineryAPI.INSTANCE.logger().error("Error while parsing machine json: {}, skipping...\n{}", id, result.error().get().message());

                iterator.remove();
            }
        }

        context = null;

        ICustomMachineryAPI.INSTANCE.logger().info("Finished creating {} custom machines.", CustomMachinery.MACHINES.keySet().size());

        //Refresh existing loaded machines
        if(ServerLifecycleHooks.getCurrentServer() != null)
            MachineList.setNeedRefresh();
        FunctionRequirement.errors.clear();
    }

    private MachineLocation getMachineLocation(ResourceManager resourceManager, ResourceLocation id) {
        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "machines/" + id.getPath() + ".json");
        try {
            Resource res = resourceManager.getResourceOrThrow(path);
            String packName = res.sourcePackId();
            if(packName.equals(MAIN_PACKNAME))
                return MachineLocation.fromDefault(id, packName);
            else if(packName.contains("KubeJS") && ModList.get().isLoaded("kubejs"))
                return KubeJSIntegration.getMachineLocation(res, packName, id);
            else {
                try(PackResources pack = res.source()) {
                    if(pack instanceof FilePackResources)
                        return MachineLocation.fromDatapackZip(id, packName);
                    else if(pack instanceof PathPackResources)
                        return MachineLocation.fromDatapack(id, packName);
                }
            }
        } catch (IOException ignored) {

        }
        return MachineLocation.fromDefault(id, MAIN_PACKNAME);
    }
}