package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.widget.GroupWidget;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MiningLevelAppearancePropertyBuilder implements IAppearancePropertyBuilder<TagKey<Block>> {
    @Override
    public Component title() {
        return Component.translatable("custommachinery.gui.creation.appearance.mining_level");
    }

    @Override
    public MachineAppearanceProperty<TagKey<Block>> type() {
        return Registration.MINING_LEVEL_PROPERTY.get();
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<TagKey<Block>> supplier, Consumer<TagKey<Block>> consumer) {
        return new MiningLevelAppearancePropertyBuilderWidget(x, y, width, height, this.title(), supplier, consumer);
    }

    private static class MiningLevelAppearancePropertyBuilderWidget extends GroupWidget {

        public MiningLevelAppearancePropertyBuilderWidget(int x, int y, int width, int height, Component message, Supplier<TagKey<Block>> supplier, Consumer<TagKey<Block>> consumer) {
            super(x, y, width, height, message);
            Map<TagKey<Block>, Item> map = new LinkedHashMap<>();
            map.put(BlockTags.NEEDS_STONE_TOOL, Items.STONE);
            map.put(BlockTags.NEEDS_IRON_TOOL, Items.IRON_INGOT);
            map.put(BlockTags.NEEDS_DIAMOND_TOOL, Items.DIAMOND);
            AtomicInteger index = new AtomicInteger();
            map.forEach((block, item) -> {
                ItemWidget widget = new ItemWidget(x + index.getAndIncrement() * 20, y, 18, 18, Component.literal(block.location().toString()), button -> {
                    this.children.forEach(children -> {
                        if(children != button && children instanceof ItemWidget itemWidget)
                            itemWidget.selected = false;
                    });
                    consumer.accept(block);
                }, item);
                if(supplier.get().equals(block))
                    widget.setSelected(true);
                widget.setTooltip(Tooltip.create(Component.literal(block.location().toString()).withStyle(ChatFormatting.GRAY)));
                this.addWidget(widget);
            });
        }
    }

    private static class ItemWidget extends Button {

        private final Item item;

        private boolean selected;

        public ItemWidget(int pX, int pY, int pWidth, int pHeight, Component message, OnPress onPress, Item item) {
            super(pX, pY, pWidth, pHeight, message, onPress, Button.DEFAULT_NARRATION);
            this.item = item;
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
            graphics.renderFakeItem(this.item.getDefaultInstance(), this.getX() + 1, this.getY() + 1);
        }

        @Override
        public void onPress() {
            this.selected = !this.selected;
            super.onPress();
        }
    }
}
