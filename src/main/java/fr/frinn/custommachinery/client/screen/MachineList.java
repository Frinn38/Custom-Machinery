package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class MachineList extends ExtendedList<MachineList.MachineEntry> {

    private MachineLoadingScreen parent;

    public MachineList(Minecraft mc, int width, int height, int x, int y, int entryheight, MachineLoadingScreen parent) {
        super(mc, width, height, y, y + height, entryheight);
        this.setLeftPos(x);
        this.func_244605_b(false);
        this.func_244606_c(false);
        this.centerListVertically = false;
        this.setRenderSelection(false);
        this.parent = parent;
    }

    protected int addMachineEntry(CustomMachineBuilder machine) {
        return this.addEntry(new MachineEntry(machine, this));
    }

    protected void removeMachineEntry(CustomMachineBuilder machine) {
        List<MachineEntry> entriesToDelete = this.getEventListeners().stream().filter(entry -> entry.machineBuilder == machine).collect(Collectors.toList());
        entriesToDelete.forEach(this::removeEntry);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width + 6;
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        double s = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
        int screenHeight = Minecraft.getInstance().getMainWindow().getHeight() / (int)s;
        RenderSystem.enableScissor(this.x0 * (int)s, (screenHeight - this.y0 - this.height) * (int)s, this.width * (int)s, (this.height - 3) * (int)s);
        super.render(matrix, mouseX, mouseY, partialTicks);
        RenderSystem.disableScissor();
    }

    @Override
    public void setSelected(@Nullable MachineEntry entry) {
        super.setSelected(entry);
        if(entry != null)
            this.parent.setSelectedMachine(entry.getMachineBuilder());
        else
            this.parent.setSelectedMachine(null);
    }

    public static class MachineEntry extends AbstractList.AbstractListEntry<MachineList.MachineEntry> {

        private CustomMachineBuilder machineBuilder;
        private MachineList machineList;

        public MachineEntry(CustomMachineBuilder machineBuilder, MachineList list) {
            this.machineBuilder = machineBuilder;
            this.machineList = list;
        }

        @ParametersAreNonnullByDefault
        @Override
        public void render(MatrixStack matrix, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isFocused, float partialTicks) {
            if(this.machineBuilder != null) {
                int nameWidth = Minecraft.getInstance().fontRenderer.getStringWidth(this.machineBuilder.getName());
                float scale = MathHelper.clamp((float)(width - 6) / (float) nameWidth, 0, 2.0F);
                matrix.push();
                matrix.translate(x, y, 0);
                matrix.scale(scale, scale, 0.0F);
                if(this.machineList.getSelected() != this)
                    Minecraft.getInstance().fontRenderer.drawString(matrix, this.machineBuilder.getName(), 0, 0, 0);
                else
                    Minecraft.getInstance().fontRenderer.drawStringWithShadow(matrix, this.machineBuilder.getName(), 0, 0, Color.RED.getRGB());
                matrix.scale(0.8F, 0.8F, 0.0F);
                Minecraft.getInstance().fontRenderer.drawString(matrix, this.machineBuilder.getLocation().getLoader().getTranslatedName().getString(), 0, 11, this.machineBuilder.getLocation().getLoader().getColor());
                matrix.pop();
            }
            else
                Minecraft.getInstance().fontRenderer.drawString(matrix, "NULL", x, y, 0);

            /*
            BufferBuilder builder = Tessellator.getInstance().getBuffer();
            builder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            builder.pos(matrix.getLast().getMatrix(), x, y + height, 0).color(1.0F, 0, 0, 1.0F).endVertex();
            builder.pos(matrix.getLast().getMatrix(), x + width, y + height, 0).color(1.0F, 0, 0, 1.0F).endVertex();
            builder.pos(matrix.getLast().getMatrix(), x + width, y, 0).color(1.0F, 0, 0, 1.0F).endVertex();
            builder.pos(matrix.getLast().getMatrix(), x, y, 0).color(1.0F, 0, 0, 1.0F).endVertex();
            Tessellator.getInstance().draw();
            */
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.machineList.setSelected(this);
            return true;
        }

        public CustomMachineBuilder getMachineBuilder() {
            return this.machineBuilder;
        }
    }
}
