package fr.frinn.custommachinery.client.render.element;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.api.guielement.IMachineScreen;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.integration.config.CMConfig;
import fr.frinn.custommachinery.common.util.CycleTimer;
import fr.frinn.custommachinery.impl.guielement.TexturedGuiElementWidget;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public class SlotGuiElementWidget extends TexturedGuiElementWidget<SlotGuiElement> {

    private static final CycleTimer timer = new CycleTimer(() -> CMConfig.get().itemSlotCycleTime);

    public SlotGuiElementWidget(SlotGuiElement element, IMachineScreen screen) {
        super(element, screen, new TextComponent("Slot"));
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(poseStack, mouseX, mouseY, partialTicks);

        if(!this.getElement().getItems().isEmpty()) {
            timer.onDraw();
            List<Item> items = this.getElement().getItems().stream().flatMap(ingredient -> ingredient.getAll().stream()).toList();
            this.getScreen().drawGhostItem(poseStack, timer.getOrDefault(items, Items.AIR).getDefaultInstance(), this.x + 1, this.y + 1, this.getElement().getColor().getARGB());
        }
    }
}
