package fr.frinn.custommachinery.client.screen.creation.upgrade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineTabManager;
import fr.frinn.custommachinery.client.screen.creation.upgrade.tabs.UpgradeBaseInfoTab;
import fr.frinn.custommachinery.client.screen.popup.ConfirmPopup;
import fr.frinn.custommachinery.client.screen.widget.tabs.EditTabNavigationBar;
import fr.frinn.custommachinery.common.network.CEditUpgradePacket;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.MachineUpgradeBuilder;
import fr.frinn.custommachinery.common.upgrade.UpgradeLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.components.toasts.TutorialToast.Icons;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class UpgradeEditScreen extends BaseScreen {

    public static final WidgetSprites SAVE_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/save_button"), CustomMachinery.rl("creation/save_button_hovered"));
    public static final WidgetSprites CLOSE_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/close_button"), CustomMachinery.rl("creation/close_button_hovered"));
    public static final WidgetSprites WIKI_SPRITES = new WidgetSprites(CustomMachinery.rl("creation/wiki_button"), CustomMachinery.rl("creation/wiki_button_hovered"));

    private final UpgradeCreationScreen parent;
    private final UpgradeLocation location;
    private final MachineUpgradeBuilder builder;

    private boolean changed = false;

    private ImageButton save;
    private ImageButton close;
    private ImageButton wiki;
    private TabManager tabManager;
    private EditTabNavigationBar topBar;
    //private EditTabNavigationBar bottomBar;

    public UpgradeEditScreen(UpgradeCreationScreen parent, int xSize, int ySize, UpgradeLocation location, MachineUpgrade upgrade) {
        super(Component.literal("Upgrade edit"), xSize, ySize);
        this.parent = parent;
        this.location = location;
        this.builder = new MachineUpgradeBuilder(upgrade);
    }

    public UpgradeLocation getLocation() {
        return this.location;
    }

    public MachineUpgradeBuilder getBuilder() {
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
        PacketDistributor.sendToServer(new CEditUpgradePacket(this.location, this.builder.build()));
        Minecraft.getInstance().getTutorial().addTimedToast(new TutorialToast(Icons.MOUSE, Component.translatable("custommachinery.gui.creation.upgrade.save.toast"), null, false), 50);
    }

    public void cancel() {
        if(!this.changed)
            Minecraft.getInstance().setScreen(new UpgradeCreationScreen());
        ConfirmPopup popup = new ConfirmPopup(this, 128, 96, () -> Minecraft.getInstance().setScreen(new UpgradeCreationScreen()));
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
        this.tabManager = new MachineTabManager(this);
        this.topBar = this.addRenderableWidget(new EditTabNavigationBar(this.xSize, this.tabManager, List.of(new UpgradeBaseInfoTab(this)), false));
        this.topBar.selectTab(0, false);
        //this.bottomBar = this.addRenderableWidget(new MachineEditTabNavigationBar(this.xSize, this.tabManager, List.of(), true));
        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        this.save.setPosition(this.x - 28, this.y + 5);
        this.close.setPosition(this.x - 28, this.y + 30);
        this.wiki.setPosition(this.x - 28, this.y + 55);

        if (this.topBar == null)
            return;

        this.topBar.setRectangle(this.xSize - 10, 20, this.x + 5, this.y - 20);
        //this.bottomBar.setRectangle((this.xSize - 10) / 4, 20, this.x + 5, this.y + this.ySize - 3);
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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(graphics, mouseX, mouseY, partialTicks);
        blankBackground(graphics, this.x, this.y, this.xSize, this.ySize);

        blankBackground(graphics, this.x - 33, this.y, 30, 80);
    }

    @Override
    public void onClose() {
        this.cancel();
    }
}
