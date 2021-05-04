package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.widget.TexturedButton;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.network.CAddMachinePacket;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

public class MachineLoadingScreen extends Screen {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/machine_list_background.png");
    private static final TranslationTextComponent SAVE_MACHINE = new TranslationTextComponent("custommachinery.gui.machineloading.save");
    private static final TranslationTextComponent NO_MACHINE = new TranslationTextComponent("custommachinery.gui.machineloading.nomachine");
    private static final TranslationTextComponent CANT_SAVE_MACHINE = new TranslationTextComponent("custommachinery.gui.machineloading.cantsave");
    private static final TranslationTextComponent DELETE_MACHINE = new TranslationTextComponent("custommachinery.gui.machineloading.delete");
    private static final TranslationTextComponent CANT_DELETE_MACHINE = new TranslationTextComponent("custommachinery.gui.machineloading.cantdelete");

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
        super(StringTextComponent.EMPTY);
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
        this.children.add(this.machineList);
        this.machineCreationScreens.forEach((machineBuilder, screen) -> screen.init(this.minecraft, this.width, this.height));
        this.machineList.getEventListeners().forEach(entry -> {
            if(this.selectedMachine != null && this.selectedMachine == entry.getMachineBuilder())
                this.machineList.setSelected(entry);
        });
        this.createButton = this.addButton(new TexturedButton(
                this.xPos + 4,
                this.yPos + 141,
                20,
                20,
                StringTextComponent.EMPTY,
                new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/create_icon.png"),
                (button) -> this.create(),
                (button, matrix, mouseX, mouseY) -> this.renderTooltip(matrix, new TranslationTextComponent("custommachinery.gui.machineloading.create"), mouseX, mouseY)
        ));
        this.saveButton = this.addButton(new TexturedButton(
                this.xPos + 26,
                this.yPos + 141,
                20,
                20,
                StringTextComponent.EMPTY,
                new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/save_icon.png"),
                (button) -> this.save(),
                (button, matrix, mouseX, mouseY) -> {
                    ITextComponent tooltip = this.selectedMachine == null ? NO_MACHINE : this.selectedMachine.getLocation().getLoader() == MachineLocation.Loader.DATAPACK ? SAVE_MACHINE : CANT_SAVE_MACHINE;
                    this.renderTooltip(matrix, tooltip, mouseX, mouseY);
                }
        ));
        this.deleteButton = this.addButton(new TexturedButton(
                this.xPos + 48,
                this.yPos + 141,
                20,
                20,
                StringTextComponent.EMPTY,
                new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/delete_icon.png"),
                (button) -> this.delete(),
                (button, matrix, mouseX, mouseY) -> {
                    ITextComponent tooltip = this.selectedMachine == null ? NO_MACHINE : this.selectedMachine.getLocation().getLoader() == MachineLocation.Loader.DATAPACK ? DELETE_MACHINE : CANT_DELETE_MACHINE;
                    this.renderTooltip(matrix, tooltip, mouseX, mouseY);
                }
        ));
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);
        this.machineList.render(matrix, mouseX, mouseY, partialTicks);
        super.render(matrix, mouseX, mouseY, partialTicks);
        if(this.selectedMachine != null && this.machineCreationScreens.containsKey(this.selectedMachine))
            this.machineCreationScreens.get(this.selectedMachine).render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(MatrixStack matrix) {
        super.renderBackground(matrix);

        Minecraft.getInstance().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        blit(matrix, this.xPos, this.yPos, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);
    }

    protected void setSelectedMachine(CustomMachineBuilder machine) {
        if(this.selectedMachine != null && this.machineCreationScreens.containsKey(this.selectedMachine))
            this.children.remove(this.machineCreationScreens.get(this.selectedMachine));
        this.selectedMachine = machine;
        if(this.selectedMachine != null && this.machineCreationScreens.containsKey(this.selectedMachine))
            this.children.add(this.machineCreationScreens.get(this.selectedMachine));
    }

    private void create() {
        CustomMachineBuilder newMachine = new CustomMachineBuilder().setLocation(MachineLocation.fromDatapack(new ResourceLocation(CustomMachinery.MODID, "new_machine"), ""));
        MachineCreationScreen newMachineScreen = new MachineCreationScreen(newMachine);
        newMachineScreen.init(this.minecraft, this.width, this.height);
        this.machineCreationScreens.put(newMachine, newMachineScreen);
        this.machineList.setSelected(this.machineList.getEventListeners().get(this.machineList.addMachineEntry(newMachine)));
    }

    private void save() {
        CustomMachine machineToSave = this.machineList.getSelected() == null ? null : this.machineList.getSelected().getMachineBuilder().build();
        if(machineToSave != null && machineToSave.getId() != null && machineToSave.getLocation().getLoader() == MachineLocation.Loader.DATAPACK) {
            if(machineToSave.getLocation().getPackName().equals(""))
                return;
            ResourceLocation id = machineToSave.getId();
            NetworkManager.CHANNEL.sendToServer(new CAddMachinePacket(id, machineToSave, true, true));
        }
    }

    private void delete() {
        CustomMachineBuilder machineToDelete = this.machineList.getSelected() == null ? null : this.machineList.getSelected().getMachineBuilder();
        if(machineToDelete != null) {
            CustomMachinery.MACHINES.remove(machineToDelete.getLocation().getId());
            this.machineList.removeMachineEntry(machineToDelete);
            this.machineCreationScreens.remove(machineToDelete);
            if(machineToDelete.getLocation().getLoader() == MachineLocation.Loader.DATAPACK)
                FileUtils.deleteMachineJSON(machineToDelete.getLocation());
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
