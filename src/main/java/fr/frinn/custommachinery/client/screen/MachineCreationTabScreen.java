package fr.frinn.custommachinery.client.screen;

import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MachineCreationTabScreen extends Screen {

    protected MachineCreationScreen parent;
    protected CustomMachineBuilder machine;

    protected int xPos;
    protected int yPos;

    public MachineCreationTabScreen(Component title, MachineCreationScreen parent, CustomMachineBuilder machine) {
        super(title);
        this.parent = parent;
        this.machine = machine;
    }

    @Override
    protected void init() {
        this.xPos = this.parent.xPos;
        this.yPos = this.parent.yPos;
    }
}
