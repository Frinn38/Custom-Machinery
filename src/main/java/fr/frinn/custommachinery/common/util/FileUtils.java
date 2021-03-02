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

    public static void writeMachineJSON(ResourceLocation id, CustomMachine machine) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null) {
            DataResult<JsonElement> result = CustomMachine.CODEC.encodeStart(JsonOps.INSTANCE, machine);
            JsonElement json = result.resultOrPartial(CustomMachinery.LOGGER::error).orElseThrow(() -> new JsonParseException("Error while writing custom machine: " + id + " to JSON"));
            try {
                String path;
                if(!id.getNamespace().equals(CustomMachinery.MODID)) {
                    path = server.func_240776_a_(FolderName.DATAPACKS) + "\\" + id.getNamespace() + "\\machines\\" + id.getPath() + ".json";
                    path = path.substring(2);
                } else {
                    path = server.getDataDirectory().getPath() + "\\Custom Machines\\" + id.getPath() + ".json";
                }

                File file = new File(path);
                file.getParentFile().mkdirs();
                CustomMachinery.LOGGER.info("Writing machine: " + id + " to: " + file);
                if(file.exists() || file.createNewFile()) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    JsonWriter writer = gson.newJsonWriter(new FileWriter(file));
                    gson.toJson(json, writer);
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteMachineJSON(ResourceLocation id) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null) {
            String path;
            if(id.getNamespace().equals(CustomMachinery.MODID))
                path = server.getDataDirectory().getPath() + "\\Custom Machines\\" + id.getPath() + ".json";
            else
                path = server.func_240776_a_(FolderName.DATAPACKS) + "\\" + id.getNamespace() + "\\machines\\" + id.getPath() + ".json";
            File file = new File(path);
            if (file.exists() && file.delete())
                CustomMachinery.LOGGER.info("Deleting Custom Machine: " + path);
            else
                CustomMachinery.LOGGER.info("Cannot delete Custom Machine: " + path);
        }
    }
}
