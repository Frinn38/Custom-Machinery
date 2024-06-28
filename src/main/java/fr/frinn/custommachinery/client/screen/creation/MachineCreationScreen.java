package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineListWidget.MachineEntry;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.util.Objects;

public class MachineCreationScreen extends BaseScreen {

    private MachineListWidget machineList;
    private Button create;
    private Button edit;
    private Button open;
    private Button delete;

    public MachineCreationScreen() {
        super(Component.literal("Machine creation"), 256, 192);
    }

    public void create() {
        this.openPopup(new CreateMachinePopup(this), "Creation popup");
    }

    public void edit() {
        MachineEntry entry = this.machineList.getSelected();
        if(entry != null)
            Minecraft.getInstance().setScreen(new MachineEditScreen(this, 288, 210, new CustomMachineBuilder(entry.getMachine())));
    }

    public void open() {
        MachineEntry entry = this.machineList.getSelected();
        if(entry != null) {
            try {
                MinecraftServer server = Objects.requireNonNull(Minecraft.getInstance().getSingleplayerServer());
                File file = Objects.requireNonNull(entry.getMachine().getLocation().getFile(server));
                Util.getPlatform().openUri(file.toURI());
            } catch (NullPointerException e) {
                CustomMachinery.LOGGER.warn("Can't open machine json for machine: {}", entry.getMachine().getId());
            }
        }
    }

    public void delete() {
        MachineEntry entry = this.machineList.getSelected();
        if(entry != null)
            this.openPopup(new DeleteMachinePopup(this, entry.getMachine()), "Delete machine");
    }

    public void reloadList() {
        this.machineList.reload();
    }

    @Override
    protected void init() {
        super.init();
        GridLayout layout = new GridLayout(this.x, this.y);
        layout.defaultCellSetting().padding(5);
        GridLayout.RowHelper row = layout.createRowHelper(4);
        LayoutSettings center = row.newCellSettings().alignHorizontallyCenter();
        this.machineList = row.addChild(new MachineListWidget(this, 0, 0, this.xSize - 10, this.ySize - 40, 30), 4, center);
        this.machineList.reload();
        this.create = row.addChild(new Button.Builder(Component.translatable("custommachinery.gui.creation.create"), button -> this.create()).bounds(0, 0, 50, 20).build(), center);
        this.edit = row.addChild(new Button.Builder(Component.translatable("custommachinery.gui.creation.edit"), button -> this.edit()).bounds(0, 0, 50, 20).build(), center);
        this.open = row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.open"), button -> this.open()).bounds(0, 0, 50, 20).build(), center);
        this.delete = row.addChild(new Button.Builder(Component.translatable("custommachinery.gui.creation.delete"), button -> this.delete()).bounds(0, 0, 50, 20).build(), center);

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(graphics, mouseX, mouseY, partialTicks);
        blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);
        MachineEntry entry = this.machineList.getSelected();
        if(entry == null) {
            Tooltip notSelected = Tooltip.create(Component.translatable("custommachinery.gui.creation.notselected"));
            this.edit.active = false;
            this.edit.setTooltip(notSelected);
            this.open.active = false;
            this.open.setTooltip(notSelected);
            this.delete.active = false;
            this.delete.setTooltip(notSelected);
            return;
        }
        if(entry.getMachine().getLocation().canEdit()) {
            this.edit.active = true;
            this.edit.setTooltip(null);
            if(Minecraft.getInstance().getSingleplayerServer() != null) {
                this.open.active = true;
                this.open.setTooltip(null);
            } else {
                this.open.active = false;
                this.open.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.cantopenserver")));
            }
            this.delete.active = true;
            this.delete.setTooltip(null);
        } else {
            this.edit.active = false;
            this.edit.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.cantedit")));
            this.open.active = false;
            this.open.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.cantopen")));
            this.delete.active = false;
            this.delete.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.cantdelete")));
        }
    }
}
