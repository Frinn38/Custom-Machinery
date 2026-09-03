package fr.frinn.custommachinery.client.screen.creation.component;

import com.mojang.datafixers.util.Either;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.component.FilterConfigPopup.FilterSelectionList.Mode;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.GridListWidget;
import fr.frinn.custommachinery.client.screen.widget.SuggestedEditBox;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.common.util.Filter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

    public static class FilterSelectionList<T> extends GridListWidget<FilterSelectionList.FilterListEntry<T>> {

        private final FilterBuilderHelper<T> helper;
        private final BaseScreen parent;
        private final Mode mode;

        public FilterSelectionList(int x, int y, int width, int height, FilterBuilderHelper<T> helper, BaseScreen parent, Mode mode) {
            super(x, y, width, height);
            this.helper = helper;
            this.parent = parent;
            this.mode = mode;
        }

        public void setList(List<Either<TagKey<T>, Holder<T>>> list) {
            this.clear();
            list.forEach(either -> {
                either.ifLeft(tag -> this.addEntry(new TagEntry<>(tag, this)));
                either.ifRight(holder -> this.addEntry(new SimpleEntry<>(holder, this)));
            });
            if(this.mode == Mode.MODIFY)
                this.addEntry(new AddEntry<>(this));
        }

        @SuppressWarnings({"unchecked","rawtypes"})
        public List<Either<TagKey<T>, Holder<T>>> getList() {
            List<Either<TagKey<T>, Holder<T>>> list = new ArrayList<>();
            this.getAll().forEach(entry -> {
                if(entry instanceof SimpleEntry simple)
                    list.add(Either.right(simple.holder));
                else if(entry instanceof TagEntry tag)
                    list.add(Either.left(tag.tag));
            });
            return list;
        }

        public enum Mode {
            SELECT,MODIFY
        }

        public abstract static class FilterListEntry<T> extends Entry{

            protected final FilterSelectionList<T> list;

            private FilterListEntry(FilterSelectionList<T> list) {
                this.list = list;
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                if(this.list.mode == Mode.SELECT && this.list.getSelected() == this)
                    graphics.fill(0, 0, 20, 20, FastColor.ARGB32.color(255, 255, 0, 0));
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
        }

        private static class SimpleEntry<T> extends FilterListEntry<T> {
            private final Holder<T> holder;

            public SimpleEntry(Holder<T> holder, FilterSelectionList<T> list) {
                super(list);
                this.holder = holder;
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.render(graphics, mouseX, mouseY, partialTicks);
                graphics.pose().pushPose();
                graphics.pose().translate(2, 2, 0);
                this.list.helper.renderSingle(this.holder.value(), graphics, mouseX, mouseY, partialTicks);
                graphics.pose().popPose();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if(this.list.getSelected() == this && this.list.mode == Mode.MODIFY) {
                    this.list.remove(this);
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public List<Component> getTooltips() {
                List<Component> tooltips = new ArrayList<>();
                tooltips.add(this.list.helper.tooltip(this.holder.value()));
                if(this.list.mode == Mode.MODIFY)
                    tooltips.add(Component.translatable("custommachinery.gui.creation.components.filter.remove").withStyle(ChatFormatting.RED));
                return tooltips;
            }
        }

        private static class TagEntry<T> extends FilterListEntry<T> {
            private final TagKey<T> tag;
            private final CycleTimer timer = new CycleTimer(() -> 1000);

            public TagEntry(TagKey<T> tag, FilterSelectionList<T> list) {
                super(list);
                this.tag = tag;
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.render(graphics, mouseX, mouseY, partialTicks);
                this.timer.onDraw();
                T single = this.timer.getOrDefault(this.list.helper.registry().getTag(this.tag).map(named -> named.stream().map(Holder::value).toList()).orElse(Collections.emptyList()), this.list.helper.defaultValue());
                graphics.pose().pushPose();
                graphics.pose().translate(2, 2, 0);
                this.list.helper.renderSingle(single, graphics, mouseX, mouseY, partialTicks);
                graphics.pose().popPose();
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if(this.list.getSelected() == this && this.list.mode == Mode.MODIFY) {
                    this.list.remove(this);
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public List<Component> getTooltips() {
                List<Component> tooltips = new ArrayList<>();
                tooltips.add(Component.literal("#" + this.tag.location()));
                if(this.list.mode == Mode.MODIFY)
                    tooltips.add(Component.translatable("custommachinery.gui.creation.components.filter.remove").withStyle(ChatFormatting.RED));
                return tooltips;
            }
        }

        private static class AddEntry<T> extends FilterListEntry<T> {

            public static final ResourceLocation ADD_TEXTURE = CustomMachinery.rl("textures/gui/create_icon.png");

            private AddEntry(FilterSelectionList<T> list) {
                super(list);
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                graphics.blit(ADD_TEXTURE, 0, 0, 0, 0, 20, 20, 20, 20);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if(this.list.getSelected() == this) {
                    this.list.parent.openPopup(new FilterValueAddPopup<>(this.list.parent, this.list.helper, entry -> {
                        FilterListEntry<T> newEntry = switch (entry) {
                            case SimpleEntry<T> simpleEntry -> new SimpleEntry<>(simpleEntry.holder, this.list);
                            case TagEntry<T> tagEntry -> new TagEntry<>(tagEntry.tag, this.list);
                            default -> throw new IllegalArgumentException("Can't add " + entry + " to list, not an item or tag entry");
                        };
                        this.list.addEntry(this.list.getAll().size() - 1, newEntry);
                    }), "Add entry");
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public List<Component> getTooltips() {
                return Collections.singletonList(Component.translatable("custommachinery.gui.creation.components.filter.add"));
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
        private final Consumer<FilterSelectionList.FilterListEntry<T>> consumer;

        private Checkbox single;
        private Checkbox tag;
        private SuggestedEditBox box;
        private FilterSelectionList<T> list;

        public FilterValueAddPopup(BaseScreen parent, FilterBuilderHelper<T> helper, Consumer<FilterSelectionList.FilterListEntry<T>> consumer) {
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


