package fr.frinn.custommachinery.client.screen.creation.upgrade.tabs;

import fr.frinn.custommachinery.client.screen.creation.upgrade.UpgradeEditScreen;
import fr.frinn.custommachinery.client.screen.widget.tabs.EditTab;
import net.minecraft.network.chat.Component;

public class UpgradeEditTab extends EditTab {

    public final UpgradeEditScreen parent;

    public UpgradeEditTab(Component title, UpgradeEditScreen parent) {
        super(title);
        this.parent = parent;
    }
}
