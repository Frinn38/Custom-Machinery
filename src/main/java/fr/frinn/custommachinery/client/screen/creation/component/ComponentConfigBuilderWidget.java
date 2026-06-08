package fr.frinn.custommachinery.client.screen.creation.component;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.ColorWidget;
import fr.frinn.custommachinery.client.screen.widget.GroupWidget;
import fr.frinn.custommachinery.client.screen.widget.config.AutoIOModeButton;
import fr.frinn.custommachinery.client.screen.widget.config.SideModeButton;
import fr.frinn.custommachinery.common.util.Color;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import fr.frinn.custommachinery.impl.component.config.IOSideMode;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.component.config.SideConfig.ConfigButtonData;
import fr.frinn.custommachinery.impl.component.config.SideConfig.ConfigGuiData;
import fr.frinn.custommachinery.impl.component.config.SideConfig.Template;
import fr.frinn.custommachinery.impl.component.config.ToggleSideConfig;
import fr.frinn.custommachinery.impl.component.config.ToggleSideMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ComponentConfigBuilderWidget extends Button {

    @SuppressWarnings("unchecked")
    public static <T extends Template<?>> ComponentConfigBuilderWidget make(int x, int y, int width, int height, Component message, BaseScreen parent, Supplier<T> baseConfig, Consumer<T> onFinish) {
        return new ComponentConfigBuilderWidget(x, y, width, height, message, parent, (Supplier<Template<?>>) baseConfig, (Consumer<Template<?>>) onFinish);
    }

    private ComponentConfigBuilderWidget(int x, int y, int width, int height, Component message, BaseScreen parent, Supplier<Template<?>> baseConfig, Consumer<Template<?>> onFinish) {
        super(x, y, width, height, message, button -> parent.openPopup(new ComponentConfigBuilderPopup(parent, baseConfig, onFinish), "IO Config"), Button.DEFAULT_NARRATION);
    }

    public static class ComponentConfigBuilderPopup extends PopupScreen {

        private final Consumer<Template<?>> onFinish;
        private Template<?> template;

        public ComponentConfigBuilderPopup(BaseScreen parent, Supplier<Template<?>> baseConfig, Consumer<Template<?>> onFinish) {
            super(parent, baseConfig.get().guiData().width(), baseConfig.get().guiData().height());
            this.template = baseConfig.get();
            this.onFinish = onFinish;
        }

        @Override
        protected void init() {
            super.init();
            GroupWidget guiConfig = this.addRenderableWidget(new GroupWidget(this.x, this.y, this.template.guiData().width(), this.template.guiData().height(), this.template.guiData().title()));
            //TOP
            guiConfig.addWidget(new SideModeButton(this.x, this.y, () -> this.template.sides().get(RelativeSide.TOP), RelativeSide.TOP, button -> this.setSide(RelativeSide.TOP, true), button -> this.setSide(RelativeSide.TOP, false), this.template.guiData().top()));
            //LEFT
            guiConfig.addWidget(new SideModeButton(this.x, this.y, () -> this.template.sides().get(RelativeSide.LEFT), RelativeSide.LEFT, button -> this.setSide(RelativeSide.LEFT, true), button -> this.setSide(RelativeSide.LEFT, false), this.template.guiData().left()));
            //FRONT
            guiConfig.addWidget(new SideModeButton(this.x, this.y, () -> this.template.sides().get(RelativeSide.FRONT), RelativeSide.FRONT, button -> this.setSide(RelativeSide.FRONT, true), button -> this.setSide(RelativeSide.FRONT, false), this.template.guiData().front()));
            //RIGHT
            guiConfig.addWidget(new SideModeButton(this.x, this.y, () -> this.template.sides().get(RelativeSide.RIGHT), RelativeSide.RIGHT, button -> this.setSide(RelativeSide.RIGHT, true), button -> this.setSide(RelativeSide.RIGHT, false), this.template.guiData().right()));
            //BACK
            guiConfig.addWidget(new SideModeButton(this.x, this.y, () -> this.template.sides().get(RelativeSide.BACK), RelativeSide.BACK, button -> this.setSide(RelativeSide.BACK, true), button -> this.setSide(RelativeSide.BACK, false), this.template.guiData().back()));
            //BOTTOM
            guiConfig.addWidget(new SideModeButton(this.x, this.y, () -> this.template.sides().get(RelativeSide.BOTTOM), RelativeSide.BOTTOM, button -> this.setSide(RelativeSide.BOTTOM, true), button -> this.setSide(RelativeSide.BOTTOM, false), this.template.guiData().bottom()));
            if(this.template instanceof IOSideConfig.Template) {
                //AUTO-INPUT
                guiConfig.addWidget(new AutoIOModeButton(this.x, this.y, () -> ((IOSideConfig.Template)this.template).autoInput(), true, button -> this.setIO(true), this.template.guiData().input()));
                //AUTO-OUTPUT
                guiConfig.addWidget(new AutoIOModeButton(this.x, this.y, () -> ((IOSideConfig.Template)this.template).autoOutput(), false, button -> this.setIO(false), this.template.guiData().output()));
            }
            //All sides none
            ConfigButtonData noneData = this.template.guiData().none();
            ImageButton allNone = guiConfig.addWidget(new ImageButton(this.x + noneData.x(), this.y + noneData.y(), noneData.width(), noneData.height(), noneData.sprites(), button -> this.setAllNone()));
            allNone.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.config.all_none")));
            //EXIT
            ConfigButtonData exitData = this.template.guiData().exit();
            ImageButton close = guiConfig.addWidget(new ImageButton(this.x + exitData.x(), this.y + exitData.y(), exitData.width(), exitData.height(), exitData.sprites(), button -> this.parent.closePopup(this)));
            close.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.config.close")));

            guiConfig.setPosition(this.x, this.y);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.renderBackground(graphics, mouseX, mouseY, partialTicks);
            graphics.blit(this.template.guiData().background().texture(), this.x, this.y, this.template.guiData().background().u(), this.template.guiData().background().v(), this.template.guiData().width(), this.template.guiData().height(), this.template.guiData().background().width(), this.template.guiData().background().height());
            graphics.drawString(Minecraft.getInstance().font, this.template.guiData().title(), (int)(this.x + this.template.guiData().width() / 2F - font.width(this.template.guiData().title()) / 2F), this.y + 5, 0, false);
        }

        @Override
        public void closed() {
            this.onFinish.accept(this.template);
        }

        private void setSide(RelativeSide side, boolean next) {
            if(this.template instanceof IOSideConfig.Template ioTemplate) {
                Map<RelativeSide, IOSideMode> sides = new HashMap<>(ioTemplate.sides());
                sides.put(side, next ? ioTemplate.sides().get(side).next() : ioTemplate.sides().get(side).previous());
                this.template = new IOSideConfig.Template(sides, ioTemplate.autoInput(), ioTemplate.autoOutput(), ioTemplate.enabled(), ioTemplate.color(), ioTemplate.guiData());
            } else if(this.template instanceof ToggleSideConfig.Template toggleTemplate) {
                Map<RelativeSide, ToggleSideMode> sides = new HashMap<>(toggleTemplate.sides());
                sides.put(side, toggleTemplate.sides().get(side) == ToggleSideMode.ENABLED ? ToggleSideMode.DISABLED : ToggleSideMode.ENABLED);
                this.template = new ToggleSideConfig.Template(sides, toggleTemplate.enabled(), toggleTemplate.color(), toggleTemplate.guiData());
            }
        }

        private void setIO(boolean input) {
            if(this.template instanceof IOSideConfig.Template(Map<RelativeSide, IOSideMode> sides, boolean autoInput, boolean autoOutput, boolean enabled, Color color, SideConfig.ConfigGuiData guiData)) {
                if(input)
                    this.template = new IOSideConfig.Template(sides, !autoInput, autoOutput, enabled, color, guiData);
                else
                    this.template = new IOSideConfig.Template(sides, autoInput, !autoOutput, enabled, color, guiData);
            }
        }

        private void setAllNone() {
            if(this.template instanceof IOSideConfig.Template ioTemplate)
                this.template = new IOSideConfig.Template(new HashMap<>(IOSideConfig.Template.DEFAULT_ALL_NONE.sides()), ioTemplate.autoInput(), ioTemplate.autoOutput(), ioTemplate.enabled(), ioTemplate.color(), ioTemplate.guiData());
            else if(this.template instanceof ToggleSideConfig.Template toggleTemplate)
                this.template = new ToggleSideConfig.Template(new HashMap<>(ToggleSideConfig.Template.DEFAULT_ALL_DISABLED.sides()), toggleTemplate.enabled(), toggleTemplate.color(), toggleTemplate.guiData());
        }
    }
}
