package fr.frinn.custommachinery.client.screen.creation.component;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.utils.Filter;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.component.FilterConfigPopup.FilterSelectionList.Mode;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.SuggestedEditBox;
import fr.frinn.custommachinery.common.util.CycleTimer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FilterConfigPopup<T> extends PopupScreen {

    private final Supplier<Filter<T>> supplier;
    private final Consumer<Filter<T>> consumer;
    private final FilterBuilderHelper<T> helper;

    private FilterSelectionList<T> whitelist;
    private FilterSelectionList<T> blacklist;

    public FilterConfigPopup(BaseScreen parent, Supplier<Filter<T>> supplier, Consumer<Filter<T>> consumer, FilterBuilderHelper<T> helper) {
        super(parent, 256, 196);
        this.supplier = supplier;
        this.consumer = consumer;
        this.helper = helper;
    }

    private void confirm() {
        this.consumer.accept(new Filter<>(this.whitelist.getList(), this.blacklist.getList()));
        this.parent.closePopup(this);
    }

    private void cancel() {
        this.parent.closePopup(this);
    }

    @Override
    protected void init() {
        super.init();

        GridLayout layout = new GridLayout(this.x + 5, this.y + 5);
        RowHelper row = layout.createRowHelper(2);
        LayoutSettings center = layout.newCellSettings().alignHorizontallyCenter();

        //Title
        row.addChild(new StringWidget(this.xSize - 10, 9, Component.translatable("custommachinery.gui.creation.components.filter"), this.font), 2, center);

        //Whitelist
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.components.filter.whitelist"), this.font), 2);
        this.whitelist = row.addChild(new FilterSelectionList<>(0, 0, this.xSize - 10, 60, this.helper, this.parent, Mode.MODIFY), 2);
        this.whitelist.setList(this.supplier.get().whitelist());

        //Blacklist
        row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.components.filter.blacklist"), this.font), 2);
        this.blacklist = row.addChild(new FilterSelectionList<>(0, 0, this.xSize - 10, 60, this.helper, this.parent, Mode.MODIFY), 2);
        this.blacklist.setList(this.supplier.get().blacklist());

        //Confirm
        row.addChild(Button.builder(ComponentBuilderPopup.CONFIRM, button -> this.confirm()).size(50, 20).build(), center);

        //Cancel
        row.addChild(Button.builder(ComponentBuilderPopup.CANCEL, button -> this.cancel()).size(50, 20).build(), center);

        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
    }

    public static class FilterSelectionList<T> extends AbstractWidget {

        private final List<FilterListEntry> list = new ArrayList<>();
        private final int maxColumns;
        private final FilterBuilderHelper<T> helper;
        private final BaseScreen parent;
        private final Mode mode;

        private double scrollAmount;
        private boolean scrolling = false;
        private FilterListEntry selected;

        public FilterSelectionList(int x, int y, int width, int height, FilterBuilderHelper<T> helper, BaseScreen parent, Mode mode) {
            super(x, y, width, height, Component.empty());
            this.maxColumns = width / 20;
            this.helper = helper;
            this.parent = parent;
            this.mode = mode;
        }

        public void setList(List<Either<TagKey<T>, Holder<T>>> list) {
            this.list.clear();
            list.forEach(either -> either.map(
                    tag -> this.list.add(new TagEntry(tag)),
                    holder -> this.list.add(new SimpleEntry(holder))));
            if(this.mode == Mode.MODIFY)
                this.list.add(new AddEntry());
        }

        public List<Either<TagKey<T>, Holder<T>>> getList() {
            List<Either<TagKey<T>, Holder<T>>> list = new ArrayList<>();
            this.list.forEach(entry -> {
                if(entry instanceof SimpleEntry simple)
                    list.add(Either.right(simple.holder));
                else if(entry instanceof TagEntry tag)
                    list.add(Either.left(tag.tag));
            });
            return list;
        }

        @Nullable
        public FilterListEntry getSelected() {
            return this.selected;
        }

        @Nullable
        public FilterListEntry getElementUnderMouse(double mouseX, double mouseY) {
            if(mouseX < this.getX() || mouseX > this.getX() + this.maxColumns * 20 || mouseY < this.getY() || mouseY > this.getY() + this.getHeight())
                return null;
            int index = (int)((mouseY - this.getY() + this.scrollAmount) / 20) * this.maxColumns + (int)((mouseX - this.getX()) / 20);
            if(index < 0 || index >= this.list.size())
                return null;
            return this.list.get(index);
        }

        private void scroll(int scroll) {
            this.setScrollAmount(this.getScrollAmount() + (double)scroll);
        }

        public double getScrollAmount() {
            return this.scrollAmount;
        }

        public void setScrollAmount(double scroll) {
            this.scrollAmount = Mth.clamp(scroll, 0.0, this.getMaxScroll());
        }

        public int getMaxScroll() {
            return Math.max(0, this.getMaxPosition() - this.getHeight() - 4);
        }

        protected int getMaxPosition() {
            return this.list.size() / this.maxColumns * 20;
        }

        public int getScrollBottom() {
            return (int)this.getScrollAmount() - this.getHeight();
        }

        protected void updateScrollingState(double mouseX, double mouseY, int button) {
            this.scrolling = button == 0 && mouseX >= this.getScrollbarPosition() && mouseX < this.getScrollbarPosition() + 6;
        }

        protected int getScrollbarPosition() {
            return this.width / 2 + 124;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.enableScissor(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight());
            graphics.pose().pushPose();
            graphics.pose().translate(this.getX(), this.getY(), 0);
            for(int i = 0; i < this.list.size(); i++) {
                int x = (i % this.maxColumns) * 20;
                int y = i / this.maxColumns * 20 - (int)this.getScrollAmount();
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);

                FilterListEntry entry = this.list.get(i);

                if(this.mode == Mode.SELECT && this.selected == entry)
                    graphics.fill(0, 0, 20, 20, FastColor.ARGB32.color(255, 255, 0, 0));

                graphics.pose().translate(0, 0, 100);

                entry.render(graphics, mouseX, mouseY, partialTick);

                graphics.pose().popPose();
            }
            graphics.pose().popPose();

            if(this.getMaxScroll() > 0) {
                int i = this.getX() + this.getWidth() - 10;
                int j = i + 6;
                int n = (this.getHeight() * this.getHeight()) / this.getMaxPosition();
                n = Mth.clamp(n, 32, this.getHeight() - 8);
                int o = (int)this.getScrollAmount() * (this.getHeight() - n) / this.getMaxScroll() + this.getY();
                if (o < this.getY()) {
                    o = this.getY();
                }
                graphics.fill(i, this.getY(), j, this.getY() + this.getHeight(), -16777216);
                graphics.fill(i, o, j, o + n, -8355712);
                graphics.fill(i, o, j - 1, o + n - 1, -4144960);
            }

            graphics.disableScissor();

            FilterListEntry hovered = this.getElementUnderMouse(mouseX, mouseY);
            if(hovered != null) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(hovered.getTooltip());
                if(!(hovered instanceof AddEntry) && this.mode == Mode.MODIFY)
                    tooltip.add(Component.translatable("custommachinery.gui.creation.components.filter.remove").withStyle(ChatFormatting.RED));
                graphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.updateScrollingState(mouseX, mouseY, button);
            FilterListEntry selected = this.getElementUnderMouse(mouseX, mouseY);
            if(selected != null) {
                this.selected = selected;
                if(selected instanceof AddEntry)
                    this.parent.openPopup(new FilterValueAddPopup<>(this.parent, this.helper, entry -> this.list.add(this.list.size() - 1, entry)), "Add entry");
                else if(this.mode == Mode.MODIFY)
                    this.list.remove(selected);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if(super.mouseDragged(mouseX, mouseY, button, dragX, dragY))
                return true;
            if (button != 0 || !this.scrolling)
                return false;
            if (mouseY < this.getY()) {
                this.setScrollAmount(0.0);
            } else if (mouseY > this.getY() + this.getHeight()) {
                this.setScrollAmount(this.getMaxScroll());
            } else {
                double d = Math.max(1, this.getMaxScroll());
                int i = this.getHeight();
                int j = Mth.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
                double e = Math.max(1.0, d / (double)(i - j));
                this.setScrollAmount(this.getScrollAmount() + dragY * e);
            }
            return true;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            this.setScrollAmount(this.getScrollAmount() - scrollY * 10);
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        public enum Mode {
            SELECT,MODIFY
        }

        public abstract class FilterListEntry {
            public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);
            public abstract Component getTooltip();
        }

        private class SimpleEntry extends FilterListEntry {
            private final Holder<T> holder;

            public SimpleEntry(Holder<T> holder) {
                this.holder = holder;
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                graphics.pose().pushPose();
                graphics.pose().translate(2, 2, 0);
                FilterSelectionList.this.helper.renderSingle(this.holder.value(), graphics, mouseX, mouseY, partialTicks);
                graphics.pose().popPose();
            }

            @Override
            public Component getTooltip() {
                return FilterSelectionList.this.helper.tooltip(this.holder.value());
            }
        }

        private class TagEntry extends FilterListEntry {
            private final TagKey<T> tag;
            private final CycleTimer timer = new CycleTimer(() -> 1000);

            public TagEntry(TagKey<T> tag) {
                this.tag = tag;
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                this.timer.onDraw();
                T single = this.timer.getOrDefault(FilterSelectionList.this.helper.registry().getTag(this.tag).map(named -> named.stream().map(Holder::value).toList()).orElse(Collections.emptyList()), FilterSelectionList.this.helper.defaultValue());
                graphics.pose().pushPose();
                graphics.pose().translate(2, 2, 0);
                FilterSelectionList.this.helper.renderSingle(single, graphics, mouseX, mouseY, partialTicks);
                graphics.pose().popPose();
            }

            @Override
            public Component getTooltip() {
                return Component.literal("#" + this.tag.location());
            }
        }

        private class AddEntry extends FilterListEntry {

            public static final ResourceLocation ADD_TEXTURE = CustomMachinery.rl("textures/gui/create_icon.png");

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                graphics.blit(ADD_TEXTURE, 0, 0, 0, 0, 20, 20, 20, 20);
            }

            @Override
            public Component getTooltip() {
                return Component.translatable("custommachinery.gui.creation.components.filter.add");
            }
        }
    }

    public interface FilterBuilderHelper<T> {

        void renderSingle(T single, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

        Component tooltip(T single);

        Registry<T> registry();

        default Stream<ResourceLocation> getAll() {
            return registry().keySet().stream();
        }

        T defaultValue();
    }

    public static class FilterValueAddPopup<T> extends PopupScreen {

        private final FilterBuilderHelper<T> helper;
        private final Consumer<FilterSelectionList<T>.FilterListEntry> consumer;

        private Checkbox single;
        private Checkbox tag;
        private SuggestedEditBox box;
        private FilterSelectionList<T> list;

        public FilterValueAddPopup(BaseScreen parent, FilterBuilderHelper<T> helper, Consumer<FilterSelectionList<T>.FilterListEntry> consumer) {
            super(parent, 128, 128);
            this.helper = helper;
            this.consumer = consumer;
        }

        private void confirm() {
            if(this.list.getSelected() != null)
                this.consumer.accept(this.list.getSelected());
            this.parent.closePopup(this);
        }

        private void cancel() {
            this.parent.closePopup(this);
        }

        @Override
        protected void init() {
            super.init();

            GridLayout layout = new GridLayout(this.x + 5, this.y + 5);
            RowHelper row = layout.createRowHelper(2);
            row.defaultCellSetting().paddingBottom(5);
            LayoutSettings center = layout.newCellSettings().alignHorizontallyCenter();

            //Title
            row.addChild(new StringWidget(this.xSize - 10, 9, Component.translatable("custommachinery.gui.creation.components.filter.add"), this.font), 2, center);

            //Checkbox
            this.single = row.addChild(Checkbox.builder(Component.translatable("custommachinery.gui.creation.components.filter.single"), this.font).onValueChange((box, value) -> this.refreshBoxSuggestions()).selected(true).build());
            this.tag = row.addChild(Checkbox.builder(Component.translatable("custommachinery.gui.creation.components.filter.tags"), this.font).onValueChange((box, value) -> this.refreshBoxSuggestions()).selected(true).build());

            //Search box
            this.box = row.addChild(new SuggestedEditBox(this.font, 0, 0, this.xSize - 10, 20, Component.empty(), 5), 2);
            this.box.setAnchorToBottom();
            this.box.setMaxLength(Integer.MAX_VALUE);
            this.box.moveCursorToStart(false);

            //List
            this.list = row.addChild(new FilterSelectionList<>(0, 0, this.xSize - 10, 60, this.helper, this.parent, Mode.SELECT), 2, center);
            this.box.setResponder(s -> this.refreshList());
            this.refreshBoxSuggestions();

            //Confirm
            row.addChild(Button.builder(ComponentBuilderPopup.CONFIRM, button -> this.confirm()).size(50, 20).build(), center);

            //Cancel
            row.addChild(Button.builder(ComponentBuilderPopup.CANCEL, button -> this.cancel()).size(50, 20).build(), center);

            layout.arrangeElements();
            layout.visitWidgets(this::addRenderableWidget);
            this.ySize = layout.getHeight() + 10;
        }

        private void refreshBoxSuggestions() {
            this.box.clearSuggestions();
            if(this.single.selected())
                this.box.addSuggestions(this.helper.getAll().map(ResourceLocation::toString).toList());
            if(this.tag.selected())
                this.box.addSuggestions(this.helper.registry().getTagNames().map(key -> "#" + key.location()).toList());
            this.refreshList();
        }

        public void refreshList() {
            List<String> suggestions = this.box.getPossibleSuggestions();
            String input = this.box.getValue();
            List<Either<TagKey<T>, Holder<T>>> list = suggestions.stream().sorted(Comparator.comparingInt(s -> {
                if(s.equals(input))
                    return -1000;
                else if(s.startsWith(input))
                    return -100;
                else if(s.contains(input))
                    return -10;
                int matchingChars = 0;
                for(char c : input.toCharArray())
                    if(s.contains("" + c))
                        matchingChars++;
                return -matchingChars;
            })).limit(100).map(s -> {
                if(s.startsWith("#"))
                    return Either.<TagKey<T>, Holder<T>>left(TagKey.create(this.helper.registry().key(), ResourceLocation.parse(s.substring(1))));
                else
                    return Either.<TagKey<T>, Holder<T>>right(this.helper.registry().getHolder(ResourceLocation.parse(s)).orElseThrow());
            }).toList();
            this.list.setList(list);
        }
    }
}


