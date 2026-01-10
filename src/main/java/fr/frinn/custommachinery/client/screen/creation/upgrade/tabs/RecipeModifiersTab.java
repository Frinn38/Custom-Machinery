package fr.frinn.custommachinery.client.screen.creation.upgrade.tabs;

import fr.frinn.custommachinery.client.screen.creation.upgrade.UpgradeEditScreen;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

public class RecipeModifiersTab extends UpgradeEditTab {

    public RecipeModifiersTab(UpgradeEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.upgrade.tab.recipe_modifiers"), parent);
        GridLayout.RowHelper row = this.layout.createRowHelper(1);
        LayoutSettings center = row.defaultCellSetting().alignHorizontallyCenter();
        //row.addChild(new RecipeModifierListWidget(parent.x, parent.y + 10, parent.xSize - 10, parent.ySize - 50, 40, parent.getBuilder()), center);
    }
}
