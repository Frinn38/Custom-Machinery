package fr.frinn.custommachinery.api.integration.jei;

import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.impl.codec.FieldCodec;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Used by the JEI integration to collect information about an {@link IDisplayInfoRequirement}
 * This can be used to set an icon in the jei recipe screen, add a tooltip when the player mouse cursor hover the requirement,
 * or execute an action when the player click on the requirement.
 */
public interface IDisplayInfo {

    /**
     * Add a new line on the tooltip to render when the player mouse cursor hover the requirement.
     * This line will always be rendered.
     * @param text The tooltip to render.
     * @return Itself, to chain calls.
     */
    default IDisplayInfo addTooltip(Component text) {
        return addTooltip(text, TooltipPredicate.ALWAYS);
    };

    /**
     * Add a new line on the tooltip to render when the player mouse cursor hover the requirement.
     * This line will be rendered only if the {@link TooltipPredicate} return true.
     * @param text The tooltip to render.
     * @param predicate A predicate to check if this line should be rendered.
     * @return Itself, to chain calls.
     */
    IDisplayInfo addTooltip(Component text, TooltipPredicate predicate);

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
     * @param atlas The location of the atlas containing the sprite.
     * @param sprite The location of sprite to display in the atlas, can be animated.
     * @return Itself, to chain calls.
     */
    IDisplayInfo setSpriteIcon(ResourceLocation atlas, ResourceLocation sprite);

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
     * The {@link ClickAction} will give you the {@link ICustomMachine} handling this {@link IMachineRecipe} and which mouse button was clicked. 0 = left, 1 = right, 2 = middle
     * @param clickAction The action to execute.
     */
    void setClickAction(ClickAction clickAction);

    /**
     * Called when the player click the requirement icon in the jei recipe gui.
     */
    interface ClickAction {
        /**
         * @param machine The machine that will process the recipe.
         * @param recipe The recipe that contain this requirement.
         * @param mouseButton The mouse button that was pressed: 0 = left, 1 = right, 2 = middle.
         */
        void handleClick(ICustomMachine machine, IMachineRecipe recipe, int mouseButton);
    }

    /**
     * Called for each added lines of the tooltip when the player mouse hover the requirement in jei.
     */
    @FunctionalInterface
    interface TooltipPredicate {
        TooltipPredicate ALWAYS = (player, advancedTooltips) -> true;
        TooltipPredicate ADVANCED = (player, advancedTooltips) -> advancedTooltips;
        TooltipPredicate CREATIVE = (player, advancedTooltips) -> player.getAbilities().instabuild;

        NamedCodec<TooltipPredicate> CODEC = NamedCodec.STRING.comapFlatMap(s -> {
            String predicate = FieldCodec.toSnakeCase(s);
            return switch (predicate) {
                case "advanced" -> DataResult.success(ADVANCED);
                case "creative" -> DataResult.success(CREATIVE);
                case "always" -> DataResult.success(ALWAYS);
                default -> DataResult.error("Invalid tooltip predicate: " + s);
            };
        }, predicate -> predicate == ADVANCED ? "advanced" : predicate == CREATIVE ? "creative" : "always", "Tooltip predicate");

        /**
         * @param player The player that hover the requirement.
         * @param advancedTooltips True if advanced tooltips (F3+H) are enabled.
         * @return True if the line should be displayed on the tooltip, false otherwise.
         */
        boolean shouldDisplay(Player player, boolean advancedTooltips);
    }
}
