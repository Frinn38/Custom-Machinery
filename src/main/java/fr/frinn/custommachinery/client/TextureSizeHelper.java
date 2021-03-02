package fr.frinn.custommachinery.client;

import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextureSizeHelper {

    private final static Map<ResourceLocation, Pair<Integer, Integer>> SIZES = new HashMap<>();

    public static int getTextureWidth(ResourceLocation texture) {
        if(SIZES.containsKey(texture))
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
                CustomMachinery.LOGGER.warn("No texture found for location: " + texture.toString());
            }
            return 0;
        }
    }

    public static int getTextureHeight(ResourceLocation texture) {
        if(SIZES.containsKey(texture))
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
                CustomMachinery.LOGGER.warn("No texture found for location: " + texture.toString());
            }
            return 0;
        }
    }
}
