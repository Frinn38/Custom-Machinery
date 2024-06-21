package fr.frinn.custommachinery.common.guielement;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.machine.MachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ResetGuiElement extends AbstractTexturedGuiElement {

    public static final ResourceLocation BASE_TEXTURE = CustomMachinery.rl("textures/gui/base_reset.png");
    public static final ResourceLocation BASE_TEXTURE_HOVERED = CustomMachinery.rl("textures/gui/base_reset_hovered.png");

    public static final NamedCodec<ResetGuiElement> CODEC = NamedCodec.record(resetGuiElement ->
            resetGuiElement.group(
                    makePropertiesCodec(BASE_TEXTURE, BASE_TEXTURE_HOVERED, Lists.newArrayList(
                            Component.translatable("custommachinery.gui.element.reset.name"),
                            Component.translatable("custommachinery.gui.element.reset.tooltip").withStyle(ChatFormatting.DARK_RED)
                    )).forGetter(ResetGuiElement::getProperties)
            ).apply(resetGuiElement, ResetGuiElement::new), "Reset gui element"
    );

    public ResetGuiElement(Properties properties) {
        super(properties);
    }

    @Override
    public GuiElementType<ResetGuiElement> getType() {
        return Registration.RESET_GUI_ELEMENT.get();
    }

    @Override
    public void handleClick(byte button, MachineTile tile, AbstractContainerMenu container, ServerPlayer player) {
        tile.resetProcess();
    }
}
