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

    public SoundEditBox(int x, int y, int width, int height, Component message, Supplier<SoundEvent> supplier, Consumer<SoundEvent> consumer) {
        super(x, y, width, height, message);
        this.editBox = this.addWidget(new SuggestedEditBox(this.font, x, y, width - 20, height, message, 5));
        this.editBox.setAnchorToBottom();
        this.editBox.setMaxLength(Integer.MAX_VALUE);
        if(!supplier.get().getLocation().getPath().isEmpty())
            this.editBox.setValue(supplier.get().getLocation().toString());
        this.editBox.addSuggestions(this.mc.getSoundManager().getAvailableSounds().stream().map(ResourceLocation::toString).toList());
        this.editBox.setResponder(s -> {
            ResourceLocation soundLoc = ResourceLocation.tryParse(s);
            if(s.isEmpty())
                consumer.accept(SoundEvent.createVariableRangeEvent(ResourceLocation.withDefaultNamespace("")));
            else if(soundLoc == null || !this.mc.getSoundManager().getAvailableSounds().contains(soundLoc))
                this.currentSound = null;
            else {
                SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(ResourceLocation.parse(this.editBox.getValue()));
                this.currentSound = SimpleSoundInstance.forUI(soundEvent, 1F);
                consumer.accept(soundEvent);
            }
        });
        WidgetSprites sprites = new WidgetSprites(CustomMachinery.rl("creation/play_button"), CustomMachinery.rl("creation/play_button_disabled"), CustomMachinery.rl("creation/play_button_hovered"));
        this.playButton = this.addWidget(new ImageButton(x + width - 20, y, 20, 20, sprites, button -> {
            if(this.currentSound != null) {
                if(Minecraft.getInstance().getSoundManager().isActive(this.currentSound))
                    Minecraft.getInstance().getSoundManager().stop(this.currentSound);
                else
                    Minecraft.getInstance().getSoundManager().play(this.currentSound);
            }
        }));
        this.playButton.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.appearance.ambient_sound.play")));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.playButton.active = this.currentSound != null;
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }
}
