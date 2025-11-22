package fr.frinn.custommachinery.client.screen.creation.upgrade.tabs;

import fr.frinn.custommachinery.client.screen.creation.upgrade.UpgradeEditScreen;
import net.minecraft.network.chat.Component;

public class CoreModifierTab extends UpgradeEditTab {

    public CoreModifierTab(UpgradeEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.upgrade.tab.core_modifier"), parent);
    }
}
