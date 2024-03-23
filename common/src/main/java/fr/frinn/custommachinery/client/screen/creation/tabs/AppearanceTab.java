package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import net.minecraft.network.chat.Component;

public class AppearanceTab extends MachineEditTab {


    public AppearanceTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.appearance"), parent);
    }
}
