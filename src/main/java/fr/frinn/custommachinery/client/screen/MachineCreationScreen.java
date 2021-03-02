package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.widget.TabButton;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
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
        super(new StringTextComponent("Machine Creation Screen"));
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
        this.addTab(new TranslationTextComponent("custommachinery.gui.machinecreation.baseinfos"), this.baseInfoScreen);
        this.addTab(new TranslationTextComponent("custommachinery.gui.machinecreation.appearance"), this.machineAppearanceScreen);
        this.addTab(new TranslationTextComponent("custommachinery.gui.machinecreation.components"), this.machineComponentScreen);
        this.addTab(new TranslationTextComponent("custommachinery.gui.machinecreation.gui"), this.machineGuiCreationScreen);

        this.tabs.forEach((screen, button) -> screen.init(this.minecraft, this.width, this.height));

        if(this.selectedTab == null)
            this.setSelectedTab(this.baseInfoScreen);
        else
            this.setSelectedTab(this.selectedTab);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);

        if(this.selectedTab == null)
            this.selectedTab = this.baseInfoScreen;

        this.selectedTab.render(matrix, mouseX, mouseY, partialTicks);

        super.render(matrix, mouseX, mouseY, partialTicks);


    }

    @ParametersAreNonnullByDefault
    @Override
    public void renderBackground(MatrixStack matrix) {
        Minecraft.getInstance().getTextureManager().bindTexture(MACHINE_CREATOR_TEXTURE);
        blit(matrix, this.xPos, this.yPos, 0, 0, this.xSize, this.ySize);
    }

    private void addTab(ITextComponent name, Screen screen) {
        TabButton tabButton = this.addButton(new TabButton(
                this.xPos + 5 + 30 * this.tabs.size(),
                this.yPos - 28,
                StringTextComponent.EMPTY,
                (button) -> this.setSelectedTab(screen),
                (button, matrix, mouseX, mouseY) -> this.renderTooltip(matrix, name, mouseX, mouseY)
        ));
        if(this.selectedTab == screen)
            tabButton.setSelected(true);
        this.tabs.put(screen, tabButton);
    }

    private void setSelectedTab(Screen screen) {
        if(this.selectedTab != null)
            this.children.remove(this.selectedTab);
        this.selectedTab = screen;
        this.children.add(this.selectedTab);
        this.tabs.forEach((tabScreen, tabButton) -> tabButton.setSelected(false));
        this.tabs.get(screen).setSelected(true);
    }
}
