package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class MachineEditTab extends GridLayoutTab {

    public final MachineEditScreen parent;

    public MachineEditTab(Component title, MachineEditScreen parent) {
        super(title);
        this.parent = parent;
    }

    public void opened() {

    }

    public void closed() {

    }

    public List<AbstractWidget> getToolButtons() {
        return Collections.emptyList();
    }
}
