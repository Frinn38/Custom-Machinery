package fr.frinn.custommachinery.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.integration.kubejs.KubeJSIntegration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static boolean writeMachineJSON(MinecraftServer server, CustomMachine machine) {
        if(server != null) {
            DataResult<JsonElement> result = CustomMachine.CODEC.encodeStart(JsonOps.INSTANCE, machine);
            JsonElement json = result.resultOrPartial(CustomMachinery.LOGGER::error).orElseThrow(() -> new JsonParseException("Error while writing custom machine: " + machine.getLocation().getId() + " to JSON"));
            try {
                List<Path> paths = getCustomMachineJson(server, machine.getLocation().getId());
                for(Path path : paths) {
                    File file = new File(path.toUri());
                    file.getParentFile().mkdirs();
                    CustomMachinery.LOGGER.info("Writing machine: " + machine.getLocation().getId() + " to: " + file.getPath());
                    if(file.exists() || file.createNewFile()) {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        JsonWriter writer = gson.newJsonWriter(new FileWriter(file));
                        gson.toJson(json, writer);
                        writer.close();
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean deleteMachineJSON(MinecraftServer server, ResourceLocation location) {
        /*
        if(server == null)
            return false;
        ResourceLocation trueLocation = new ResourceLocation(location.getNamespace(), "machines/" + location.getPath() + ".json");
        for(PackResources pack : server.getResourceManager().listPacks().toList()) {
            if(pack.getResource(PackType.SERVER_DATA, trueLocation) == null)
                continue;
            if(pack instanceof FilePackResources filePackResources) {
                filePackResources.close();
                Path zipPath = Platform.getGameFolder().resolve(filePackResources.file.toString().substring(2));
                try(FileSystem zipfs = FileSystems.newFileSystem(zipPath, Map.of("create", "false"))) {
                    Path path = zipfs.getPath("data/" + trueLocation.getNamespace() + "/" + trueLocation.getPath());
                    if(Files.exists(path)) {
                        Files.delete(path);
                        CustomMachinery.LOGGER.info("Deleted custom machine json for id {} in zip archive {}", location, zipPath);
                    }
                } catch (IOException e) {
                    CustomMachinery.LOGGER.error(e.getMessage(), e);
                }
                try {
                    filePackResources.getOrCreateZipFile();
                } catch (IOException ignored) {}
            } else if (pack instanceof AbstractPackResources packResources) {
                Path path = Platform.getGameFolder().resolve(packResources.file.toString().substring(2)).resolve("data/" + trueLocation.getNamespace() + "/" + trueLocation.getPath());
                File file = new File(path.toUri());
                if(file.exists() && file.isFile() && file.delete())
                    CustomMachinery.LOGGER.info("Deleted custom machine json for id {} at path {}", location, path);
            } else if(pack.packId().contains("KubeJS")) {
                Path path = KubeJSIntegration.getMachineJsonPath(trueLocation);
                File file = new File(path.toUri());
                if(file.exists() && file.isFile() && file.delete())
                    CustomMachinery.LOGGER.info("Deleted custom machine json for id {} at path {}", location, path);
            }

        }*/
        return false;
    }

    public static List<Path> getCustomMachineJson(MinecraftServer server, ResourceLocation location) {
        ResourceLocation trueLocation = new ResourceLocation(location.getNamespace(), "machines/" + location.getPath() + ".json");
        List<Path> paths = new ArrayList<>();
        for(PackResources pack : server.getResourceManager().listPacks().toList()) {
            if(pack.getResource(PackType.SERVER_DATA, trueLocation) == null)
                continue;
            if(pack.packId().contains("KubeJS"))
                paths.add(KubeJSIntegration.getMachineJsonPath(trueLocation));
        }
        return paths;
    }
}
