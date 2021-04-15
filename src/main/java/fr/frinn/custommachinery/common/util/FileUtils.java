package fr.frinn.custommachinery.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    public static boolean writeMachineJSON(ResourceLocation id, CustomMachine machine) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null) {
            DataResult<JsonElement> result = CustomMachine.CODEC.encodeStart(JsonOps.INSTANCE, machine);
            JsonElement json = result.resultOrPartial(CustomMachinery.LOGGER::error).orElseThrow(() -> new JsonParseException("Error while writing custom machine: " + id + " to JSON"));
            try {
                File file = getCustomMachineJson(server, id);
                file.getParentFile().mkdirs();
                CustomMachinery.LOGGER.info("Writing machine: " + id + " to: " + file.getPath());
                if(file.exists() || file.createNewFile()) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    JsonWriter writer = gson.newJsonWriter(new FileWriter(file));
                    gson.toJson(json, writer);
                    writer.close();
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean deleteMachineJSON(ResourceLocation id) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null) {
            File file = getCustomMachineJson(server, id);
            if (file.exists() && file.delete()) {
                CustomMachinery.LOGGER.info("Deleting Custom Machine: " + file.getPath());
                return true;
            }
            else
                CustomMachinery.LOGGER.info("Cannot delete Custom Machine: " + file.getPath());
        }
        return false;
    }

    public static File getCustomMachineJson(MinecraftServer server, ResourceLocation id) {
        String path = server.func_240776_a_(FolderName.DATAPACKS) + File.separator + id.getNamespace() + File.separator + "machines" + File.separator + id.getPath() + ".json";
        return new File(path);
    }
}
