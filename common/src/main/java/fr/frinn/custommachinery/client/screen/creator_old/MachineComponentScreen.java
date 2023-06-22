package fr.frinn.custommachinery.client.screen.creator_old;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.machine.builder.CustomMachineBuilder;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MachineComponentScreen extends MachineCreationTabScreen {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/machine_list_background.png");

    private ComponentList componentList;

    public MachineComponentScreen(MachineCreationScreen parent, CustomMachineBuilder machine) {
        super(Component.literal("Machine Components"), parent, machine);
    }

    @Override
    protected void init() {
        super.init();
        this.componentList = new ComponentList(this.minecraft, 63, 111, this.xPos + 264, this.yPos + 5, 20, this);
        this.machine.getComponentBuilders().forEach(builder -> this.componentList.addComponent(builder));
        ((List<GuiEventListener>)this.children()).add(this.componentList);
    }

    @Override
    public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrix);

        if(this.componentList.children().isEmpty())
            this.font.draw(matrix, Component.translatable("custommachinery.gui.component.empty").getString(), this.xPos + 5, this.yPos + 130, 0);
        else if(this.componentList.getSelected() == null)
            this.font.draw(matrix, Component.translatable("custommachinery.gui.component.select").getString(), this.xPos + 5, this.yPos + 5, 0);

        this.componentList.render(matrix, mouseX, mouseY, partialTicks);

        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(PoseStack matrix) {
        ClientHandler.bindTexture(BACKGROUND_TEXTURE);
        blit(matrix, this.xPos + 259, this.yPos, 0, 0, 72, 166, 72, 166);
    }

    public List<GuiEventListener> getChildrens() {
        return (List<GuiEventListener>)this.children();
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        this.getChildrens().stream().filter(widget -> widget instanceof EditBox && widget != listener).map(widget -> (EditBox)widget).forEach(widget -> widget.setFocus(false));
        super.setFocused(listener);
    }
}
