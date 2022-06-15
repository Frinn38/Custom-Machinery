package fr.frinn.custommachinery.api.guielement;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMachineScreen {

    /**
     * @return The MachineTile that the player currently use, only client side data will be available in this MachineTile instance.
     */
    MachineTile getTile();

    /**
     * @return The ICustomMachine instance linked to the MachineTile the player is currently using.
     */
    ICustomMachine getMachine();

    /**
     * @return Itself as a MC ContainerScreen instance, to access useful screen methods like tooltips and rendering.
     */
    AbstractContainerScreen<? extends AbstractContainerMenu> getScreen();

    /**
     * Render a list of tooltips to the screen at the mouse position.
     */
    void drawTooltips(PoseStack pose, List<Component> tooltips, int mouseX, int mouseY);

    /**
     * Render a transparent item at the specified position of the machine screen.
     * @param pose The current {@link PoseStack}.
     * @param item The item to render, the damage bar and other overlays won't be rendered.
     * @param posX The position on the X axis to render the item.
     * @param posY The position on the Y axis to render the item.
     */
    void drawGhostItem(PoseStack pose, ItemStack item, int posX, int posY);
}
