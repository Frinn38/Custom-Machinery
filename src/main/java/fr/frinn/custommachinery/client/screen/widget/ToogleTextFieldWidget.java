package fr.frinn.custommachinery.client.screen.widget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Predicate;

public class ToogleTextFieldWidget extends TextFieldWidget {

    private Predicate<ToogleTextFieldWidget> toogle;

    public ToogleTextFieldWidget(FontRenderer font, int x, int y, int width, int height, ITextComponent title, Predicate<ToogleTextFieldWidget> toogle) {
        super(font, x, y, width, height, title);
        this.toogle = toogle;
    }

    @Override
    public boolean getVisible() {
        return this.toogle.test(this);
    }
}
