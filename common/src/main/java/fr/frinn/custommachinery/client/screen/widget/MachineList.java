package fr.frinn.custommachinery.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import fr.frinn.custommachinery.client.screen.widget.MachineList.MachineEntry;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.util.Color3F;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MachineList extends ObjectSelectionList<MachineEntry> {

    private final Consumer<CustomMachineBuilder> onSelect;


    public MachineList(Minecraft minecraft, int width, int height, int x, int y, int entryHeight, Consumer<CustomMachineBuilder> onSelect) {
        super(minecraft, width, height, y, y + height, entryHeight);
        this.setLeftPos(x);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.centerListVertically = false;
        this.setRenderSelection(true);
        this.onSelect = onSelect;
    }

    public void addMachine(CustomMachineBuilder builder) {
        addEntry(new MachineEntry(builder, this));
    }

    public void removeMachine(CustomMachineBuilder builder) {
        this.children().removeIf(machineEntry -> machineEntry.machineBuilder == builder);
    }

    public void setSelected(CustomMachineBuilder selected) {
        for(MachineEntry entry : children())
            if(entry.machineBuilder == selected)
                setSelected(entry);
    }

    @Nullable
    public CustomMachineBuilder selected() {
        return this.getSelected() == null ? null : this.getSelected().machineBuilder;
    }

    @Override
    public void setSelected(@Nullable MachineList.MachineEntry selected) {
        super.setSelected(selected);
        this.onSelect.accept(selected == null ? null : selected.machineBuilder);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width - 6;
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        double s = Minecraft.getInstance().getWindow().getGuiScale();
        int screenHeight = Minecraft.getInstance().getWindow().getScreenHeight() / (int)s;
        RenderSystem.enableScissor(this.x0 * (int)s, (screenHeight - this.y0 - this.height) * (int)s, this.width * (int)s, this.height * (int)s);
        super.render(pose, mouseX, mouseY, partialTicks);
        RenderSystem.disableScissor();
    }

    public static class MachineEntry extends ObjectSelectionList.Entry<MachineEntry> {

        private final CustomMachineBuilder machineBuilder;
        private final MachineList machineList;

        public MachineEntry(CustomMachineBuilder machineBuilder, MachineList list) {
            this.machineBuilder = machineBuilder;
            this.machineList = list;
        }

        @Override
        public void render(PoseStack pose, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isFocused, float partialTicks) {
            Color3F color = Color3F.of(100, 100, 100);
            if(this.machineList.getSelected() == this) {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                BufferBuilder builder = Tesselator.getInstance().getBuilder();
                builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                builder.vertex(pose.last().pose(), x, y + height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
                builder.vertex(pose.last().pose(), x + width, y + height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
                builder.vertex(pose.last().pose(), x + width, y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
                builder.vertex(pose.last().pose(), x, y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), 1.0F).endVertex();
                Tesselator.getInstance().end();
            }

            if(this.machineBuilder != null) {
                int nameWidth = Minecraft.getInstance().font.width(this.machineBuilder.getName().getString());
                float scale = Mth.clamp((float)(width - 8) / (float) nameWidth, 0, 0.8F);
                pose.pushPose();
                pose.translate(x + 2, y + 2, 0);
                pose.scale(scale, scale, 0.0F);
                Minecraft.getInstance().font.draw(pose, this.machineBuilder.getName(), 0, 0, DyeColor.RED.getId());
                pose.scale(0.8F, 0.8F, 0.0F);
                Minecraft.getInstance().font.draw(pose, this.machineBuilder.getLocation().getLoader().getTranslatedName().getString(), 0, 11, this.machineBuilder.getLocation().getLoader().getColor());
                pose.popPose();
            }
            else Minecraft.getInstance().font.draw(pose, "NULL", x, y, 0);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.machineList.setSelected(this);
            return true;
        }

        @Override
        public Component getNarration() {
            if(this.machineBuilder != null)
                return this.machineBuilder.getName();
            return new TextComponent("NULL");
        }

        public CustomMachineBuilder getMachineBuilder() {
            return this.machineBuilder;
        }
    }
}
