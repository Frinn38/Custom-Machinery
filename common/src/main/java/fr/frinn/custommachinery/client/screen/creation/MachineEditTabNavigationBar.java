package fr.frinn.custommachinery.client.screen.creation;

import com.google.common.collect.ImmutableList;
import fr.frinn.custommachinery.client.screen.creation.tabs.EditTabButton;
import fr.frinn.custommachinery.client.screen.creation.tabs.MachineEditTab;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MachineEditTabNavigationBar extends AbstractContainerEventHandler implements Renderable, GuiEventListener, NarratableEntry {

    private static final int NO_TAB = -1;
    private static final int MAX_WIDTH = 400;
    private static final int HEIGHT = 24;
    private static final int MARGIN = 14;
    private static final Component USAGE_NARRATION = Component.translatable("narration.tab_navigation.usage");
    private final GridLayout layout;

    private int x;
    private int y;
    private int width;
    private int height;
    private final TabManager tabManager;
    private final ImmutableList<MachineEditTab> tabs;
    private final ImmutableList<TabButton> tabButtons;

    public MachineEditTabNavigationBar(int width, TabManager tabManager, Iterable<MachineEditTab> tabs) {
        this.width = width;
        this.tabManager = tabManager;
        this.tabs = ImmutableList.copyOf(tabs);
        this.layout = new GridLayout(0, 0);
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        ImmutableList.Builder<TabButton> builder = ImmutableList.builder();
        int i = 0;

        for (MachineEditTab tab : tabs)
            builder.add(this.layout.addChild(new EditTabButton(tabManager, tab, 0, 24), 0, i++));

        this.tabButtons = builder.build();
    }

    public void setFocused(@Nullable GuiEventListener focused) {
        super.setFocused(focused);
        if (focused instanceof TabButton tabButton)
            this.tabManager.setCurrentTab(tabButton.tab(), true);
    }

    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent event) {
        if (!this.isFocused()) {
            TabButton tabButton = this.currentTabButton();
            if (tabButton != null) {
                return ComponentPath.path(this, ComponentPath.leaf(tabButton));
            }
        }

        return event instanceof FocusNavigationEvent.TabNavigation ? null : super.nextFocusPath(event);
    }

    @Override
    public List<TabButton> children() {
        return this.tabButtons;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.tabButtons.stream().map(AbstractWidget::narrationPriority).max(Comparator.naturalOrder()).orElse(NarrationPriority.NONE);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        Optional<TabButton> optional = this.tabButtons.stream().filter(AbstractWidget::isHovered).findFirst().or(() -> Optional.ofNullable(this.currentTabButton()));
        optional.ifPresent((tabButton) -> {
            this.narrateListElementPosition(narrationElementOutput.nest(), tabButton);
            tabButton.updateNarration(narrationElementOutput);
        });
        if (this.isFocused())
            narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
    }

    protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, TabButton tabButton) {
        if (this.tabs.size() > 1) {
            int i = this.tabButtons.indexOf(tabButton);
            if (i != -1) {
                narrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.tab", i + 1, this.tabs.size()));
            }
        }

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (TabButton tabButton : this.tabButtons)
            tabButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void bounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public ScreenRectangle getRectangle() {
        return this.layout.getRectangle();
    }

    public void arrangeElements() {
        int i = Math.min(400, this.width);
        int j = Mth.roundToward(i / this.tabs.size(), 2);

        for (TabButton tabButton : this.tabButtons)
            tabButton.setWidth(j);

        this.layout.arrangeElements();
        this.layout.setX(this.x);
        this.layout.setY(this.y);
    }

    public void selectTab(int index, boolean playClickSound) {
        if (this.isFocused())
            this.setFocused(this.tabButtons.get(index));
        else
            this.tabManager.setCurrentTab(this.tabs.get(index), playClickSound);
    }

    private int getNextTabIndex(int keycode) {
        if (keycode >= 49 && keycode <= 57) {
            return keycode - 49;
        } else {
            if (keycode == 258) {
                int i = this.currentTabIndex();
                if (i != -1) {
                    int j = Screen.hasShiftDown() ? i - 1 : i + 1;
                    return Math.floorMod(j, this.tabs.size());
                }
            }

            return -1;
        }
    }

    private int currentTabIndex() {
        Tab tab = this.tabManager.getCurrentTab();
        int i = this.tabs.indexOf(tab);
        return i != -1 ? i : -1;
    }

    @Nullable
    private TabButton currentTabButton() {
        int i = this.currentTabIndex();
        return i != -1 ? this.tabButtons.get(i) : null;
    }
}
