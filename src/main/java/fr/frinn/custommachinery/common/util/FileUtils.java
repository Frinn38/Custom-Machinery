package fr.frinn.custommachinery.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.network.SUpdateMachinesPacket;
import net.minecraft.Util;
import net.minecraft.Util.OS;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileUtils {

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static void writeNewMachineJson(MinecraftServer server, CustomMachine machine, boolean kubejs) {
        if(kubejs && !ModList.get().isLoaded("kubejs")) {
            CustomMachinery.LOGGER.error("Can't write new machine json {} in kubejs data folder because KubeJS isn't present", machine.getId());
            return;
        }
        DataResult<JsonElement> result = CustomMachine.CODEC.encodeStart(JsonOps.INSTANCE, machine);
        if(result.error().isPresent()) {
            CustomMachinery.LOGGER.error("Can't write new machine json: {}\n{}", machine.getId().getPath(), result.error().get().message());
            return;
        }
        if(result.result().isPresent()) {
            JsonElement json = result.result().get();
            String root = server.getServerDirectory().toFile().getAbsolutePath();
            if(!FMLLoader.isProduction())
                root = root.substring(0, root.length() - 2);
            if(kubejs)
                root = root + File.separator + "kubejs" + File.separator + "data" + File.separator + machine.getId().getNamespace() + File.separator + "machine";
            File file = new File(root, machine.getId().getPath() + ".json");
            File directory = file.getParentFile();
            if(!directory.exists() && !directory.mkdirs()) {
                CustomMachinery.LOGGER.error("Can't create new directory in '{}'", directory.getAbsolutePath());
                return;
            }
            CustomMachinery.LOGGER.info("Writing new machine: {} in {}", machine.getLocation().id(), file.getPath());
            try {
                if(file.exists() || file.createNewFile()) {
                    JsonWriter writer = GSON.newJsonWriter(new FileWriter(file));
                    GSON.toJson(json, writer);
                    writer.close();
                    if(kubejs) {
                        //Immediately update machine list
                        CustomMachinery.MACHINES.put(machine.getId(), machine);
                        MachineList.setNeedRefresh();
                        PacketDistributor.sendToAllPlayers(new SUpdateMachinesPacket(CustomMachinery.MACHINES));
                    }
                } else {
                    CustomMachinery.LOGGER.error("Can't write new machine file in '{}'", file.getAbsolutePath());
                }
            } catch (IOException e) {
                CustomMachinery.LOGGER.error("Error while writing new machine to file: {}\n{}\n{}", file.getAbsolutePath(), e.getMessage(), ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public static void writeMachineJson(MinecraftServer server, CustomMachine machine) {
        MachineLocation location = machine.getLocation();
        File machineJson = location.getFile(server);
        if(machineJson == null) {
            CustomMachinery.LOGGER.error("Error while editing machine: {}\nCan't edit machine loaded with {}", location.id(), location.loader().toString());
            return;
        } else if(!machineJson.exists() || machineJson.isDirectory()) {
            CustomMachinery.LOGGER.error("Error while editing machine: {}\nFile '{}' doesn't exist", location.id(), machineJson.getAbsolutePath());
            return;
        }
        try(JsonWriter writer = GSON.newJsonWriter(new FileWriter(machineJson))) {
            DataResult<JsonElement> result = CustomMachine.CODEC.encodeStart(MachineJsonOps.INSTANCE, machine);
            if(result.error().isPresent()) {
                CustomMachinery.LOGGER.error("Can't edit machine json: {}\n{}", machine.getId().getPath(), result.error().get().message());
                return;
            }
            if(result.result().isPresent()) {
                JsonElement json = result.result().get();
                GSON.toJson(json, writer);
                BasicFileAttributes attributes = Files.getFileAttributeView(machineJson.toPath(), BasicFileAttributeView.class).readAttributes();
                machine.setLocation(MachineLocation.fromLoader(machine.getLocation().loader(), machine.getId(), machine.getLocation().packName(), attributes.creationTime(), attributes.lastModifiedTime()));
                CustomMachinery.LOGGER.info("Successfully edited machine: {} at location '{}'", location.id(), machineJson.getAbsolutePath());
            }
        } catch (IOException e) {
            CustomMachinery.LOGGER.error("Error while editing machine to file: {}\n{}\n{}", machineJson.getAbsolutePath(), e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
    }

    public static boolean deleteMachineJson(MinecraftServer server, MachineLocation location) {
        File machineJson = location.getFile(server);
        if(machineJson == null) {
            CustomMachinery.LOGGER.error("Error while deleting machine: {}\nCan't delete machine loaded with {}", location.id(), location.loader().toString());
            return false;
        } else if(!machineJson.exists() || machineJson.isDirectory()) {
            CustomMachinery.LOGGER.error("Error while deleting machine: {}\nFile '{}' doesn't exist", location.id(), machineJson.getAbsolutePath());
            return false;
        } else if(!machineJson.delete()) {
            CustomMachinery.LOGGER.error("Error while deleting machine: {}\nFile '{}' can't be deleted", location.id(), machineJson.getAbsolutePath());
            return false;
        }
        CustomMachinery.LOGGER.info("Successfully deleted machine: {} at location '{}'", location.id(), machineJson.getAbsolutePath());
        return true;
    }

    public static void writeTempMachineJson(File gameDirectory, CustomMachineBuilder builder) {
        String id = builder.getLocation().id().toString().replace(":", "_").replace("/", "_");
        File temp = Path.of(gameDirectory.toURI()).resolve(".temp custommachinery " + id + " " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime()) + ".json").toFile();
        try(JsonWriter writer = GSON.newJsonWriter(new FileWriter(temp))) {
            if(temp.exists() || temp.createNewFile()) {
                if (Util.getPlatform() == OS.WINDOWS)
                    Files.setAttribute(temp.toPath(), "dos:hidden", true);
                DataResult<JsonElement> result = CustomMachine.CODEC.encodeStart(MachineJsonOps.INSTANCE, builder.build());
                if (result.error().isPresent()) {
                    CustomMachinery.LOGGER.error("Can't write temp machine json: {}\n{}", builder.getLocation().id().getPath(), result.error().get().message());
                    return;
                }
                if (result.result().isPresent()) {
                    JsonElement json = result.result().get();
                    GSON.toJson(json, writer);
                    CustomMachinery.LOGGER.info("Writing temp machine: {} at location '{}'", builder.getLocation().id(), temp.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            CustomMachinery.LOGGER.error("Error while writing temp machine to file: {}\n{}\n{}", temp.getAbsolutePath(), e.getMessage(), ExceptionUtils.getStackTrace(e));
        }
    }
}
