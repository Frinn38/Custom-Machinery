package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.widget.GroupWidget;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.CycleTimer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

public class ToolTypeAppearancePropertyBuilder implements IAppearancePropertyBuilder<List<TagKey<Block>>> {
    @Override
    public Component title() {
        return Component.translatable("custommachinery.gui.creation.appearance.tool_type");
    }

    @Override
    public MachineAppearanceProperty<List<TagKey<Block>>> type() {
        return Registration.TOOL_TYPE_PROPERTY.get();
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<List<TagKey<Block>>> supplier, Consumer<List<TagKey<Block>>> consumer) {
        return new ToolTypeAppearancePropertyBuilderWidget(x, y, width, height, this.title(), supplier, consumer);
    }

    private static class ToolTypeAppearancePropertyBuilderWidget extends GroupWidget {

        public ToolTypeAppearancePropertyBuilderWidget(int x, int y, int width, int height, Component message, Supplier<List<TagKey<Block>>> supplier, Consumer<List<TagKey<Block>>> consumer) {
            super(x, y, width, height, message);
            Map<TagKey<Block>, TagKey<Item>> map = Map.of(BlockTags.MINEABLE_WITH_AXE, ItemTags.AXES, BlockTags.MINEABLE_WITH_HOE, ItemTags.HOES, BlockTags.MINEABLE_WITH_PICKAXE, ItemTags.PICKAXES, BlockTags.MINEABLE_WITH_SHOVEL, ItemTags.SHOVELS);
            AtomicInteger index = new AtomicInteger();
            List<TagKey<Block>> blocks = new ArrayList<>(supplier.get());
            map.forEach((block, item) -> {
                ItemTagWidget widget = new ItemTagWidget(x + index.getAndIncrement() * 20, y, 18, 18, Component.literal(block.location().toString()), button -> {
                    if(((ItemTagWidget)button).selected)
                        blocks.add(block);
                    else
                        blocks.remove(block);
                    consumer.accept(blocks);
                }, item);
                if(supplier.get().contains(block))
                    widget.setSelected(true);
                widget.setTooltip(Tooltip.create(Component.literal(block.location().toString()).withStyle(ChatFormatting.GRAY)));
                this.addWidget(widget);
            });
        }
    }

    private static class ItemTagWidget extends Button {

        private final TagKey<Item> items;
        private final CycleTimer timer = new CycleTimer(() -> 2000);

        private boolean selected;

        public ItemTagWidget(int pX, int pY, int pWidth, int pHeight, Component message, OnPress onPress, TagKey<Item> items) {
            super(pX, pY, pWidth, pHeight, message, onPress, Button.DEFAULT_NARRATION);
            this.items = items;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if(this.selected) {
                graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), FastColor.ARGB32.color(255, 0, 0, 0));
                graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, FastColor.ARGB32.color(255, 198, 198, 198));
            }
            List<Item> list = StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(this.items).spliterator(), false).map(Holder::value).toList();
            this.timer.onDraw();
            graphics.renderFakeItem(this.timer.getOrDefault(list, Items.AIR).getDefaultInstance(), this.getX() + 1, this.getY() + 1);
        }

        @Override
        public void onPress() {
            this.selected = !this.selected;
            super.onPress();
        }
    }
}
