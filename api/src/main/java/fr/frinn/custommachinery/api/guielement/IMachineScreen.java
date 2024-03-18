package fr.frinn.custommachinery.api.guielement;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface IMachineScreen {

    int getX();

    int getY();

    int getWidth();

    int getHeight();

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
}
