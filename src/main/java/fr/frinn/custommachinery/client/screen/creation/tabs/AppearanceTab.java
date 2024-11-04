package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.client.screen.creation.AppearanceListWidget;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditTabNavigationBar;
import fr.frinn.custommachinery.client.screen.creation.MachineTabManager;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppearanceTab extends MachineEditTab {

    private final TabManager tabManager;
    private final MachineEditTabNavigationBar bar;

    public AppearanceTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.appearance"), parent);
        RowHelper row = this.layout.createRowHelper(1);
        row.defaultCellSetting().paddingHorizontal(0);

        //RESET
        //WidgetSprites resetSprites = new WidgetSprites(CustomMachinery.rl("creation/reset_button"), CustomMachinery.rl("creation/reset_button_hovered"));
        //ImageButton resetButton = row.addChild(new ImageButton(0, 0, 20, 20, resetSprites, button -> this.resetAppearance()));
        //resetButton.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance.reset.tooltip")));

        //TABS
        this.tabManager = new MachineTabManager(parent::addRenderableWidget, parent::removeWidget);
        List<MachineEditTab> tabs = new ArrayList<>();
        tabs.add(new SpecificAppearanceTab(Component.translatable("custommachinery.craftingstatus.default"), parent, null));
        Arrays.stream(MachineStatus.values()).forEach(status -> tabs.add(new SpecificAppearanceTab(Component.translatable("custommachinery.craftingstatus." + status.getSerializedName()), parent, status)));
        this.bar = row.addChild(new MachineEditTabNavigationBar(parent.xSize, this.tabManager, tabs), row.newCellSettings().alignVerticallyTop().alignHorizontallyCenter().paddingTop(5));
        this.bar.setRectangle(parent.xSize - 10, 20, 0, 0);

        row.addChild(new SeparationWidget(parent.xSize, 5, CustomMachinery.rl("textures/gui/base_background.png"), parent.xSize, 192), row.newCellSettings().paddingBottom(parent.ySize - 30).alignVerticallyTop());
    }

    @Override
    public void opened() {
        if(this.tabManager.getCurrentTab() == null)
            this.bar.selectTab(0, false);
        else {
            this.tabManager.getCurrentTab().visitChildren(this.parent::addRenderableWidget);
        }
    }

    @Override
    public void closed() {
        if(this.tabManager.getCurrentTab() != null)
            this.tabManager.getCurrentTab().visitChildren(this.parent::removeWidget);
    }

    @Override
    public void doLayout(ScreenRectangle rectangle) {
        super.doLayout(rectangle);
        this.tabManager.setTabArea(new ScreenRectangle(this.parent.x + 5, this.parent.y + 30, this.parent.xSize - 10, this.parent.ySize - 35));
    }

    public void resetAppearance() {
        if(this.tabManager.getCurrentTab() instanceof SpecificAppearanceTab appearanceTab) {
            Component status = Component.translatable("custommachinery.craftingstatus." + (appearanceTab.status == null ? "default" : appearanceTab.status.getSerializedName())).withStyle(ChatFormatting.GOLD);
            this.parent.openPopup(new ConfirmPopup(this.parent, 116, 96, () -> {
                this.parent.getBuilder().getAppearance(appearanceTab.status).reset();
                appearanceTab.appearanceList.init();
            })
            .title(Component.translatable("custommachinery.gui.creation.appearance.reset.title"))
            .text(Component.translatable("custommachinery.gui.creation.appearance.reset.text", status)));
        }
    }

    private class SpecificAppearanceTab extends MachineEditTab {

        @Nullable
        private final MachineStatus status;
        private final AppearanceListWidget appearanceList;

        public SpecificAppearanceTab(Component title, MachineEditScreen parent, @Nullable MachineStatus status) {
            super(title, parent);
            this.status = status;
            RowHelper row = this.layout.createRowHelper(1);
            this.appearanceList = row.addChild(new AppearanceListWidget(0, 0, parent.xSize - 20, parent.ySize - 40, 30, () -> parent.getBuilder().getAppearance(status), this.parent));
        }
    }

    private class SeparationWidget extends AbstractWidget {

        private final ResourceLocation texture;
        private final int textureWidth;
        private final int textureHeight;

        public SeparationWidget(int width, int height, ResourceLocation texture, int textureWidth, int textureHeight) {
            super(0, 0, width, height, Component.empty());
            this.texture = texture;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(this.texture, this.getX(), this.getY(), -1, 0, 0, this.getWidth(), this.getHeight(), this.textureWidth, this.textureHeight);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }
}
