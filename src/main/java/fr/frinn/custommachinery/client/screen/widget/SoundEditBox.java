package fr.frinn.custommachinery.client.screen.widget;

import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SoundEditBox extends GroupWidget {

    private final SuggestedEditBox editBox;
    private final ImageButton playButton;
    @Nullable
    private SoundInstance currentSound = null;

    public SoundEditBox(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.editBox = this.addWidget(new SuggestedEditBox(this.font, x, y, width - 20, height, message, 5));
        this.editBox.setAnchorToBottom();
        this.editBox.setMaxLength(Integer.MAX_VALUE);
        this.editBox.addSuggestions(this.mc.getSoundManager().getAvailableSounds().stream().map(ResourceLocation::toString).toList());
        WidgetSprites sprites = new WidgetSprites(CustomMachinery.rl("creation/play_button"), CustomMachinery.rl("creation/play_button_disabled"), CustomMachinery.rl("creation/play_button_hovered"));
        this.playButton = this.addWidget(new ImageButton(x + width - 20, y, 20, 20, sprites, button -> {
            if(this.currentSound != null) {
                if(Minecraft.getInstance().getSoundManager().isActive(this.currentSound)) {
                    Minecraft.getInstance().getSoundManager().stop(this.currentSound);
                    this.currentSound = null;
                }
                else
                    Minecraft.getInstance().getSoundManager().play(this.currentSound);
            }
        }) {
            @Override
            public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                if(this.active && this.visible) {
                    if (this.isValidClickButton(pButton) && this.clicked(pMouseX, pMouseY)) {
                        this.onClick(pMouseX, pMouseY, pButton);
                        return true;
                    }
                }
                return false;
            }
        });
        this.playButton.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.play")));
    }

    public void setValue(String value) {
        this.editBox.setValue(value);
        this.editBox.hideSuggestions();
    }

    public String getValue() {
        return this.editBox.getValue();
    }

    public void setResponder(Consumer<String> responder) {
        this.editBox.setResponder(responder);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation soundLoc = ResourceLocation.tryParse(this.editBox.getValue());
        if(soundLoc != null && Minecraft.getInstance().getSoundManager().getAvailableSounds().contains(soundLoc))
            this.currentSound = SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(soundLoc), 1f);
        else
            this.currentSound = null;
        this.playButton.active = this.currentSound != null;
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}
