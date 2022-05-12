package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.widget.TabButton;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineCreationScreen extends Screen {

    private static final ResourceLocation MACHINE_CREATOR_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/machine_creator.png");

    protected int xSize = 256;
    protected int ySize = 166;
    protected int xPos;
    protected int yPos;

    protected CustomMachineBuilder machine;

    private Map<Screen, TabButton> tabs = new HashMap<>();
    private Screen selectedTab;

    private BaseInfoScreen baseInfoScreen;
    private MachineAppearanceScreen machineAppearanceScreen;
    private MachineComponentScreen machineComponentScreen;
    private MachineGuiCreationScreen machineGuiCreationScreen;

    public MachineCreationScreen(CustomMachineBuilder machine) {
        super(new TextComponent("Machine Creation Screen"));
        this.machine = machine;
        this.baseInfoScreen = new BaseInfoScreen(this, machine);
        this.machineAppearanceScreen = new MachineAppearanceScreen(this, machine);
        this.machineComponentScreen = new MachineComponentScreen(this, machine);
        this.machineGuiCreationScreen = new MachineGuiCreationScreen(this, machine);
    }

    @Override
    protected void init() {
        this.xPos = (this.width - this.xSize) / 2;
        this.yPos = (this.height - this.ySize) / 2;

        this.tabs.clear();
        this.addTab(new TranslatableComponent("custommachinery.gui.machinecreation.baseinfos"), this.baseInfoScreen);
        this.addTab(new TranslatableComponent("custommachinery.gui.machinecreation.appearance"), this.machineAppearanceScreen);
        this.addTab(new TranslatableComponent("custommachinery.gui.machinecreation.components"), this.machineComponentScreen);
        this.addTab(new TranslatableComponent("custommachinery.gui.machinecreation.gui"), this.machineGuiCreationScreen);

        this.tabs.forEach((screen, button) -> screen.init(this.minecraft, this.width, this.height));

        if(this.selectedTab == null)
            this.setSelectedTab(this.baseInfoScreen);
        else
            this.setSelectedTab(this.selectedTab);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);

        super.render(matrix, mouseX, mouseY, partialTicks);

        if(this.selectedTab == null)
            this.selectedTab = this.baseInfoScreen;
        this.selectedTab.render(matrix, mouseX, mouseY, partialTicks);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void renderBackground(PoseStack matrix) {
        ClientHandler.bindTexture(MACHINE_CREATOR_TEXTURE);
        blit(matrix, this.xPos, this.yPos, 0, 0, this.xSize, this.ySize);
    }

    private void addTab(Component name, Screen screen) {
        int index = this.tabs.size() + 1;
        TabButton tabButton = this.addRenderableWidget(new TabButton(
                this.xPos + 5 + 30 * this.tabs.size(),
                this.yPos - 28,
                new TextComponent(index + ""),
                (button) -> this.setSelectedTab(screen),
                (button, matrix, mouseX, mouseY) -> this.renderTooltip(matrix, name, mouseX, mouseY)
        ));
        if(this.selectedTab == screen)
            tabButton.setSelected(true);
        this.tabs.put(screen, tabButton);
    }

    private void setSelectedTab(Screen screen) {
        if(this.selectedTab != null)
            this.removeWidget(this.selectedTab);
        this.selectedTab = screen;
        ((List<GuiEventListener>)this.children()).add(this.selectedTab);
        this.tabs.forEach((tabScreen, tabButton) -> tabButton.setSelected(false));
        this.tabs.get(screen).setSelected(true);
    }
}
