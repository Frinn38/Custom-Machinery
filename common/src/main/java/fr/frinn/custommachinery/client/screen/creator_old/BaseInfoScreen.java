package fr.frinn.custommachinery.client.screen.creator_old;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BaseInfoScreen extends MachineCreationTabScreen {

    private EditBox namePrompt;
    private EditBox namespacePrompt;
    private EditBox idPrompt;
    private EditBox packNamePrompt;

    public BaseInfoScreen(MachineCreationScreen parent, CustomMachineBuilder machine) {
        super(new TextComponent("Base Machine Infos"), parent, machine);
    }

    @Override
    protected void init() {
        super.init();

        this.namePrompt = this.addWidget(new EditBox(
                this.font,
                this.xPos + 65,
                this.yPos + 10,
                150,
                20,
                TextComponent.EMPTY
        ));
        this.namePrompt.setValue(this.machine.getName().getString());
        this.namePrompt.setResponder(s -> this.machine.setName(new TextComponent(s)));
        this.namespacePrompt = this.addWidget(new EditBox(
                this.font,
                this.xPos + 65,
                this.yPos + 35,
                150,
                20,
                TextComponent.EMPTY
        ));
        this.namespacePrompt.setFilter(s -> ResourceLocation.isValidResourceLocation(s + ":"));
        this.namespacePrompt.setValue(this.machine.getLocation().getId().getNamespace());
        this.namespacePrompt.setResponder(id -> this.machine.setId(new ResourceLocation(id, this.machine.getLocation().getId().getPath())));
        this.idPrompt = this.addWidget(new EditBox(
                this.font,
                this.xPos + 65,
                this.yPos + 60,
                150,
                20,
                TextComponent.EMPTY
        ));
        this.idPrompt.setFilter(ResourceLocation::isValidResourceLocation);
        this.idPrompt.setValue(this.machine.getLocation().getId().getPath());
        this.idPrompt.setResponder(id -> this.machine.setId(new ResourceLocation(this.machine.getLocation().getId().getNamespace(), id)));
        this.packNamePrompt = this.addWidget(new EditBox(
                this.font,
                this.xPos + 65,
                this.yPos + 85,
                150,
                20,
                TextComponent.EMPTY
        ));
        this.packNamePrompt.setValue(this.machine.getLocation().getPackName());
        this.packNamePrompt.setResponder(packName -> this.machine.setLocation(MachineLocation.fromDatapack(this.machine.getLocation().getId(), packName)));
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        this.font.draw(matrix, new TranslatableComponent("custommachinery.gui.baseinfo.name").getString(), this.xPos + 6, this.yPos + 15, 0);
        this.namePrompt.render(matrix, mouseX, mouseY, partialTicks);
        this.font.draw(matrix, new TranslatableComponent("custommachinery.gui.baseinfo.namespace").getString(), this.xPos + 6, this.yPos + 40, 0);
        this.namespacePrompt.render(matrix, mouseX, mouseY, partialTicks);
        this.font.draw(matrix, new TranslatableComponent("custommachinery.gui.baseinfo.id").getString(), this.xPos + 6, this.yPos + 65, 0);
        this.idPrompt.render(matrix, mouseX, mouseY, partialTicks);
        if(this.machine.getLocation().getLoader() == MachineLocation.Loader.DATAPACK) {
            this.font.draw(matrix, new TranslatableComponent("custommachinery.gui.baseinfo.packname").getString(), this.xPos + 6, this.yPos + 90, 0);
            this.packNamePrompt.render(matrix, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        if(listener instanceof EditBox) {
            if(this.getFocused() == this.namePrompt && listener != this.namePrompt)
                this.namePrompt.setFocus(false);
            else if(this.getFocused() == this.idPrompt && listener != this.idPrompt)
                this.idPrompt.setFocus(false);
        }
        super.setFocused(listener);
    }
}
