package fr.frinn.custommachinery.client.screen.widget.tabs;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class EditTabNavigationBar extends AbstractWidget implements ContainerEventHandler {

    private static final int NO_TAB = -1;
    private static final int MAX_WIDTH = 400;
    private static final int HEIGHT = 24;
    private static final int MARGIN = 14;
    private static final Component USAGE_NARRATION = Component.translatable("narration.tab_navigation.usage");
    private final GridLayout layout;

    private final TabManager tabManager;
    private final ImmutableList<EditTab> tabs;
    private final ImmutableList<TabButton> tabButtons;

    @Nullable
    private GuiEventListener focused;
    private boolean isDragging;

    public EditTabNavigationBar(int width, TabManager tabManager, Iterable<? extends EditTab> tabs, boolean inverted) {
        super(0, 0, width, HEIGHT, Component.empty());
        this.width = width;
        this.tabManager = tabManager;
        this.tabs = ImmutableList.copyOf(tabs);
        this.layout = new GridLayout(0, 0);
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        ImmutableList.Builder<TabButton> builder = ImmutableList.builder();
        int i = 0;

        for (EditTab tab : tabs)
            builder.add(this.layout.addChild(new EditTabButton(tabManager, tab, 0, 24, inverted), 0, i++));

        this.tabButtons = builder.build();
    }

    public void setFocused(@Nullable GuiEventListener focused) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (focused != null) {
            focused.setFocused(true);
        }

        this.focused = focused;
        if (focused instanceof TabButton tabButton)
            this.tabManager.setCurrentTab(tabButton.tab(), true);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(mouseX, mouseY, button))
                this.setFocused(guieventlistener);
        }
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
    public void setDragging(boolean dragging) {
        this.isDragging = dragging;
    }

    @Override
    public boolean isDragging() {
        return this.isDragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.tabButtons.stream().map(AbstractWidget::narrationPriority).max(Comparator.naturalOrder()).orElse(NarrationPriority.NONE);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
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
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (TabButton tabButton : this.tabButtons)
            tabButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.arrangeElements();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.arrangeElements();
    }

    public void arrangeElements() {
        int i = Math.min(MAX_WIDTH, this.width);
        int j = Mth.roundToward(i / this.tabs.size(), 2);

        for (TabButton tabButton : this.tabButtons)
            tabButton.setWidth(j);

        this.layout.arrangeElements();
        this.layout.setX(this.getX());
        this.layout.setY(this.getY());
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
        if(tab instanceof EditTab editTab)
            return this.tabs.indexOf(editTab);
        return -1;
    }

    @Nullable
    private TabButton currentTabButton() {
        int i = this.currentTabIndex();
        return i != -1 ? this.tabButtons.get(i) : null;
    }
}
