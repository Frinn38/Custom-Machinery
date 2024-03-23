package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import net.minecraft.network.chat.Component;

public class GuiTab extends MachineEditTab {


    public GuiTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.gui"), parent);
    }
}
