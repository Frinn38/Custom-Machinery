package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.regex.Pattern;

public class BaseInfoScreen extends MachineCreationTabScreen {

    private TextFieldWidget namePrompt;
    private TextFieldWidget idPrompt;

    public BaseInfoScreen(MachineCreationScreen parent, CustomMachineBuilder machine) {
        super(new StringTextComponent("Base Machine Infos"), parent, machine);
    }

    @Override
    protected void init() {
        super.init();

        this.namePrompt = this.addListener(new TextFieldWidget(
                this.font,
                this.xPos + 50,
                this.yPos + 10,
                150,
                20,
                StringTextComponent.EMPTY
        ));
        this.namePrompt.setText(this.machine.getName());
        this.namePrompt.setResponder(this.machine::setName);
        this.idPrompt = this.addListener(new TextFieldWidget(
                this.font,
                this.xPos + 50,
                this.yPos + 35,
                150,
                20,
                StringTextComponent.EMPTY
        ));
        this.idPrompt.setValidator(ResourceLocation::isResouceNameValid);
        this.idPrompt.setText(this.machine.getLocation().getId().getPath());
        this.idPrompt.setResponder(id -> this.machine.setId(new ResourceLocation(this.machine.getLocation().getId().getNamespace(), id)));
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.baseinfo.name").getString(), this.xPos + 8, this.yPos + 15, 0);
        this.namePrompt.render(matrix, mouseX, mouseY, partialTicks);
        this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.baseinfo.id").getString(), this.xPos + 8, this.yPos + 40, 0);
        this.idPrompt.render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public void setListener(@Nullable IGuiEventListener listener) {
        if(listener instanceof TextFieldWidget) {
            if(this.getListener() == this.namePrompt && listener != this.namePrompt)
                this.namePrompt.setFocused2(false);
            else if(this.getListener() == this.idPrompt && listener != this.idPrompt)
                this.idPrompt.setFocused2(false);
        }
        super.setListener(listener);
    }
}
