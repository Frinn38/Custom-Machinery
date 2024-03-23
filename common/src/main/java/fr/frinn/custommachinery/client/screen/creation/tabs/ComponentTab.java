package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import net.minecraft.network.chat.Component;

public class ComponentTab extends MachineEditTab {


    public ComponentTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.components"), parent);
    }
}
