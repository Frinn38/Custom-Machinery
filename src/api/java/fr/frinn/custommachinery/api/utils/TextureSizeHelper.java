package fr.frinn.custommachinery.api.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class to find the size of a texture, do not call its methods on a dedicated server as it will immediately crash.
 */
public class TextureSizeHelper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, Pair<Integer, Integer>> SIZES = new HashMap<>();

    public static int getTextureWidth(@Nullable ResourceLocation texture) {
        if(texture == null)
            return 0;
        else if(SIZES.containsKey(texture))
            return SIZES.get(texture).getLeft();
        else {
            try {
                BufferedImage image = ImageIO.read(Minecraft.getInstance().getResourceManager().getResource(texture).getInputStream());
                int width = image.getWidth();
                int height = image.getHeight();
                Pair<Integer, Integer> sizes = Pair.of(width, height);
                SIZES.put(texture, sizes);
                return width;
            } catch (IOException e) {
                LOGGER.warn("No texture found for location: " + texture);
            }
            return 0;
        }
    }

    public static int getTextureHeight(@Nullable ResourceLocation texture) {
        if(texture == null)
            return 0;
        else if(SIZES.containsKey(texture))
            return SIZES.get(texture).getRight();
        else {
            try {
                BufferedImage image = ImageIO.read(Minecraft.getInstance().getResourceManager().getResource(texture).getInputStream());
                int width = image.getWidth();
                int height = image.getHeight();
                Pair<Integer, Integer> sizes = Pair.of(width, height);
                SIZES.put(texture, sizes);
                return height;
            } catch (IOException e) {
                LOGGER.warn("No texture found for location: " + texture);
            }
            return 0;
        }
    }
}
