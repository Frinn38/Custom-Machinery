package fr.frinn.custommachinery.client.screen.widget.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.client.screen.widget.custom.MachineList.MachineEntry;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

import java.util.function.Supplier;

public class MachineList extends ListWidget<CustomMachineBuilder, MachineEntry> {

    public MachineList(Supplier<Integer> x, Supplier<Integer> y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void add(CustomMachineBuilder value) {
        this.add(new MachineEntry(value));
    }

    public static class MachineEntry extends Entry<CustomMachineBuilder> {

        public MachineEntry(CustomMachineBuilder value) {
            super(value);
        }

        @Override
        public void render(PoseStack pose, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
            CustomMachineBuilder builder = this.getValue();
            Font font = Minecraft.getInstance().font;
            int nameWidth = font.width(builder.getName().getString());
            float scale = Mth.clamp((float)(width - 8) / (float) nameWidth, 0, 0.8F);
            pose.pushPose();
            pose.translate(x + 2, y + 2, 0);
            pose.scale(scale, scale, 0.0F);
            font.draw(pose, builder.getName(), 0, 0, DyeColor.RED.getId());
            pose.scale(0.8F, 0.8F, 0.0F);
            font.draw(pose, builder.getLocation().getLoader().getTranslatedName().getString(), 0, 11, builder.getLocation().getLoader().getColor());
            pose.popPose();
        }
    }
}
