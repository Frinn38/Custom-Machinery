package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class MachineComponentScreen extends MachineCreationTabScreen {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/machine_list_background.png");

    private ComponentList componentList;

    public MachineComponentScreen(MachineCreationScreen parent, CustomMachineBuilder machine) {
        super(new StringTextComponent("Machine Components"), parent, machine);
    }

    @Override
    protected void init() {
        super.init();
        this.componentList = new ComponentList(this.minecraft, 63, 111, this.xPos + 264, this.yPos + 5, 20, this);
        this.machine.getComponentBuilders().forEach(builder -> this.componentList.addComponent(builder));
        this.children.add(this.componentList);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);

        if(this.componentList.getEventListeners().isEmpty())
            this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.component.empty").getString(), this.xPos + 5, this.yPos + 130, 0);
        else if(this.componentList.getSelected() == null)
            this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.component.select").getString(), this.xPos + 5, this.yPos + 5, 0);

        this.componentList.render(matrix, mouseX, mouseY, partialTicks);

        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    @ParametersAreNonnullByDefault
    @Override
    public void renderBackground(MatrixStack matrix) {
        Minecraft.getInstance().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        blit(matrix, this.xPos + 259, this.yPos, 0, 0, 72, 166, 72, 166);
    }

    public List<IGuiEventListener> getChildrens() {
        return this.children;
    }

    @Override
    public void setListener(@Nullable IGuiEventListener listener) {
        this.getChildrens().stream().filter(widget -> widget instanceof TextFieldWidget && widget != listener).map(widget -> (TextFieldWidget)widget).forEach(widget -> widget.setFocused2(false));
        super.setListener(listener);
    }
}
