package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.widget.tabs.EditTab;
import net.minecraft.network.chat.Component;

public class MachineEditTab extends EditTab {

    public final MachineEditScreen parent;

    public MachineEditTab(Component title, MachineEditScreen parent) {
        super(title);
        this.parent = parent;
    }
}
