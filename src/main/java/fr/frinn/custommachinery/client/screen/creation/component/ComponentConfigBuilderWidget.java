package fr.frinn.custommachinery.client.screen.creation.component;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.popup.PopupScreen;
import fr.frinn.custommachinery.client.screen.widget.config.AutoIOModeButton;
import fr.frinn.custommachinery.client.screen.widget.config.SideModeButton;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import fr.frinn.custommachinery.impl.component.config.IOSideMode;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.component.config.SideConfig.SideMode;
import fr.frinn.custommachinery.impl.component.config.SideConfig.Template;
import fr.frinn.custommachinery.impl.component.config.ToggleSideConfig;
import fr.frinn.custommachinery.impl.component.config.ToggleSideMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;

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

        private static final WidgetSprites ALL_NONE_SPRITES = new WidgetSprites(CustomMachinery.rl("config/all_none_button"), CustomMachinery.rl("config/all_none_button_hovered"));
        private static final WidgetSprites EXIT_SPRITES = new WidgetSprites(CustomMachinery.rl("config/exit_button"), CustomMachinery.rl("config/exit_button_hovered"));
        private static final Component TITLE = Component.translatable("custommachinery.gui.config.component");

        private final Consumer<Template<?>> onFinish;
        private Template<?> template;

        public ComponentConfigBuilderPopup(BaseScreen parent, Supplier<Template<?>> baseConfig, Consumer<Template<?>> onFinish) {
            super(parent, 96, 96);
            this.template = baseConfig.get();
            this.onFinish = onFinish;
        }

        @Override
        protected void init() {
            super.init();
            //TOP
            this.addRenderableWidget(new SideModeButton(this.x + 41, this.y + 25, () -> this.template.sides().get(RelativeSide.TOP), RelativeSide.TOP, button -> this.setSide(RelativeSide.TOP, true), button -> this.setSide(RelativeSide.TOP, false)));
            //LEFT
            this.addRenderableWidget(new SideModeButton(this.x + 25, this.y + 41, () -> this.template.sides().get(RelativeSide.LEFT), RelativeSide.LEFT, button -> this.setSide(RelativeSide.LEFT, true), button -> this.setSide(RelativeSide.LEFT, false)));
            //FRONT
            this.addRenderableWidget(new SideModeButton(this.x + 41, this.y + 41, () -> this.template.sides().get(RelativeSide.FRONT), RelativeSide.FRONT, button -> this.setSide(RelativeSide.FRONT, true), button -> this.setSide(RelativeSide.FRONT, false)));
            //RIGHT
            this.addRenderableWidget(new SideModeButton(this.x + 57, this.y + 41, () -> this.template.sides().get(RelativeSide.RIGHT), RelativeSide.RIGHT, button -> this.setSide(RelativeSide.RIGHT, true), button -> this.setSide(RelativeSide.RIGHT, false)));
            //BACK
            this.addRenderableWidget(new SideModeButton(this.x + 25, this.y + 57, () -> this.template.sides().get(RelativeSide.BACK), RelativeSide.BACK, button -> this.setSide(RelativeSide.BACK, true), button -> this.setSide(RelativeSide.BACK, false)));
            //BOTTOM
            this.addRenderableWidget(new SideModeButton(this.x + 41, this.y + 57, () -> this.template.sides().get(RelativeSide.BOTTOM), RelativeSide.BOTTOM, button -> this.setSide(RelativeSide.BOTTOM, true), button -> this.setSide(RelativeSide.BOTTOM, false)));
            if(this.template instanceof IOSideConfig.Template) {
                //AUTO-INPUT
                this.addRenderableWidget(new AutoIOModeButton(this.x + 18, this.y + 75, () -> ((IOSideConfig.Template)this.template).autoInput(), true, button -> this.setIO(true)));
                //AUTO-OUTPUT
                this.addRenderableWidget(new AutoIOModeButton(this.x + 50, this.y + 75, () -> ((IOSideConfig.Template)this.template).autoOutput(), false, button -> this.setIO(false)));
            }
            //All sides none
            ImageButton allNone = this.addRenderableWidget(new ImageButton(this.x + 78, this.y + 57, 14, 14, ALL_NONE_SPRITES, button -> this.setAllNone()));
            allNone.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.config.all_none")));
            //EXIT
            ImageButton close = this.addRenderableWidget(new ImageButton(this.x + 5, this.y + 5, 9, 9, EXIT_SPRITES, button -> this.parent.closePopup(this)));
            close.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.config.close")));
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.renderBackground(graphics, mouseX, mouseY, partialTicks);
            graphics.drawString(Minecraft.getInstance().font, TITLE, (int)(this.x + this.xSize / 2F - font.width(TITLE) / 2F), this.y + 5, 0, false);
        }

        @Override
        public void closed() {
            this.onFinish.accept(this.template);
        }

        private void setSide(RelativeSide side, boolean next) {
            if(this.template instanceof IOSideConfig.Template ioTemplate) {
                Map<RelativeSide, IOSideMode> sides = new HashMap<>(ioTemplate.sides());
                sides.put(side, next ? ioTemplate.sides().get(side).next() : ioTemplate.sides().get(side).previous());
                this.template = new IOSideConfig.Template(sides, ioTemplate.autoInput(), ioTemplate.autoOutput(), ioTemplate.enabled());
            } else if(this.template instanceof ToggleSideConfig.Template toggleTemplate) {
                Map<RelativeSide, ToggleSideMode> sides = new HashMap<>(toggleTemplate.sides());
                sides.put(side, toggleTemplate.sides().get(side) == ToggleSideMode.ENABLED ? ToggleSideMode.DISABLED : ToggleSideMode.ENABLED);
                this.template = new ToggleSideConfig.Template(sides, toggleTemplate.enabled());
            }
        }

        private void setIO(boolean input) {
            if(this.template instanceof IOSideConfig.Template ioTemplate) {
                if(input)
                    this.template = new IOSideConfig.Template(ioTemplate.sides(), !ioTemplate.autoInput(), ioTemplate.autoOutput(), ioTemplate.enabled());
                else
                    this.template = new IOSideConfig.Template(ioTemplate.sides(), ioTemplate.autoInput(), !ioTemplate.autoOutput(), ioTemplate.enabled());
            }
        }

        private void setAllNone() {
            if(this.template instanceof IOSideConfig.Template ioTemplate)
                this.template = new IOSideConfig.Template(new HashMap<>(IOSideConfig.Template.DEFAULT_ALL_NONE.sides()), ioTemplate.autoInput(), ioTemplate.autoOutput(), ioTemplate.enabled());
            else if(this.template instanceof ToggleSideConfig.Template toggleTemplate)
                this.template = new ToggleSideConfig.Template(new HashMap<>(ToggleSideConfig.Template.DEFAULT_ALL_DISABLED.sides()), toggleTemplate.enabled());
        }
    }
}
