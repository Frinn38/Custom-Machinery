package fr.frinn.custommachinery.client.screen.creator;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.widget.TexturedButton;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.network.CAddMachinePacket;
import fr.frinn.custommachinery.common.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineLoadingScreen extends Screen {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/machine_list_background.png");
    private static final TranslatableComponent SAVE_MACHINE = new TranslatableComponent("custommachinery.gui.machineloading.save");
    private static final TranslatableComponent NO_MACHINE = new TranslatableComponent("custommachinery.gui.machineloading.nomachine");
    private static final TranslatableComponent CANT_SAVE_MACHINE = new TranslatableComponent("custommachinery.gui.machineloading.cantsave");
    private static final TranslatableComponent DELETE_MACHINE = new TranslatableComponent("custommachinery.gui.machineloading.delete");
    private static final TranslatableComponent CANT_DELETE_MACHINE = new TranslatableComponent("custommachinery.gui.machineloading.cantdelete");

    public static final MachineLoadingScreen INSTANCE = new MachineLoadingScreen();

    private int xSize = 72;
    private int ySize = 166;
    private int xPos;
    private int yPos;

    private MachineList machineList;
    private Map<CustomMachineBuilder, MachineCreationScreen> machineCreationScreens = new HashMap<>();
    private CustomMachineBuilder selectedMachine;

    private Button createButton;
    private Button saveButton;
    private Button deleteButton;

    public MachineLoadingScreen() {
        super(TextComponent.EMPTY);
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            CustomMachineBuilder machineBuilder = new CustomMachineBuilder(machine);
            this.machineCreationScreens.put(machineBuilder, new MachineCreationScreen(machineBuilder));
        });
    }

    @Override
    protected void init() {
        this.xPos = (this.width - 256) / 2 - this.xSize - 5;
        this.yPos = (this.height - 166) / 2;
        this.machineList = new MachineList(this.minecraft, this.xSize - 5, this.ySize - 28, this.xPos + 3, this.yPos + 1, 20, this);
        this.machineCreationScreens.forEach((machineBuilder, screen) -> this.machineList.addMachineEntry(machineBuilder));
        ((List<GuiEventListener>)this.children()).add(this.machineList);
        this.machineCreationScreens.forEach((machineBuilder, screen) -> screen.init(this.minecraft, this.width, this.height));
        this.machineList.children().forEach(entry -> {
            if(this.selectedMachine != null && this.selectedMachine == entry.getMachineBuilder())
                this.machineList.setSelected(entry);
        });
        this.createButton = this.addRenderableWidget(new TexturedButton(
                this.xPos + 4,
                this.yPos + 141,
                20,
                20,
                TextComponent.EMPTY,
                new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/create_icon.png"),
                (button) -> this.create(),
                (button, matrix, mouseX, mouseY) -> this.renderTooltip(matrix, new TranslatableComponent("custommachinery.gui.machineloading.create"), mouseX, mouseY)
        ));
        this.saveButton = this.addRenderableWidget(new TexturedButton(
                this.xPos + 26,
                this.yPos + 141,
                20,
                20,
                TextComponent.EMPTY,
                new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/save_icon.png"),
                (button) -> this.save(),
                (button, matrix, mouseX, mouseY) -> {
                    Component tooltip = this.selectedMachine == null ? NO_MACHINE : this.selectedMachine.getLocation().getLoader() == MachineLocation.Loader.DATAPACK ? SAVE_MACHINE : CANT_SAVE_MACHINE;
                    this.renderTooltip(matrix, tooltip, mouseX, mouseY);
                }
        ));
        this.deleteButton = this.addRenderableWidget(new TexturedButton(
                this.xPos + 48,
                this.yPos + 141,
                20,
                20,
                TextComponent.EMPTY,
                new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/delete_icon.png"),
                (button) -> this.delete(),
                (button, matrix, mouseX, mouseY) -> {
                    Component tooltip = this.selectedMachine == null ? NO_MACHINE : this.selectedMachine.getLocation().getLoader() == MachineLocation.Loader.DATAPACK ? DELETE_MACHINE : CANT_DELETE_MACHINE;
                    this.renderTooltip(matrix, tooltip, mouseX, mouseY);
                }
        ));
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);
        this.machineList.render(matrix, mouseX, mouseY, partialTicks);
        super.render(matrix, mouseX, mouseY, partialTicks);
        if(this.selectedMachine != null && this.machineCreationScreens.containsKey(this.selectedMachine))
            this.machineCreationScreens.get(this.selectedMachine).render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(PoseStack matrix) {
        super.renderBackground(matrix);

        ClientHandler.bindTexture(BACKGROUND_TEXTURE);
        blit(matrix, this.xPos, this.yPos, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);
    }

    protected void setSelectedMachine(CustomMachineBuilder machine) {
        if(this.selectedMachine != null && this.machineCreationScreens.containsKey(this.selectedMachine))
            this.children().remove(this.machineCreationScreens.get(this.selectedMachine));
        this.selectedMachine = machine;
        if(this.selectedMachine != null && this.machineCreationScreens.containsKey(this.selectedMachine))
            ((List<GuiEventListener>)this.children()).add(this.machineCreationScreens.get(this.selectedMachine));
    }

    private void create() {
        CustomMachineBuilder newMachine = new CustomMachineBuilder().setLocation(MachineLocation.fromDatapack(new ResourceLocation(CustomMachinery.MODID, "new_machine"), ""));
        MachineCreationScreen newMachineScreen = new MachineCreationScreen(newMachine);
        newMachineScreen.init(this.minecraft, this.width, this.height);
        this.machineCreationScreens.put(newMachine, newMachineScreen);
        this.machineList.setSelected(this.machineList.children().get(this.machineList.addMachineEntry(newMachine)));
    }

    private void save() {
        CustomMachine machineToSave = this.machineList.getSelected() == null ? null : this.machineList.getSelected().getMachineBuilder().build();
        if(machineToSave != null && machineToSave.getId() != null && machineToSave.getLocation().getLoader() == MachineLocation.Loader.DATAPACK) {
            if(machineToSave.getLocation().getPackName().equals(""))
                return;
            ResourceLocation id = machineToSave.getId();
            new CAddMachinePacket(id, machineToSave, true, true).sendToServer();
        }
    }

    private void delete() {
        CustomMachineBuilder machineToDelete = this.machineList.getSelected() == null ? null : this.machineList.getSelected().getMachineBuilder();
        if(machineToDelete != null) {
            CustomMachinery.MACHINES.remove(machineToDelete.getLocation().getId());
            this.machineList.removeMachineEntry(machineToDelete);
            this.machineCreationScreens.remove(machineToDelete);
            if(machineToDelete.getLocation().getLoader() == MachineLocation.Loader.DATAPACK)
                FileUtils.deleteMachineJSON(Minecraft.getInstance().player.getServer(), machineToDelete.getLocation());
        }
    }

    public Map<ResourceLocation, CustomMachineBuilder> getBuilders() {
        Map<ResourceLocation, CustomMachineBuilder> builders = new HashMap<>();
        this.machineCreationScreens.keySet().forEach(builder -> builders.put(builder.getLocation().getId(), builder));
        return builders;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
