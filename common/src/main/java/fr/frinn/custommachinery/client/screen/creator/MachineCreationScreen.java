package fr.frinn.custommachinery.client.screen.creator;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import fr.frinn.custommachinery.client.screen.widget.custom.ButtonWidget;
import fr.frinn.custommachinery.client.screen.widget.custom.ListWidget;
import fr.frinn.custommachinery.client.screen.widget.custom.MachineList;
import fr.frinn.custommachinery.client.screen.widget.custom.MachineList.MachineEntry;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.network.CRemoveMachinePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MachineCreationScreen extends BaseScreen {

    public static final MachineCreationScreen INSTANCE = new MachineCreationScreen();
    private static final Component CREATE = new TranslatableComponent("custommachinery.gui.creation.create");
    private static final Component SAVE = new TranslatableComponent("custommachinery.gui.creation.save");
    private static final Component DELETE = new TranslatableComponent("custommachinery.gui.creation.delete");

    private static final ResourceLocation CREATE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/create_icon.png");
    private static final ResourceLocation SAVE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/save_icon.png");
    private static final ResourceLocation DELETE_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/delete_icon.png");

    private ListWidget<CustomMachineBuilder, MachineEntry> machineList;
    private ButtonWidget create;
    private ButtonWidget save;
    private ButtonWidget delete;

    private final List<CustomMachineBuilder> builders = new ArrayList<>();
    @Nullable
    private CustomMachineBuilder selected = null;

    public MachineCreationScreen() {
        super(new TextComponent("Machine Creator"), 72, 166);
        CustomMachinery.MACHINES.values().stream().map(CustomMachineBuilder::new).forEach(this.builders::add);
    }

    @Override
    protected void init() {
        super.init();
        baseMoveDraggingArea();
        baseSizeDraggingArea(3);
        this.machineList = addCustomWidget(new MachineList(() -> this.getX() + 4, () -> this.getY() + 4, 64, 136)
                .selectionCallback(this::select)
        );
        this.builders.forEach(this.machineList::add);
        if(this.selected != null)
            this.machineList.setSelected(this.selected);
        this.create = addCustomWidget(new ButtonWidget(() -> this.getX() + 4, () -> this.getY() + 142, 20, 20)
                        .title(CREATE, false)
                        .texture(CREATE_TEXTURE)
                        .callback(this::create)
                        .tooltip(CREATE)
        );
        this.save = addCustomWidget(new ButtonWidget(() -> this.getX() + 26, () -> this.getY() + 142, 20, 20)
                        .title(SAVE, false)
                        .texture(SAVE_TEXTURE)
                        .callback(this::save)
                        .tooltip(SAVE)
        );
        this.delete = addCustomWidget(new ButtonWidget(() -> this.getX() + 48, () -> this.getY() + 142, 20, 20)
                        .title(DELETE, false)
                        .texture(DELETE_TEXTURE)
                        .callback(button -> {
                            if(this.selected == null)
                                return;
                            this.openPopup(new ConfirmPopup(190, 100, this::delete)
                                    .text(new TextComponent("The following machine will be deleted"),
                                            this.selected.getName(),
                                            new TextComponent("This can't be undone!"))
                            );
                        })
                        .tooltip(DELETE)
        );
    }

    @Override
    public void renderBackground(PoseStack pose) {
        blankBackground(pose, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    private void select(@Nullable CustomMachineBuilder selected) {
        this.selected = selected;
    }

    private void create(ButtonWidget button) {

    }

    private void save(ButtonWidget button) {

    }

    private void delete() {
        if(this.selected == null)
            return;
        this.builders.remove(this.selected);
        ResourceLocation id = this.selected.getLocation().getId();
        CustomMachinery.MACHINES.remove(id);
        new CRemoveMachinePacket(id).sendToServer();
        this.selected = null;
        this.init();
    }

    public void refreshMachineList() {
        this.builders.removeIf(builder -> !CustomMachinery.MACHINES.containsKey(builder.getLocation().getId()));
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            if(this.builders.stream().noneMatch(builder -> builder.getLocation().getId().equals(id)))
                this.builders.add(new CustomMachineBuilder(machine));
        });
        this.init();
    }
}
