package fr.frinn.custommachinery.api.integration.jei;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

/**
 * Used by the JEI integration to collect information about an {@link IDisplayInfoRequirement}
 * This can be used to set an icon in the jei recipe screen, add a tooltip when the player mouse cursor hover the requirement,
 * or execute an action when the player click on the requirement.
 */
public interface IDisplayInfo {

    /**
     * Add a tooltip to render when the player mouse cursor hover the requirement.
     * Each call will start a new line on the tooltip.
     * @param text The tooltip to render.
     * @return Itself, to chain calls.
     */
    IDisplayInfo addTooltip(Component text);

    /**
     * Set a texture as the requirement icon in the jei recipe screen.
     * The texture will be treated as a 16x16px texture.
     * @param texture The location of the texture, the path must start from the "textures" folder and include the extension ".png".
     * @return Itself, to chain calls.
     */
    default IDisplayInfo setTextureIcon(ResourceLocation texture) {
        return setTextureIcon(texture, 16, 16, 0, 0);
    }

    /**
     * Set a texture as the requirement icon in the jei recipe screen.
     * Use this method if the texture you want to display is not 16x16.
     * @param texture The location of the texture, the path must start from the "textures" folder and include the extension ".png".
     * @param width The partial width of the texture you want to display.
     * @param height The partial height of the texture you want to display.
     * @return Itself, to chain calls.
     */
    default IDisplayInfo setTextureIcon(ResourceLocation texture, int width, int height) {
        return setTextureIcon(texture, width, height, 0, 0);
    }

    /**
     * Set a texture as the requirement icon in the jei recipe screen.
     * Use this method if the texture you want to display is not 16x16px, and you don't want to start in the top left corner of the texture.
     * @param texture The location of the texture, the path must start from the "textures" folder and include the extension ".png".
     * @param width The partial width of the texture you want to display.
     * @param height The partial height of the texture you want to display.
     * @param u The horizontal coordinate you want to start drawing the texture, from left to right.
     * @param v The vertical coordinate you want to start drawing the texture, from top to bottom.
     * @return Itself, to chain calls.
     */
    IDisplayInfo setTextureIcon(ResourceLocation texture, int width, int height, int u, int v);

    /**
     * Set an already loaded and stitched {@link TextureAtlasSprite} as the requirement icon in the jei recipe screen.
     * A {@link TextureAtlasSprite} can be obtained using {@code <pre>Minecraft.getInstance().getAtlasSpriteGetter(atlasLocation).apply(textureLocation);</pre>}
     * @param sprite The sprite to display, can be animated.
     * @return Itself, to chain calls.
     */
    IDisplayInfo setSpriteIcon(TextureAtlasSprite sprite);

    /**
     * Set an {@link Item} as the requirement icon in the jei recipe screen.
     * @param item The item to display.
     * @return Itself, to chain calls.
     */
    default IDisplayInfo setItemIcon(Item item) {
        return setItemIcon(item.getDefaultInstance());
    }

    /**
     * Set an {@link ItemStack} as the requirement icon in the jei recipe screen.
     * @param stack The item to display.
     * @return Itself, to chain calls.
     */
    IDisplayInfo setItemIcon(ItemStack stack);

    /**
     * Set an action to execute when the player click on the requirement icon in the jei recipe gui.
     * Note that the action will always be executed on the client side, if you want to execute some server-side code you will need to send a packet.
     * The {@link BiConsumer} will give you the {@link ICustomMachine} handling this recipe and which mouse button was clicked. 0 = left, 1 = right, 2 = middle
     * @param clickAction The action to execute.
     */
    void setClickAction(BiConsumer<ICustomMachine, Integer> clickAction);

    //TODO: Remove in 1.18
    @Deprecated
    IDisplayInfo setVisible(boolean visible);
}
