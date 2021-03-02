package fr.frinn.custommachinery.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.TextureSizeHelper;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.gui.*;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.List;

public class CustomMachineScreen extends ContainerScreen<CustomMachineContainer> {

    private CustomMachineTile tile;
    private CustomMachine machine;

    public CustomMachineScreen(CustomMachineContainer container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        this.tile = container.tile;
        this.machine = container.tile.getMachine();
        this.xSize = 256;
        this.ySize = 192;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(matrix);

        matrix.push();
        matrix.translate(this.guiLeft, this.guiTop, 0);
        this.machine.getGuiElements()
                .stream()
                .sorted(Utils.GUI_ELEMENTS_COMPARATOR.reversed())
                .forEach(element -> element.getType().getRenderer().renderElement(matrix, element, this));
        matrix.pop();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY) {
        matrix.push();
        matrix.translate(-this.guiLeft, -this.guiTop, 0);
        this.renderHoveredTooltip(matrix, mouseX, mouseY);
        this.machine.getGuiElements()
                .stream()
                .filter(element -> element.getType().getRenderer().isHovered(element, this, mouseX - this.guiLeft, mouseY - this.guiTop))
                .max(Utils.GUI_ELEMENTS_COMPARATOR)
                .ifPresent(element -> element.getType().getRenderer().renderTooltip(matrix, element, this, mouseX, mouseY));
        matrix.pop();
    }

    public CustomMachine getMachine() {
        return this.machine;
    }

    public CustomMachineTile getTile() {
        return this.tile;
    }

}
