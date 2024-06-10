package fr.frinn.custommachinery.api.guielement;

import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.api.machine.MachineTile;

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
}
