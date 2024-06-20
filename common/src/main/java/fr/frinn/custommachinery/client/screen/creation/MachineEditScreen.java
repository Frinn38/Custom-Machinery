package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.tabs.AppearanceTab;
import fr.frinn.custommachinery.client.screen.creation.tabs.BaseInfoTab;
import fr.frinn.custommachinery.client.screen.creation.tabs.ComponentTab;
import fr.frinn.custommachinery.client.screen.creation.tabs.GuiTab;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.network.CEditMachinePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.components.toasts.TutorialToast.Icons;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MachineEditScreen extends BaseScreen {

    public static final ResourceLocation WIDGETS = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/edit_widget.png");

    private final MachineCreationScreen parent;
    private final CustomMachineBuilder builder;

    private boolean changed = false;

    private ImageButton save;
    private ImageButton close;
    private ImageButton wiki;
    private TabManager tabManager;
    private MachineEditTabNavigationBar bar;

    public MachineEditScreen(MachineCreationScreen parent, int xSize, int ySize, CustomMachineBuilder builder) {
        super(Component.literal("Machine edit"), xSize, ySize);
        this.parent = parent;
        this.builder = builder;
    }

    public CustomMachineBuilder getBuilder() {
        return this.builder;
    }

    public void setChanged() {
        this.changed = true;
    }

    public void save() {
        this.changed = false;
        new CEditMachinePacket(this.builder.build()).sendToServer();
        Minecraft.getInstance().getTutorial().addTimedToast(new TutorialToast(Icons.MOUSE, Component.translatable("custommachinery.gui.creation.save.toast"), null, false), 50);
    }

    public void cancel() {
        if(!this.changed)
            Minecraft.getInstance().setScreen(new MachineCreationScreen());
        ConfirmPopup popup = new ConfirmPopup(this, 128, 96, () -> Minecraft.getInstance().setScreen(new MachineCreationScreen()));
        popup.title(Component.translatable("custommachinery.gui.popup.warning").withStyle(ChatFormatting.DARK_RED));
        popup.text(Component.translatable("custommachinery.gui.creation.popup.quit"));
        this.openPopup(popup);
    }

    public void wiki() {
        String[] s = SharedConstants.getCurrentVersion().getName().split("\\.");
        String version = "1.19";
        if(s.length >= 2)
            version = "1." + s[1];
        Util.getPlatform().openUri("https://frinn.gitbook.io/custom-machinery-" + version);
    }

    @Override
    protected void init() {
        super.init();
        this.save = this.addRenderableWidget(new ImageButton(this.x - 28, this.y + 5, 20, 20, 0, 0, WIDGETS, button -> this.save()));
        this.save.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.save")));
        this.close = this.addRenderableWidget(new ImageButton(this.x - 28, this.y + 30, 20, 20, 20, 0, WIDGETS, button -> this.cancel()));
        this.close.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.close")));
        this.wiki = this.addRenderableWidget(new ImageButton(this.x - 28, this.y + 55, 20, 20, 40, 0, WIDGETS, button -> this.wiki()));
        this.wiki.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.wiki")));
        this.tabManager = new MachineTabManager(this::addRenderableWidget, this::removeWidget);
        this.bar = this.addRenderableWidget(new MachineEditTabNavigationBar(this.xSize, this.tabManager, List.of(new BaseInfoTab(this), new AppearanceTab(this), new ComponentTab(this), new GuiTab(this))));
        this.bar.selectTab(0, false);
        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        this.save.setPosition(this.x - 28, this.y + 5);
        this.close.setPosition(this.x - 28, this.y + 30);
        this.wiki.setPosition(this.x - 28, this.y + 55);

        if (this.bar == null)
            return;

        this.bar.bounds(this.x + 5, this.y - 20, this.xSize - 10, 20);
        this.bar.arrangeElements();
        this.tabManager.setTabArea(new ScreenRectangle(this.x, this.y, this.xSize, this.ySize));
    }

    @Override
    public  <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    @Override
    public void removeWidget(GuiEventListener listener) {
        super.removeWidget(listener);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
        blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);

        blankBackground(graphics, this.x - 33, this.y, 30, 80);
    }

    @Override
    public void onClose() {
        this.cancel();
    }
}
