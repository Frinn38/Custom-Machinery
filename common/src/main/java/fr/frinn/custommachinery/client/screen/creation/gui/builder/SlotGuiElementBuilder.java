package fr.frinn.custommachinery.client.screen.creation.gui.builder;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.client.screen.creation.gui.GuiElementBuilderPopup;
import fr.frinn.custommachinery.client.screen.creation.gui.IGuiElementBuilder;
import fr.frinn.custommachinery.client.screen.creation.gui.MutableProperties;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.GroupWidget;
import fr.frinn.custommachinery.client.screen.widget.IntegerSlider;
import fr.frinn.custommachinery.client.screen.widget.SuggestedEditBox;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Color;
import fr.frinn.custommachinery.common.util.GhostItem;
import fr.frinn.custommachinery.common.util.ingredient.ItemIngredient;
import fr.frinn.custommachinery.impl.guielement.AbstractGuiElement.Properties;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.function.Consumer;

public class SlotGuiElementBuilder implements IGuiElementBuilder<SlotGuiElement> {

    @Override
    public GuiElementType<SlotGuiElement> type() {
        return Registration.SLOT_GUI_ELEMENT.get();
    }

    @Override
    public SlotGuiElement make(Properties properties, @Nullable SlotGuiElement from) {
        if(from != null)
            return new SlotGuiElement(properties, from.getGhost());
        else
            return new SlotGuiElement(properties, GhostItem.EMPTY);
    }

    @Override
    public PopupScreen makeConfigPopup(MachineEditScreen parent, MutableProperties properties, SlotGuiElement from, Consumer<SlotGuiElement> onFinish) {
        return new SlotGuiElementBuilderPopup(parent, properties, from, onFinish);
    }

    public static class SlotGuiElementBuilderPopup extends GuiElementBuilderPopup<SlotGuiElement> {

        private GhostItemWidget ghostItem;

        public SlotGuiElementBuilderPopup(BaseScreen parent, MutableProperties properties, @Nullable SlotGuiElement from, Consumer<SlotGuiElement> onFinish) {
            super(parent, properties, from, onFinish);
        }

        @Override
        public SlotGuiElement makeElement() {
            return new SlotGuiElement(this.properties.build(), this.ghostItem.getGhost());
        }

        @Override
        public Component canCreate() {
            if(this.properties.getId().isEmpty())
                return Component.translatable("custommachinery.gui.creation.gui.id.missing");
            return super.canCreate();
        }

        @Override
        public void addWidgets(RowHelper row) {
            this.addTexture(row, Component.translatable("custommachinery.gui.creation.gui.texture"), this.properties::setTexture, this.baseElement == null ? SlotGuiElement.BASE_TEXTURE : this.baseElement.getTexture());
            this.addId(row);
            this.addPriority(row);
            row.addChild(new StringWidget(Component.translatable("custommachinery.gui.creation.gui.slot.ghost"), this.font));
            this.ghostItem = row.addChild(new GhostItemWidget());
            if(this.baseElement != null && this.baseElement.getGhost() != GhostItem.EMPTY)
                this.ghostItem.setGhost(this.baseElement.getGhost());
        }
    }

    public static class GhostItemWidget extends GroupWidget {

        private static final ResourceLocation WIDGETS = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/style_widget.png");
        private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_slot.png");

        private final SuggestedEditBox items;
        private final Checkbox alwaysVisible;
        private final IntegerSlider transparency;
        private Color color = Color.WHITE;

        public GhostItemWidget() {
            super(0, 0, 100, 60, Component.empty());
            this.items = this.addWidget(new SuggestedEditBox(Minecraft.getInstance().font, 0, 0, 100, 20, Component.empty(), 5));
            this.items.addSuggestions(BuiltInRegistries.ITEM.keySet().stream().map(ResourceLocation::toString).toList());
            this.alwaysVisible = this.addWidget(new Checkbox(80, 22, 20, 20, Component.empty(), false));
            this.alwaysVisible.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.gui.slot.ghost.alwaysVisible")));
            this.transparency = this.addWidget(IntegerSlider.builder().displayOnlyValue().bounds(0, 100).defaultValue(100).setResponder(value -> this.color = Color.fromColors((int)(value * 2.55f), this.color.getRed(), this.color.getGreen(), this.color.getBlue())).create(0, 43, 100, 20, Component.translatable("custommachinery.gui.creation.gui.slot.ghost.transparency", 100)));
            for(int i = 0; i < 16; i++) {
                ChatFormatting format = ChatFormatting.getById(i);
                if(format == null)
                    continue;
                ImageButton button = new ImageButton(i % 8 * 10 - 1, (i < 8 ? 0 : 10) + 22, 10, 10, i * 10, 0, WIDGETS, b -> this.color = format.getColor() != null ? Color.fromARGB(this.color.getAlpha() << 24 | format.getColor()) : Color.WHITE);
                this.addWidget(button);
                button.setTooltip(Tooltip.create(Component.translatable(format.getName()).withStyle(format)));
            }
        }

        public void setGhost(GhostItem ghost) {
            this.items.setValue(BuiltInRegistries.ITEM.getKey(ghost.items().get(0).getAll().get(0)).toString());
            this.items.hideSuggestions();
            if(ghost.alwaysRender() != this.alwaysVisible.selected())
                this.alwaysVisible.onPress();
            this.transparency.setValue((int)(ghost.color().getAlpha() / 255f * 100));
            this.color = ghost.color();
        }

        public GhostItem getGhost() {
            try {
                return new GhostItem(Collections.singletonList(new ItemIngredient(BuiltInRegistries.ITEM.get(new ResourceLocation(this.items.getValue())))), this.color, this.alwaysVisible.selected());
            } catch (ResourceLocationException | NullPointerException e) {
                return GhostItem.EMPTY;
            }
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(SLOT_TEXTURE, this.getX() - 20, this.getY(), 18, 18, 0, 0, 18, 18, 18, 18);
            try {
                graphics.setColor(this.color.getRed() / 255f, this.color.getGreen() / 255f, this.color.getBlue() / 255f, this.color.getAlpha() / 255f);
                graphics.renderItem(BuiltInRegistries.ITEM.get(new ResourceLocation(this.items.getValue())).getDefaultInstance(), this.getX() - 19, this.getY() + 1);
                graphics.setColor(1f, 1f, 1f, 1f);
            } catch (ResourceLocationException | NullPointerException ignored) {
                System.out.println("NULL");
            }
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }
    }
}
