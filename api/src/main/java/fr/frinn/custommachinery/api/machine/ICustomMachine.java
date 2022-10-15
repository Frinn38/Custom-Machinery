package fr.frinn.custommachinery.api.machine;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public interface ICustomMachine {

    /**
     * @return The name of this machine, as specified in the "name" property of the machine json.
     */
    Component getName();

    /**
     * @return The id of the machine. Path of the id will be the path of the machine json file in the datapack.
     */
    ResourceLocation getId();

    /**
     * @return true if the machine is DUMMY, usually indicate that something went wrong, or a machine was not found.
     */
    boolean isDummy();

    /**
     * @return The MachineAppearance corresponding to the supplied MachineStatus.
     */
    IMachineAppearance getAppearance(MachineStatus status);
}
