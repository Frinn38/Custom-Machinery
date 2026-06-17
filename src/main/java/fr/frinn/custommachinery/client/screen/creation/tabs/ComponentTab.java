package fr.frinn.custommachinery.client.screen.creation.tabs;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.client.screen.creation.MachineComponentListWidget;
import fr.frinn.custommachinery.client.screen.creation.MachineComponentListWidget.MachineComponentEntry;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.component.ComponentCreationPopup;
import fr.frinn.custommachinery.client.screen.creation.component.IMachineComponentBuilder;
import fr.frinn.custommachinery.client.screen.creation.component.MachineComponentBuilderRegistry;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class ComponentTab extends MachineEditTab {

    public static final WidgetSprites CREATE_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/create_button"), CustomMachinery.rl("creation/create_button_hovered"));
    public static final WidgetSprites EDIT_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/edit_button"), CustomMachinery.rl("creation/edit_button_disabled"), CustomMachinery.rl("creation/edit_button_hovered"), CustomMachinery.rl("creation/edit_button_disabled_hovered"));
    public static final WidgetSprites COPY_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/copy_button"), CustomMachinery.rl("creation/copy_button_disabled"), CustomMachinery.rl("creation/copy_button_hovered"), CustomMachinery.rl("creation/copy_button_disabled_hovered"));
    public static final WidgetSprites DELETE_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/delete_button"), CustomMachinery.rl("creation/delete_button_disabled"), CustomMachinery.rl("creation/delete_button_hovered"), CustomMachinery.rl("creation/delete_button_disabled_hovered"));

    private final MachineComponentListWidget componentList;
    private final ImageButton create;
    private final ImageButton edit;
    private final ImageButton duplicate;
    private final ImageButton delete;

    public ComponentTab(MachineEditScreen parent) {
        super(Component.translatable("custommachinery.gui.creation.tab.components"), parent);
        this.layout.rowSpacing(5).columnSpacing(10);
        this.layout.defaultCellSetting().paddingTop(5);
        GridLayout.RowHelper row = this.layout.createRowHelper(4);
        LayoutSettings center = row.defaultCellSetting().alignHorizontallyCenter();
        this.componentList = row.addChild(new MachineComponentListWidget(parent.x, parent.y + 10, parent.xSize - 10, parent.ySize - 10, 40, this), 4, center);

        this.create = new ImageButton(0, 0, 20, 20, CREATE_SPRITES, button -> this.create());
        this.create.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.components.create.tooltip")));

        this.edit = new ImageButton(0, 0, 20, 20, EDIT_SPRITES, button -> this.edit());
        this.edit.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.components.edit.tooltip")));

        this.duplicate = new ImageButton(0, 0, 20, 20, COPY_SPRITES, button -> this.duplicate());
        this.duplicate.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.components.duplicate.tooltip")));

        this.delete = new ImageButton(0, 0, 20, 20, DELETE_SPRITES, button -> this.delete());
        this.delete.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.components.delete.tooltip")));

        this.componentList.setup(parent.getBuilder());//Call at the end because it will call setupButtons
    }

    @Override
    public List<AbstractWidget> getToolButtons() {
        return List.of(this.create, this.edit, this.duplicate, this.delete);
    }

    public void setupButtons() {
        if(this.componentList.getSelected() != null) {
            this.edit.active = true;
            this.duplicate.active = !this.componentList.getSelected().getTemplate().getType().isSingle();
            this.delete.active = true;
        } else {
            this.edit.active = false;
            this.duplicate.active = false;
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

    public void duplicate() {
        MachineComponentListWidget.MachineComponentEntry entry = this.componentList.getSelected();
        if(entry != null && !entry.getTemplate().getType().isSingle()) {
            MachineComponentEntry copy = entry.copy();
            this.parent.getBuilder().getComponents().add(copy.getTemplate());
            this.parent.setChanged();
            this.componentList.addEntry(copy);
            this.componentList.sort(Comparator.comparing(componentEntry -> componentEntry.getTemplate().getType().getId() + ":" + componentEntry.getTemplate().getId()));
            this.componentList.setSelected(copy);
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
