package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.integration.config.CMConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public abstract class CustomJsonReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {

    private static final Gson GSON = new GsonBuilder().create();
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private static final Logger LOGGER = CustomMachinery.LOGGER;
    private final String directory;
    private final String legacyDirectory;

    public CustomJsonReloadListener(String directory, String legacyDirectory) {
        this.directory = directory;
        this.legacyDirectory = legacyDirectory;
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        //Checking if any files are in legacy directory
        if(CMConfig.get().logLegacyFolderFiles && !manager.listResources(this.legacyDirectory, loc -> loc.getPath().endsWith(".json")).isEmpty()) {
            LOGGER.warn("Files found in legacy folder: '{}'. To make CM work correctly please move these files to the correct folder: '{}'", this.legacyDirectory, this.directory);
            if(ServerLifecycleHooks.getCurrentServer() != null)
                ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastSystemMessage(Component.literal("Files found in legacy folder: " + this.legacyDirectory + "\nTo make CM work correctly please move these files to the correct folder: " + this.directory + "\nThis message can be turned off in CM config").withStyle(ChatFormatting.RED), false);
        }

        LOGGER.info("Parsing all .json files in {} folder.", this.directory);
        Map<ResourceLocation, JsonElement> map = Maps.newHashMap();
        int i = this.directory.length() + 1;

        for(Map.Entry<ResourceLocation, Resource> entry : manager.listResources(this.directory, loc -> loc.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation loc = entry.getKey();
            String path = loc.getPath();
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), path.substring(i, path.length() - PATH_SUFFIX_LENGTH));

            try(Reader reader = entry.getValue().openAsReader()) {
                JsonElement jsonElement = GsonHelper.fromNullableJson(GSON, reader, JsonElement.class, false);
                if(jsonElement != null) {
                    JsonElement replaced = map.put(id, jsonElement);
                    if(replaced != null)
                        throw new IllegalStateException("Duplicate data file ignored with ID " + id);
                } else
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", id, loc);
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                LOGGER.error("Couldn't parse data file {} from {}\n{}", id, loc, e);
            }
        }
        LOGGER.info("Finished, {} .json files successfully parsed in {} folder.", map.size(), this.directory);
        return map;
    }
}
