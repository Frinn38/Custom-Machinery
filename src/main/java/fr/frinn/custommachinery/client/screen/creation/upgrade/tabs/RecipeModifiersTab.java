package fr.frinn.custommachinery.client.screen.creation.upgrade.tabs;

import fr.frinn.custommachinery.client.screen.creation.upgrade.UpgradeEditScreen;
import net.minecraft.network.chat.Component;

public class RecipeModifiersTab extends UpgradeEditTab {

    public RecipeModifiersTab(UpgradeEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.upgrade.tab.recipe_modifiers"), parent);
    }
}
