package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.creation.tabs.MachineEditTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MachineTabManager extends TabManager {

    private final MachineEditScreen parent;
    private GridLayout toolboxLayout;
    private ImageWidget toolboxBackground;

    public MachineTabManager(MachineEditScreen parent) {
        super(parent::addRenderableWidget, parent::removeWidget);
        this.parent = parent;
    }

    @Override
    public void setTabArea(ScreenRectangle tabArea) {
        super.setTabArea(tabArea);
        if(this.toolboxLayout != null)
            this.setToolboxPos();
    }

    @Override
    public void setCurrentTab(Tab tab, boolean playClickSound) {
        if(this.getCurrentTab() instanceof MachineEditTab editTab) {
            editTab.closed();
            if(this.toolboxLayout != null)
                this.toolboxLayout.visitWidgets(this.parent::removeWidget);
            if(this.toolboxBackground != null)
                this.parent.removeWidget(this.toolboxBackground);
        }
        super.setCurrentTab(tab, playClickSound);
        if(tab instanceof MachineEditTab editTab) {
            editTab.opened();
            if(!editTab.getToolButtons().isEmpty())
                this.setupToolbox(editTab.getToolButtons());
        }
    }

    private void setupToolbox(List<AbstractWidget> toolButtons) {
        int columns = (int)Math.ceil(toolButtons.size() / 5D);
        this.toolboxLayout = new GridLayout();
        RowHelper row = this.toolboxLayout.spacing(0).createRowHelper(columns);
        row.defaultCellSetting().paddingBottom(1).paddingRight(1).alignHorizontallyCenter().alignVerticallyMiddle();
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.toolbox"), Minecraft.getInstance().font), columns, row.newCellSettings().paddingBottom(5));
        toolButtons.forEach(row::addChild);
        this.toolboxLayout.arrangeElements();
        this.toolboxBackground = this.parent.addRenderableWidget(ImageWidget.sprite(this.toolboxLayout.getWidth() + 10, this.toolboxLayout.getHeight() + 10, CustomMachinery.rl("background")));
        this.toolboxBackground.active = false;
        this.toolboxLayout.visitWidgets(this.parent::addRenderableWidget);
        this.setToolboxPos();
    }

    private void setToolboxPos() {
        this.toolboxLayout.setPosition(this.parent.x - this.toolboxLayout.getWidth() - 8, this.parent.y + 85);
        this.toolboxBackground.setPosition(this.parent.x - this.toolboxLayout.getWidth() - 13, this.parent.y + 80);
    }
}
