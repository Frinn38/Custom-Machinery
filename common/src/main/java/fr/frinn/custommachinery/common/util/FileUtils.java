package fr.frinn.custommachinery.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    public static boolean writeMachineJSON(MinecraftServer server, CustomMachine machine) {
        if(server != null) {
            DataResult<JsonElement> result = CustomMachine.CODEC.encodeStart(JsonOps.INSTANCE, machine);
            JsonElement json = result.resultOrPartial(CustomMachinery.LOGGER::error).orElseThrow(() -> new JsonParseException("Error while writing custom machine: " + machine.getLocation().getId() + " to JSON"));
            try {
                File file = getCustomMachineJson(server, machine.getLocation());
                file.getParentFile().mkdirs();
                CustomMachinery.LOGGER.info("Writing machine: " + machine.getLocation().getId() + " to: " + file.getPath());
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

    public static boolean deleteMachineJSON(MinecraftServer server, MachineLocation loacation) {
        if(server != null) {
            File file = getCustomMachineJson(server, loacation);
            if (file.exists() && file.delete()) {
                CustomMachinery.LOGGER.info("Deleting Custom Machine: " + file.getPath());
                return true;
            }
            else
                CustomMachinery.LOGGER.info("Cannot delete Custom Machine: " + file.getPath());
        }
        return false;
    }

    public static File getCustomMachineJson(MinecraftServer server, MachineLocation location) {
        String path = server.getWorldPath(LevelResource.DATAPACK_DIR) + File.separator + location.getPath();
        return new File(path);
    }
}
