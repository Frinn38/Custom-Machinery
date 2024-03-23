package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.tabs.AppearanceTab;
import fr.frinn.custommachinery.client.screen.creation.tabs.BaseInfoTab;
import fr.frinn.custommachinery.client.screen.creation.tabs.ComponentTab;
import fr.frinn.custommachinery.client.screen.creation.tabs.GuiTab;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MachineEditScreen extends BaseScreen {

    private final MachineCreationScreen parent;
    private final CustomMachineBuilder builder;

    private TabManager tabManager;
    private MachineEditTabNavigationBar bar;

    public MachineEditScreen(MachineCreationScreen parent, int xSize, int ySize, CustomMachineBuilder builder) {
        super(Component.literal("Machine edit"), xSize, ySize);
        this.parent = parent;
        this.builder = builder;
    }

    public CustomMachineBuilder getBuilder() {
        return this.builder;
    }

    @Override
    protected void init() {
        super.init();
        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
        this.bar = this.addRenderableWidget(new MachineEditTabNavigationBar(this.xSize, this.tabManager, List.of(new BaseInfoTab(this), new AppearanceTab(this), new ComponentTab(this), new GuiTab(this))));
        this.bar.selectTab(0, false);
        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        if (this.bar == null)
            return;

        this.bar.bounds(this.x + 5, this.y - 20, this.xSize - 10, 20);
        this.bar.arrangeElements();
        this.tabManager.setTabArea(new ScreenRectangle(this.x, this.y, this.xSize, this.ySize));
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
        blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);
    }
}
