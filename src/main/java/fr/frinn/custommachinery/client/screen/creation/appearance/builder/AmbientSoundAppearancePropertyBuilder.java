package fr.frinn.custommachinery.client.screen.creation.appearance.builder;

import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.client.screen.BaseScreen;
import fr.frinn.custommachinery.client.screen.creation.appearance.IAppearancePropertyBuilder;
import fr.frinn.custommachinery.client.screen.widget.SoundEditBox;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AmbientSoundAppearancePropertyBuilder implements IAppearancePropertyBuilder<SoundEvent> {

    @Override
    public Component title() {
        return Component.translatable("custommachinery.gui.creation.appearance.ambient_sound");
    }

    @Override
    public MachineAppearanceProperty<SoundEvent> type() {
        return Registration.AMBIENT_SOUND_PROPERTY.get();
    }

    @Override
    public AbstractWidget makeWidget(BaseScreen parent, int x, int y, int width, int height, Supplier<SoundEvent> supplier, Consumer<SoundEvent> consumer) {
        SoundEditBox editBox = new SoundEditBox(x, y, width, height, title());
        if(!supplier.get().getLocation().getPath().isEmpty())
            editBox.setValue(supplier.get().getLocation().toString());
        editBox.setResponder(s -> {
            ResourceLocation soundLoc = ResourceLocation.tryParse(s);
            if(s.isEmpty())
                consumer.accept(SoundEvent.createVariableRangeEvent(ResourceLocation.withDefaultNamespace("")));
            else if(soundLoc != null && Minecraft.getInstance().getSoundManager().getAvailableSounds().contains(soundLoc))
                consumer.accept(SoundEvent.createVariableRangeEvent(soundLoc));
        });
        return editBox;
    }
}
