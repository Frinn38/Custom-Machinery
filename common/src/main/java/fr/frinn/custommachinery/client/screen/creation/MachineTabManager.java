package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.client.screen.creation.tabs.MachineEditTab;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;

import java.util.function.Consumer;

public class MachineTabManager extends TabManager {

    public MachineTabManager(Consumer<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        super(addWidget, removeWidget);
    }

    @Override
    public void setCurrentTab(Tab tab, boolean playClickSound) {
        if(this.getCurrentTab() instanceof MachineEditTab editTab)
            editTab.closed();
        super.setCurrentTab(tab, playClickSound);
        if(tab instanceof MachineEditTab editTab)
            editTab.opened();
    }
}
