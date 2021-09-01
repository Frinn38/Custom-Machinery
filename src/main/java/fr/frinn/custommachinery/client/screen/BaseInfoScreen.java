package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.common.data.MachineLocation;
import fr.frinn.custommachinery.common.data.builder.CustomMachineBuilder;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class BaseInfoScreen extends MachineCreationTabScreen {

    private TextFieldWidget namePrompt;
    private TextFieldWidget namespacePrompt;
    private TextFieldWidget idPrompt;
    private TextFieldWidget packNamePrompt;

    public BaseInfoScreen(MachineCreationScreen parent, CustomMachineBuilder machine) {
        super(new StringTextComponent("Base Machine Infos"), parent, machine);
    }

    @Override
    protected void init() {
        super.init();

        this.namePrompt = this.addListener(new TextFieldWidget(
                this.font,
                this.xPos + 65,
                this.yPos + 10,
                150,
                20,
                StringTextComponent.EMPTY
        ));
        this.namePrompt.setText(this.machine.getName().getString());
        this.namePrompt.setResponder(s -> this.machine.setName(new StringTextComponent(s)));
        this.namespacePrompt = this.addListener(new TextFieldWidget(
                this.font,
                this.xPos + 65,
                this.yPos + 35,
                150,
                20,
                StringTextComponent.EMPTY
        ));
        this.namespacePrompt.setValidator(s -> ResourceLocation.isResouceNameValid(s + ":"));
        this.namespacePrompt.setText(this.machine.getLocation().getId().getNamespace());
        this.namespacePrompt.setResponder(id -> this.machine.setId(new ResourceLocation(id, this.machine.getLocation().getId().getPath())));
        this.idPrompt = this.addListener(new TextFieldWidget(
                this.font,
                this.xPos + 65,
                this.yPos + 60,
                150,
                20,
                StringTextComponent.EMPTY
        ));
        this.idPrompt.setValidator(ResourceLocation::isResouceNameValid);
        this.idPrompt.setText(this.machine.getLocation().getId().getPath());
        this.idPrompt.setResponder(id -> this.machine.setId(new ResourceLocation(this.machine.getLocation().getId().getNamespace(), id)));
        this.packNamePrompt = this.addListener(new TextFieldWidget(
                this.font,
                this.xPos + 65,
                this.yPos + 85,
                150,
                20,
                StringTextComponent.EMPTY
        ));
        this.packNamePrompt.setText(this.machine.getLocation().getPackName());
        this.packNamePrompt.setResponder(packName -> this.machine.setLocation(MachineLocation.fromDatapack(this.machine.getLocation().getId(), packName)));
    }

    @ParametersAreNonnullByDefault
    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.baseinfo.name").getString(), this.xPos + 6, this.yPos + 15, 0);
        this.namePrompt.render(matrix, mouseX, mouseY, partialTicks);
        this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.baseinfo.namespace").getString(), this.xPos + 6, this.yPos + 40, 0);
        this.namespacePrompt.render(matrix, mouseX, mouseY, partialTicks);
        this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.baseinfo.id").getString(), this.xPos + 6, this.yPos + 65, 0);
        this.idPrompt.render(matrix, mouseX, mouseY, partialTicks);
        if(this.machine.getLocation().getLoader() == MachineLocation.Loader.DATAPACK) {
            this.font.drawString(matrix, new TranslationTextComponent("custommachinery.gui.baseinfo.packname").getString(), this.xPos + 6, this.yPos + 90, 0);
            this.packNamePrompt.render(matrix, mouseX, mouseY, partialTicks);
        }
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
