package fr.frinn.custommachinery.client.screen.widget;

import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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
        this.editBox.addSuggestions(this.mc.getSoundManager().getAvailableSounds().stream().map(Identifier::toString).toList());
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
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                if(this.active && this.visible) {
                    if (this.isValidClickButton(event.buttonInfo())) {
                        this.onClick(event, doubleClick);
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
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Identifier soundLoc = Identifier.tryParse(this.editBox.getValue());
        if(soundLoc != null && Minecraft.getInstance().getSoundManager().getAvailableSounds().contains(soundLoc))
            this.currentSound = SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(soundLoc), 1f);
        else
            this.currentSound = null;
        this.playButton.active = this.currentSound != null;
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
    }
}
