package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

public class ToogleTextFieldWidget extends EditBox {

    private Predicate<ToogleTextFieldWidget> toogle;

    public ToogleTextFieldWidget(Font font, int x, int y, int width, int height, Component title, Predicate<ToogleTextFieldWidget> toogle) {
        super(font, x, y, width, height, title);
        this.toogle = toogle;
    }

    @Override
    public boolean isVisible() {
        return this.toogle.test(this);
    }
}
