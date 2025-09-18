package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.joml.Matrix4f;

public record MachineTooltipComponent(CustomMachine machine) implements TooltipComponent {

    public static class ClientMachineTooltipComponent implements ClientTooltipComponent {

        private final CustomMachine machine;
        private final CustomMachineScreen screen;

        public ClientMachineTooltipComponent(MachineTooltipComponent component) {
            this.machine = component.machine();
            if(Minecraft.getInstance().player == null)
                throw new IllegalStateException();
            Inventory playerInv = Minecraft.getInstance().player.getInventory();
            this.screen = new TemplateMachineScreen(new CustomMachineContainer(0, playerInv, new TemplateMachineTile()), playerInv, Component.empty());
            this.screen.init(Minecraft.getInstance(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
        }

        @Override
        public int getHeight() {
            return this.screen.getYSize() / 2;
        }

        @Override
        public int getWidth(Font font) {
            return this.screen.getXSize() / 2;
        }

        @Override
        public void renderText(Font font, int mouseX, int mouseY, Matrix4f matrix, BufferSource bufferSource) {
            font.drawInBatch(Component.empty(), (float)mouseX, (float)mouseY, -1, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
            graphics.pose().pushPose();
            graphics.pose().scale(1/2f, 1/2f, 1f);
            graphics.pose().translate(x * 2 - this.screen.getX(), y * 2 - this.screen.getY(), 0);
            this.screen.render(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            graphics.pose().popPose();
        }

        private class TemplateMachineTile extends CustomMachineTile {

            public TemplateMachineTile() {
                super(BlockPos.ZERO, Registration.CUSTOM_MACHINE_BLOCK.get().defaultBlockState());
            }

            @Override
            public CustomMachine getMachine() {
                return ClientMachineTooltipComponent.this.machine;
            }
        }

        private class TemplateMachineScreen extends CustomMachineScreen {

            public TemplateMachineScreen(CustomMachineContainer container, Inventory inv, Component name) {
                super(container, inv, name);
            }

            @Override
            public void renderTransparentBackground(GuiGraphics graphics) {

            }
        }
    }
}
