package fr.frinn.custommachinery.client.screen.creation;

import fr.frinn.custommachinery.client.screen.CustomMachineScreen;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

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
            this.screen.init(Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
        }

        @Override
        public int getHeight(Font font) {
            return this.screen.getImageHeight() / 2;
        }

        @Override
        public int getWidth(Font font) {
            return this.screen.getImageWidth() / 2;
        }

        @Override
        public void extractText(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {

        }

        @Override
        public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
            graphics.pose().pushMatrix();
            graphics.pose().scale(1/2f, 1/2f);
            graphics.pose().translation(x * 2 - this.screen.getX(), y * 2 - this.screen.getY());
            this.screen.extractRenderState(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            graphics.pose().popMatrix();
        }

        private class TemplateMachineTile extends CustomMachineTile {

            public TemplateMachineTile() {
                super(BlockPos.ZERO, CMRegistration.CUSTOM_MACHINE_BLOCK.get().defaultBlockState());
            }

            @Override
            public CustomMachine getMachine() {
                return ClientMachineTooltipComponent.this.machine;
            }
        }

        private static class TemplateMachineScreen extends CustomMachineScreen {

            public TemplateMachineScreen(CustomMachineContainer container, Inventory inv, Component name) {
                super(container, inv, name);
            }

            @Override
            public void extractTransparentBackground(GuiGraphicsExtractor graphics) {

            }
        }
    }
}
