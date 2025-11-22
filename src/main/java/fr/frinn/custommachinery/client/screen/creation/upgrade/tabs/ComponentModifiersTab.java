package fr.frinn.custommachinery.client.screen.creation.upgrade.tabs;

import fr.frinn.custommachinery.client.screen.creation.upgrade.UpgradeEditScreen;
import net.minecraft.network.chat.Component;

public class ComponentModifiersTab extends UpgradeEditTab {

    public ComponentModifiersTab(UpgradeEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.upgrade.tab.component_modifiers"), parent);
    }
}
