package fr.frinn.custommachinery.common.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class CustomJsonReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {

    private static final Gson GSON = new GsonBuilder().create();
    private static final int PATH_SUFFIX_LENGTH = ".json".length();
    private static final Logger LOGGER = CustomMachinery.LOGGER;
    private final String directory;

    public CustomJsonReloadListener(String string) {
        this.directory = string;
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        LOGGER.info("Parsing all .json files in {} folder.", this.directory);
        Map<ResourceLocation, JsonElement> map = Maps.newHashMap();
        int i = this.directory.length() + 1;

        for (ResourceLocation loc : manager.listResources(this.directory, loc -> loc.endsWith(".json"))) {
            String path = loc.getPath();
            ResourceLocation id = new ResourceLocation(loc.getNamespace(), path.substring(i, path.length() - PATH_SUFFIX_LENGTH));

            try(Resource resource = manager.getResource(loc)) {
                try(InputStream inputStream = resource.getInputStream()) {
                    try(Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        JsonElement jsonElement = GsonHelper.fromJson(GSON, reader, JsonElement.class);
                        if(jsonElement != null) {
                            JsonElement replaced = map.put(id, jsonElement);
                            if(replaced != null)
                                throw new IllegalStateException("Duplicate data file ignored with ID " + id);
                        } else
                            LOGGER.error("Couldn't load data file {} from {} as it's null or empty", id, loc);
                    }
                }
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                LOGGER.error("Couldn't parse data file {} from {}\n{}", id, loc, e);
            }
        }
        LOGGER.info("Finished parsing .json files in {} folder.", this.directory);
        return map;
    }
}
