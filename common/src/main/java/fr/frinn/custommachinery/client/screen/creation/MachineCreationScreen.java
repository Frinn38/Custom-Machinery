package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineListWidget.MachineEntry;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class MachineCreationScreen extends BaseScreen {

    private MachineListWidget machineList;
    private Button create;
    private Button edit;
    private Button delete;

    public MachineCreationScreen(int xSize, int ySize) {
        super(Component.literal("Machine creation"), xSize, ySize);
    }

    public void create() {
        this.openPopup(new CreateMachinePopup(this));
    }

    public void edit() {
        MachineEntry entry = this.machineList.getSelected();
        if(entry != null)
            Minecraft.getInstance().setScreen(new MachineEditScreen(this, 256, 192, new CustomMachineBuilder(entry.getMachine())));
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
        this.machineList = this.addRenderableWidget(new MachineListWidget(this, this.mc, this.xSize - 20, this.ySize - 30, this.y + 5, this.y + this.ySize - 35, 30));
        this.machineList.setLeftPos(this.x + 10);
        this.machineList.reload();
        this.create = this.addRenderableWidget(new Button.Builder(Component.translatable("custommachinery.gui.creation.create"), button -> this.create()).bounds(this.x + 10, this.y + this.ySize - 30, 72, 20).build());
        this.edit = this.addRenderableWidget(new Button.Builder(Component.translatable("custommachinery.gui.creation.edit"), button -> this.edit()).bounds(this.x + 92, this.y + this.ySize - 30, 72, 20).build());
        this.delete = this.addRenderableWidget(new Button.Builder(Component.translatable("custommachinery.gui.creation.delete"), button -> this.delete()).bounds(this.x + 174, this.y + this.ySize - 30, 72, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
        blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);
        MachineEntry entry = this.machineList.getSelected();
        if(entry != null && entry.getMachine().getLocation().canEdit()) {
            this.edit.active = true;
            this.edit.setTooltip(null);
            this.delete.active = true;
            this.delete.setTooltip(null);
        } else {
            Component tooltip = entry != null && entry.getMachine().getLocation().canEdit() ? Component.translatable("custommachinery.gui.creation.notselected") : Component.translatable("custommachinery.gui.creation.cantdelete");
            this.edit.active = false;
            this.edit.setTooltip(Tooltip.create(tooltip));
            this.delete.active = false;
            this.delete.setTooltip(Tooltip.create(tooltip));
        }
    }
}
