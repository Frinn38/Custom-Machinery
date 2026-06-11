package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.tabs.AppearanceTab;
import fr.frinn.custommachinery.client.screen.creation.tabs.ComponentTab;
import fr.frinn.custommachinery.client.screen.creation.tabs.GuiTab;
import fr.frinn.custommachinery.client.screen.creation.tabs.MachineBaseInfoTab;
import fr.frinn.custommachinery.client.screen.creation.tabs.TooltipsTab;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import fr.frinn.custommachinery.client.screen.widget.tabs.EditTabNavigationBar;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.network.CEditMachinePacket;
import fr.frinn.custommachinery.common.util.CMVerifier;
import fr.frinn.custommachinery.common.util.CMVerifier.Result;
import fr.frinn.custommachinery.common.util.CMVerifier.ResultBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.components.toasts.TutorialToast.Icons;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class MachineEditScreen extends BaseScreen {

    public static final WidgetSprites SAVE_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/save_button"), CustomMachinery.rl("creation/save_button_hovered"));
    public static final WidgetSprites CLOSE_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/close_button"), CustomMachinery.rl("creation/close_button_hovered"));
    public static final WidgetSprites WIKI_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/wiki_button"), CustomMachinery.rl("creation/wiki_button_hovered"));
    public static final ResourceLocation OK_TEXTURE = CustomMachinery.rl("textures/gui/base_status_running.png");
    public static final ResourceLocation ERROR_TEXTURE = CustomMachinery.rl("textures/gui/base_status_errored.png");

    private final CustomMachineBuilder builder;

    private boolean changed = false;

    private ImageButton save;
    private ImageButton close;
    private ImageButton wiki;
    private ImageWidget errors;
    private TabManager tabManager;
    private EditTabNavigationBar topBar;
    private EditTabNavigationBar bottomBar;

    public MachineEditScreen(MachineCreationScreen parent, int xSize, int ySize, CustomMachineBuilder builder) {
        super(Component.literal("Machine edit"), xSize, ySize);
        this.builder = builder;
    }

    public CustomMachineBuilder getBuilder() {
        return this.builder;
    }

    public void setChanged() {
        this.changed = true;
    }

    public boolean isChanged() {
        return this.changed;
    }

    public void save() {
        this.changed = false;
        PacketDistributor.sendToServer(new CEditMachinePacket(this.builder.build()));
        Minecraft.getInstance().getTutorial().addTimedToast(new TutorialToast(Icons.MOUSE, Component.translatable("custommachinery.gui.creation.save.toast"), null, false), 50);
        this.checkErrors();
    }

    public void cancel() {
        if(!this.changed)
            Minecraft.getInstance().setScreen(new MachineCreationScreen());
        ConfirmPopup popup = new ConfirmPopup(this, 128, 96, () -> Minecraft.getInstance().setScreen(new MachineCreationScreen()));
        popup.title(Component.translatable("custommachinery.gui.popup.warning").withStyle(ChatFormatting.DARK_RED));
        popup.text(Component.translatable("custommachinery.gui.creation.popup.quit"));
        this.openPopup(popup, "close without editing");
    }

    public void wiki() {
        String[] s = SharedConstants.getCurrentVersion().getName().split("\\.");
        String version = "1.19";
        if(s.length >= 2)
            version = "1." + s[1];
        Util.getPlatform().openUri("https://frinn.gitbook.io/custom-machinery-" + version);
    }

    public void checkErrors() {
        this.removeWidget(this.errors);
        ResultBuilder builder = new ResultBuilder();
        CMVerifier.verifyMachine(builder, this.builder.build());
        Result result = builder.build();
        if(result.errors() == 0) {
            this.errors = this.addRenderableWidget(ImageWidget.texture(16, 16, OK_TEXTURE, 16, 16));
            this.errors.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.warning.ok")));
        } else {
            this.errors = this.addRenderableWidget(ImageWidget.texture(16, 16, ERROR_TEXTURE, 16, 16));
            MutableComponent tooltip = Component.empty();
            tooltip.append(Component.translatable("custommachinery.gui.creation.warning.error" + (result.errors() == 1 ? "" : "s"), result.errors()).withStyle(ChatFormatting.DARK_RED));
            result.getErrors().forEach(error -> tooltip.append("\n - " + error));
            this.errors.setTooltip(Tooltip.create(tooltip));
        }
        this.errors.setPosition(this.x - 55, this.y + 3);
    }

    public TabManager getTabManager() {
        return this.tabManager;
    }

    @Override
    protected void init() {
        super.init();
        this.save = this.addRenderableWidget(new ImageButton(this.x - 28, this.y + 5, 20, 20, SAVE_SPRITES, button -> this.save()));
        this.save.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.save")));
        this.close = this.addRenderableWidget(new ImageButton(this.x - 28, this.y + 30, 20, 20, CLOSE_SPRITES, button -> this.cancel()));
        this.close.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.close")));
        this.wiki = this.addRenderableWidget(new ImageButton(this.x - 28, this.y + 55, 20, 20, WIKI_SPRITES, button -> this.wiki()));
        this.wiki.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.wiki")));
        this.checkErrors();
        this.tabManager = new MachineTabManager(this);
        this.topBar = this.addRenderableWidget(new EditTabNavigationBar(this.xSize, this.tabManager, List.of(new MachineBaseInfoTab(this), new AppearanceTab(this), new ComponentTab(this), new GuiTab(this)), false));
        this.topBar.selectTab(0, false);
        this.bottomBar = this.addRenderableWidget(new EditTabNavigationBar(this.xSize, this.tabManager, List.of(new TooltipsTab(this)), true));
        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        this.save.setPosition(this.x - 28, this.y + 5);
        this.close.setPosition(this.x - 28, this.y + 30);
        this.wiki.setPosition(this.x - 28, this.y + 55);
        this.checkErrors();

        if (this.topBar == null)
            return;

        this.topBar.setRectangle(this.xSize - 10, 20, this.x + 5, this.y - 20);
        this.bottomBar.setRectangle((this.xSize - 10) / 4, 20, this.x + 5, this.y + this.ySize - 3);
        this.tabManager.setTabArea(new ScreenRectangle(this.x, this.y, this.xSize, this.ySize));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(graphics, mouseX, mouseY, partialTicks);
        //Background
        blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);
        //Buttons
        blankBackground(graphics, this.x - 33, this.y, 30, 80);
        //Errors
        blankBackground(graphics, this.x - 58, this.y, 22, 22);
    }

    @Override
    public void onClose() {
        this.cancel();
    }
}
