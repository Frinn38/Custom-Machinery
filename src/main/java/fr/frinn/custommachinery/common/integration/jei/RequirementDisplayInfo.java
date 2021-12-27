package fr.frinn.custommachinery.common.integration.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.utils.TextureSizeHelper;
import fr.frinn.custommachinery.common.data.CustomMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RequirementDisplayInfo implements IDisplayInfo {

    private ResourceLocation icon = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/create_icon.png");
    private int width = 10;
    private int height = 10;
    private int u = 0;
    private int v = 0;
    private TextureAtlasSprite sprite;
    private ItemStack item;
    private final List<ITextComponent> tooltips = new ArrayList<>();
    private BiConsumer<ICustomMachine, Integer> clickAction;
    private boolean visible = true;
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
    public RequirementDisplayInfo addTooltip(ITextComponent tooltip) {
        this.tooltips.add(tooltip);
        return this;
    }

    @Override
    public void setClickAction(BiConsumer<ICustomMachine, Integer> clickAction) {
        this.clickAction = clickAction;
    }

    @Override
    public RequirementDisplayInfo setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @SuppressWarnings("deprecation")
    public void renderIcon(MatrixStack matrix, int size) {
        switch (this.iconType) {
            case TEXTURE:
                Minecraft.getInstance().textureManager.bindTexture(this.icon);
                AbstractGui.blit(matrix, 0, 0, size, size, this.u, this.v, this.width, this.height, TextureSizeHelper.getTextureWidth(this.icon), TextureSizeHelper.getTextureHeight(this.icon));
                break;
            case ANIMATED:
                Minecraft.getInstance().getTextureManager().bindTexture(this.sprite.getAtlasTexture().getTextureLocation());
                AbstractGui.blit(matrix, 0, 0, 0, size, size, this.sprite);
                break;
            case ITEM:
                matrix.scale(size / 16.0F, size / 16.0F, 1.0F);
                RenderSystem.pushMatrix();
                RenderSystem.multMatrix(matrix.getLast().getMatrix());
                Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(this.item, 0, 0);
                RenderSystem.popMatrix();
                break;
        }
    }

    public void renderTooltips(MatrixStack matrix, int mouseX, int mouseY, int maxWidth, int maxHeight) {
        if(!this.tooltips.isEmpty())
            GuiUtils.drawHoveringText(matrix, this.tooltips, mouseX, mouseY, maxWidth, maxHeight, maxWidth, Minecraft.getInstance().fontRenderer);
    }

    public boolean hasClickAction() {
        return this.clickAction != null;
    }

    public boolean handleClick(CustomMachine machine, int mouseButton) {
        if(hasClickAction()) {
            this.clickAction.accept(machine, mouseButton);
            return true;
        }
        return false;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public enum IconType {
        TEXTURE,
        ANIMATED,
        ITEM
    }
}
