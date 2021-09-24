package fr.frinn.custommachinery.api.guielement;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineTile;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Container;

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
    ContainerScreen<? extends Container> getScreen();
}
