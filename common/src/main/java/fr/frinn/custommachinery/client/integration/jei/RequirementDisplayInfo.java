package fr.frinn.custommachinery.client.integration.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.impl.util.TextureSizeHelper;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RequirementDisplayInfo implements IDisplayInfo {

    private ResourceLocation icon = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/create_icon.png");
    private int width = 10;
    private int height = 10;
    private int u = 0;
    private int v = 0;
    private TextureAtlasSprite sprite;
    private ItemStack item;
    private final List<Pair<Component, TooltipPredicate>> tooltips = new ArrayList<>();
    private ClickAction clickAction;
    private IconType iconType = IconType.TEXTURE;

    @Override
    public RequirementDisplayInfo setTextureIcon(ResourceLocation icon, int width, int height, int u, int v) {
        this.icon = icon;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        this.iconType = IconType.TEXTURE;
        return this;
    }

    @Override
    public IDisplayInfo setSpriteIcon(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        this.iconType = IconType.ANIMATED;
        return this;
    }

    @Override
    public RequirementDisplayInfo setItemIcon(ItemStack stack) {
        this.item = stack;
        this.iconType = IconType.ITEM;
        return this;
    }

    @Override
    public RequirementDisplayInfo addTooltip(Component tooltip, TooltipPredicate predicate) {
        this.tooltips.add(Pair.of(tooltip, predicate));
        return this;
    }

    @Override
    public void setClickAction(ClickAction clickAction) {
        this.clickAction = clickAction;
    }

    public void renderIcon(PoseStack matrix, int size) {
        switch (this.iconType) {
            case TEXTURE -> {
                ClientHandler.bindTexture(this.icon);
                GuiComponent.blit(matrix, 0, 0, size, size, this.u, this.v, this.width, this.height, TextureSizeHelper.getTextureWidth(this.icon), TextureSizeHelper.getTextureHeight(this.icon));
            }
            case ANIMATED -> {
                ClientHandler.bindTexture(this.sprite.atlas().location());
                GuiComponent.blit(matrix, 0, 0, 0, size, size, this.sprite);
            }
            case ITEM -> {
                matrix.scale(size / 16.0F, size / 16.0F, 1.0F);
                ClientHandler.renderItemAndEffectsIntoGUI(matrix, this.item, 0, 0);
            }
        }
    }

    public List<Pair<Component, TooltipPredicate>> getTooltips() {
        return this.tooltips;
    }

    public boolean hasClickAction() {
        return this.clickAction != null;
    }

    public boolean handleClick(CustomMachine machine, IMachineRecipe recipe, InputConstants.Key button) {
        if(hasClickAction()) {
            this.clickAction.handleClick(machine, recipe, button.getValue());
            return true;
        }
        return false;
    }

    public enum IconType {
        TEXTURE,
        ANIMATED,
        ITEM
    }
}