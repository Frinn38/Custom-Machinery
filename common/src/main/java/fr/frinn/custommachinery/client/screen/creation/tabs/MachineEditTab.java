package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.network.chat.Component;

public class MachineEditTab extends GridLayoutTab {

    public final MachineEditScreen parent;

    public MachineEditTab(Component title, MachineEditScreen parent) {
        super(title);
        this.parent = parent;
    }
}
