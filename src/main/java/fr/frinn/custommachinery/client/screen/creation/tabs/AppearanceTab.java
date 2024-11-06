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
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
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

    public static final WidgetSprites RESET_SPRITE = new WidgetSprites(CustomMachinery.rl("creation/reset_button"), CustomMachinery.rl("creation/reset_button_hovered"));

    private final TabManager tabManager;
    private final MachineEditTabNavigationBar bar;

    public AppearanceTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.appearance"), parent);
        RowHelper row = this.layout.createRowHelper(1);
        row.defaultCellSetting().paddingHorizontal(0);

        //TABS
        this.tabManager = new MachineTabManager(parent);
        List<MachineEditTab> tabs = new ArrayList<>();
        tabs.add(new SpecificAppearanceTab(Component.translatable("custommachinery.craftingstatus.default"), parent, null));
        Arrays.stream(MachineStatus.values()).forEach(status -> tabs.add(new SpecificAppearanceTab(Component.translatable("custommachinery.craftingstatus." + status.getSerializedName()), parent, status)));
        this.bar = row.addChild(new MachineEditTabNavigationBar(parent.xSize, this.tabManager, tabs), row.newCellSettings().alignVerticallyTop().alignHorizontallyCenter().paddingTop(5));
        this.bar.setRectangle(parent.xSize - 10, 20, 0, 0);

        row.addChild(new SeparationWidget(parent.xSize, 5), row.newCellSettings().paddingBottom(parent.ySize - 30).alignVerticallyTop());
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

    @Override
    public List<AbstractWidget> getToolButtons() {
        ImageButton reset = new ImageButton(20, 20, RESET_SPRITE, button -> this.resetAppearance(), Component.empty());
        reset.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance.reset.tooltip")));
        return List.of(reset);
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

        public SeparationWidget(int width, int height) {
            super(0, 0, width, height, Component.empty());
            this.texture = CustomMachinery.rl("creation/separation");
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blitSprite(this.texture, this.getX(), this.getY(), -1, this.getWidth(), this.getHeight());
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }
}
