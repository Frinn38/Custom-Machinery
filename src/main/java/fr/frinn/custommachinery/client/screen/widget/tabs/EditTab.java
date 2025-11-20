package fr.frinn.custommachinery.client.screen.widget.tabs;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class EditTab extends GridLayoutTab {

    public EditTab(Component title) {
        super(title);
    }

    public void opened() {

    }

    public void closed() {

    }

    public List<AbstractWidget> getToolButtons() {
        return Collections.emptyList();
    }
}
