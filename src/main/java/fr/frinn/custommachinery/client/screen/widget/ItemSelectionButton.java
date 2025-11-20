package fr.frinn.custommachinery.client.screen.widget;

import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class ItemSelectionButton extends Button {

    private final BaseScreen parent;
    @Nullable
    private Consumer<Item> responder = null;
    private Item item = Items.AIR;

    public ItemSelectionButton(BaseScreen parent, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), button -> {}, DEFAULT_NARRATION);
        this.parent = parent;
    }

    public void setItem(Item item) {
        this.item = item;
        if(this.responder != null)
            this.responder.accept(this.item);
    }

    public Item getItem() {
        return this.item;
    }

    public void setResponder(@Nullable Consumer<Item> responder) {
        this.responder = responder;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.pose().pushPose();
        graphics.pose().translate(this.getX(), this.getY(), 0);
        graphics.blit(SlotGuiElement.BASE_TEXTURE.texture(), 0, 0, this.width, this.height, 0, 0, 18, 18, 18, 18);

        graphics.pose().pushPose();
        graphics.pose().scale(this.width / 18.0f, this.height / 18.0f, 1.0f);
        graphics.renderItem(this.item.getDefaultInstance(), 1, 1);
        graphics.pose().popPose();

        graphics.pose().translate(0, 0, 100);
        if(this.isMouseOver(mouseX, mouseY))
            ClientHandler.renderSlotHighlight(graphics, 1, 1, this.width - 2, this.height - 2);
        graphics.pose().popPose();

        //Tooltip
        if(this.isMouseOver(mouseX, mouseY))
            graphics.renderTooltip(Minecraft.getInstance().font, this.item.getDefaultInstance(), mouseX, mouseY);
    }

    @Override
    public void onPress() {
        this.parent.openPopup(new ItemSelectionPopup(this.parent, this::setItem), "choose item");
    }

    private static class ItemSelectionPopup extends PopupScreen {

        private final Consumer<Item> consumer;

        private SuggestedEditBox box;
        private ItemSelectionList list;

        public ItemSelectionPopup(BaseScreen parent, Consumer<Item> consumer) {
            super(parent, 128, 128);
            this.consumer = consumer;
        }

        private void confirm() {
            if(this.list.getSelected() != null)
                this.consumer.accept(this.list.getSelected().item);
            this.parent.closePopup(this);
        }

        private void cancel() {
            this.parent.closePopup(this);
        }

        @Override
        protected void init() {
            super.init();

            GridLayout layout = new GridLayout(this.x + 5, this.y + 5);
            GridLayout.RowHelper row = layout.createRowHelper(2);
            row.defaultCellSetting().paddingBottom(5);
            LayoutSettings center = layout.newCellSettings().alignHorizontallyCenter();

            //Title
            row.addChild(new StringWidget(this.xSize - 10, 9, Component.translatable("custommachinery.gui.creation.upgrade.popup.item"), this.font), 2, center);

            //Search box
            this.box = row.addChild(new SuggestedEditBox(this.font, 0, 0, this.xSize - 10, 20, Component.empty(), 5), 2);
            this.box.setAnchorToBottom();
            this.box.setMaxLength(Integer.MAX_VALUE);
            this.box.moveCursorToStart(false);

            //List
            this.list = row.addChild(new ItemSelectionList(0, 0, this.xSize - 10, 60), 2, center);
            this.list.setRenderSelection();
            this.box.setResponder(s -> this.refreshList());
            this.refreshBoxSuggestions();

            //Confirm
            row.addChild(Button.builder(CONFIRM, button -> this.confirm()).size(50, 20).build(), center);

            //Cancel
            row.addChild(Button.builder(CANCEL, button -> this.cancel()).size(50, 20).build(), center);

            layout.arrangeElements();
            layout.visitWidgets(this::addRenderableWidget);
            this.ySize = layout.getHeight() + 10;
        }

        private void refreshBoxSuggestions() {
            this.box.clearSuggestions();
            this.box.addSuggestions(BuiltInRegistries.ITEM.keySet().stream().map(ResourceLocation::toString).toList());
            this.refreshList();
        }

        public void refreshList() {
            List<String> suggestions = this.box.getPossibleSuggestions();
            String input = this.box.getValue();
            List<Item> list = suggestions.stream().sorted(Comparator.comparingInt(s -> {
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
            })).limit(100).map(s -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(s))).toList();
            this.list.setList(list);
        }
    }

    private static class ItemSelectionList extends GridListWidget<ItemSelectionList.ItemEntry> {

        public ItemSelectionList(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        public void setList(List<Item> items) {
            this.clear();
            items.forEach(item -> this.addEntry(new ItemEntry(item)));
        }

        private static class ItemEntry extends Entry {

            private final Item item;

            private ItemEntry(Item item) {
                this.item = item;
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                graphics.renderItem(this.item.getDefaultInstance(), 2, 2);
            }

            @Override
            public List<Component> getTooltips() {
                return this.item.getDefaultInstance().getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
        }
    }
}
