package fr.frinn.custommachinery.client.screen.creator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MachineList extends ObjectSelectionList<MachineList.MachineEntry> {

    private MachineLoadingScreen parent;

    public MachineList(Minecraft mc, int width, int height, int x, int y, int entryHeight, MachineLoadingScreen parent) {
        super(mc, width, height, y, y + height, entryHeight);
        this.setLeftPos(x);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.centerListVertically = false;
        this.setRenderSelection(false);
        this.parent = parent;
    }

    protected int addMachineEntry(CustomMachineBuilder machine) {
        return this.addEntry(new MachineEntry(machine, this));
    }

    protected void removeMachineEntry(CustomMachineBuilder machine) {
        List<MachineEntry> entriesToDelete = this.children().stream().filter(entry -> entry.machineBuilder == machine).toList();
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

    //TODO: Add scrollbar
    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        double s = Minecraft.getInstance().getWindow().getGuiScale();
        int screenHeight = Minecraft.getInstance().getWindow().getScreenHeight() / (int)s;
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

    public static class MachineEntry extends ObjectSelectionList.Entry<MachineList.MachineEntry> {

        private CustomMachineBuilder machineBuilder;
        private MachineList machineList;

        public MachineEntry(CustomMachineBuilder machineBuilder, MachineList list) {
            this.machineBuilder = machineBuilder;
            this.machineList = list;
        }

        @Override
        public void render(PoseStack matrix, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean isFocused, float partialTicks) {
            if(this.machineBuilder != null) {
                int nameWidth = Minecraft.getInstance().font.width(this.machineBuilder.getName().getString());
                float scale = Mth.clamp((float)(width - 6) / (float) nameWidth, 0, 2.0F);
                matrix.pushPose();
                matrix.translate(x, y, 0);
                matrix.scale(scale, scale, 0.0F);
                if(this.machineList.getSelected() != this)
                    Minecraft.getInstance().font.draw(matrix, this.machineBuilder.getName(), 0, 0, 0);
                else
                    Minecraft.getInstance().font.drawShadow(matrix, this.machineBuilder.getName(), 0, 0, DyeColor.RED.getId());
                matrix.scale(0.8F, 0.8F, 0.0F);
                Minecraft.getInstance().font.draw(matrix, this.machineBuilder.getLocation().getLoader().getTranslatedName().getString(), 0, 11, this.machineBuilder.getLocation().getLoader().getColor());
                matrix.popPose();
            }
            else Minecraft.getInstance().font.draw(matrix, "NULL", x, y, 0);

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

        @Override
        public Component getNarration() {
            return null;
        }

        public CustomMachineBuilder getMachineBuilder() {
            return this.machineBuilder;
        }
    }
}
