package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.ParametersAreNonnullByDefault;

public class MachineGuiCreationScreen extends MachineCreationTabScreen {

    public MachineGuiCreationScreen(MachineCreationScreen parent, CustomMachineBuilder machine) {
        super(new StringTextComponent("Machine Gui Creation"), parent, machine);
    }

    @Override
    protected void init() {
        super.init();
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.font.drawString(matrix, "GUI", this.xPos + 20, this.yPos + 20, 0);

        super.render(matrix, mouseX, mouseY, partialTicks);
    }
}
