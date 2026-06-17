package fr.frinn.custommachinery.client.screen.creation.upgrade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.common.config.CMConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.util.Objects;

public class UpgradeCreationScreen extends BaseScreen {

    private UpgradeListWidget upgradeList;
    private Button edit;
    private Button open;
    private Button delete;

    public UpgradeCreationScreen() {
        super(Component.literal("Upgrade creation"), 256, 192);
    }

    public void sort(UpgradeListSorting sorting) {
        CMConfig.CONFIG.sortUpgradeList.set(sorting);
        CMConfig.CONFIG.sortUpgradeList.save();
        this.upgradeList.sort();
    }

    public void create() {
        this.openPopup(new CreateUpgradePopup(this), "Creation popup");
    }

    public void edit() {
        UpgradeListWidget.UpgradeEntry entry = this.upgradeList.getSelected();
        if(entry != null)
            Minecraft.getInstance().setScreen(new UpgradeEditScreen(this, 288, 210, entry.getLocation(), entry.getUpgrade()));
    }

    public void open() {
        UpgradeListWidget.UpgradeEntry entry = this.upgradeList.getSelected();
        if(entry != null && Minecraft.getInstance().getSingleplayerServer() != null) {
            try {
                File file = Objects.requireNonNull(entry.getLocation().getFile(Minecraft.getInstance().getSingleplayerServer()));
                Util.getPlatform().openUri(file.toURI());
            } catch (NullPointerException e) {
                CustomMachinery.LOGGER.warn("Can't open upgrade json for upgrade: {}", entry.getLocation().id());
            }
        }
    }

    public void delete() {
        UpgradeListWidget.UpgradeEntry entry = this.upgradeList.getSelected();
        if(entry != null)
            this.openPopup(new DeleteUpgradePopup(this, entry.getLocation(), entry.getUpgrade()), "Delete upgrade");
    }

    public void reloadList() {
        this.upgradeList.reload();
    }

    @Override
    protected void init() {
        super.init();
        GridLayout layout = new GridLayout(this.x, this.y);
        layout.defaultCellSetting().padding(5);
        GridLayout.RowHelper row = layout.createRowHelper(4);
        LayoutSettings center = row.newCellSettings().alignHorizontallyCenter();

        //Sort
        CycleButton<UpgradeListSorting> sorter = row.addChild(CycleButton.<UpgradeListSorting>builder(v -> Component.literal(v.name()))
                .withValues(UpgradeListSorting.values())
                .displayOnlyValue()
                .withInitialValue(CMConfig.CONFIG.sortUpgradeList.get())
                .create(0, 0, 50, 20, Component.empty(), (button, sort) -> this.sort(sort)), 1, row.newCellSettings().alignHorizontallyLeft().paddingBottom(0));

        //Search
        EditBox search = row.addChild(new EditBox(this.font, 180, 20, Component.empty()), 3, row.newCellSettings().alignHorizontallyLeft().paddingBottom(0));
        search.setResponder(s -> {
            this.upgradeList.setFilterSearch(s);
            this.upgradeList.reload();
        });

        //List
        this.upgradeList = row.addChild(new UpgradeListWidget(0, 0, this.xSize - 10, this.ySize - 65, 30), 4, center);
        this.upgradeList.reload();

        //Buttons
        Button create = row.addChild(new Button.Builder(Component.translatable("custommachinery.gui.creation.create"), button -> this.create()).bounds(0, 0, 50, 20).build(), center);
        this.edit = row.addChild(new Button.Builder(Component.translatable("custommachinery.gui.creation.edit"), button -> this.edit()).bounds(0, 0, 50, 20).build(), center);
        this.open = row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.open"), button -> this.open()).bounds(0, 0, 50, 20).build(), center);
        this.delete = row.addChild(new Button.Builder(Component.translatable("custommachinery.gui.creation.delete"), button -> this.delete()).bounds(0, 0, 50, 20).build(), center);

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);
        UpgradeListWidget.UpgradeEntry entry = this.upgradeList.getSelected();
        if(entry == null) {
            Tooltip notSelected = Tooltip.create(Component.translatable("custommachinery.gui.creation.upgrade.not_selected"));
            this.edit.active = false;
            this.edit.setTooltip(notSelected);
            this.open.active = false;
            this.open.setTooltip(notSelected);
            this.delete.active = false;
            this.delete.setTooltip(notSelected);
            return;
        }
        if(entry.getLocation().canEdit()) {
            this.edit.active = true;
            this.edit.setTooltip(null);
            if(Minecraft.getInstance().getSingleplayerServer() != null) {
                this.open.active = true;
                this.open.setTooltip(null);
            } else {
                this.open.active = false;
                this.open.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.upgrade.cant_open_server")));
            }
            this.delete.active = true;
            this.delete.setTooltip(null);
        } else {
            this.edit.active = false;
            this.edit.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.upgrade.cant_edit")));
            this.open.active = false;
            this.open.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.upgrade.cant_open")));
            this.delete.active = false;
            this.delete.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.upgrade.cant_delete")));
        }
    }
}
