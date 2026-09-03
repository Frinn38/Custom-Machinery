package fr.frinn.custommachinery.api.guielement;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineTile;

public interface IMachineScreen {

    /**
     * @return The left position (in pixels) of this machine screen.
     */
    int getX();

    /**
     * @return The top position (in pixels) of this machine screen.
     */
    int getY();

    /**
     * @return The width (in pixels) of this machine screen.
     */
    int getWidth();

    /**
     * @return The height (in pixels) of this machine screen.
     */
    int getHeight();

    /**
     * @return The MachineTile that the player currently use, only client side data will be available in this MachineTile instance.
     */
    MachineTile getTile();

    /**
     * @return The ICustomMachine instance linked to the MachineTile the player is currently using.
     */
    ICustomMachine getMachine();
}
