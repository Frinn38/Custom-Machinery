package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.client.screen.creation.MachineComponentListWidget;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentCreationPopup;
import fr.frinn.custommachinery.client.screen.creation.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.MachineComponentBuilderRegistry;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ComponentTab extends MachineEditTab {

    private final MachineComponentListWidget componentList;
    private final Button create;
    private final Button edit;
    private final Button delete;

    public ComponentTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.components"), parent);
        this.layout.rowSpacing(5).columnSpacing(10);
        this.layout.defaultCellSetting().paddingTop(5);
        GridLayout.RowHelper row = this.layout.createRowHelper(3);
        LayoutSettings center = row.defaultCellSetting().alignHorizontallyCenter();
        this.componentList = row.addChild(new MachineComponentListWidget(parent.x, parent.y + 10, parent.xSize - 10, parent.ySize - 50, 40, this), 3, center);
        this.componentList.setup(parent.getBuilder());
        this.create = row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.create"), button -> this.create()).size(60, 20).build(), center);
        this.edit = row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.edit"), button -> this.edit()).size(60, 20).build(), center);
        this.delete = row.addChild(Button.builder(Component.translatable("custommachinery.gui.creation.delete"), button -> this.delete()).size(60, 20).build(), center);
        this.setupButtons();
    }

    public void setupButtons() {
        if(this.componentList.getSelected() != null) {
            this.edit.active = true;
            this.delete.active = true;
        } else {
            this.edit.active = false;
            this.delete.active = false;
        }
    }

    public void create() {
        this.parent.openPopup(new ComponentCreationPopup(this.parent, () -> this.componentList.setup(this.parent.getBuilder())));
    }

    public void edit() {
        MachineComponentListWidget.MachineComponentEntry entry = this.componentList.getSelected();
        if(entry != null) {
            PopupScreen componentEditPopup = getComponentEditPopup(entry.getTemplate(), entry);
            if(componentEditPopup != null)
                this.parent.openPopup(componentEditPopup);
        }
    }

    public void delete() {
        ConfirmPopup popup = new ConfirmPopup(this.parent, 128, 96, () -> {
            MachineComponentListWidget.MachineComponentEntry entry = this.componentList.getSelected();
            if(entry != null) {
                this.parent.getBuilder().getComponents().remove(entry.getTemplate());
                this.parent.setChanged();
                this.componentList.setup(this.parent.getBuilder());
            }
        });
        popup.title(Component.translatable("custommachinery.gui.creation.components.delete.title"));
        popup.text(Component.translatable("custommachinery.gui.creation.components.delete.info"));
        this.parent.openPopup(popup);
    }

    @Nullable
    private <C extends IMachineComponent, T extends IMachineComponentTemplate<C>> PopupScreen getComponentEditPopup(T template, MachineComponentListWidget.MachineComponentEntry entry) {
        IMachineComponentBuilder<C, T> builder = MachineComponentBuilderRegistry.getBuilder(template.getType());
        if(builder == null)
            return null;
        return builder.makePopup(this.parent, template, t -> {
            entry.setTemplate(t);
            this.parent.setChanged();
        });
    }
}
