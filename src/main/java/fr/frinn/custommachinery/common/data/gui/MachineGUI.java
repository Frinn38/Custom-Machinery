package fr.frinn.custommachinery.common.data.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MachineGUI {

    private List<IGuiElement> elements = new ArrayList<>();

    public void addElement(IGuiElement element) {
        this.elements.add(element);
    }

    public List<IGuiElement> getElements() {
        return this.elements;
    }

    public <E extends IGuiElement> List<E> getElements(GuiElementType<E> type) {
        return this.elements.stream().filter(element -> element.getType() == type).map(element -> (E)element).collect(Collectors.toList());
    }

    public boolean hasElement(GuiElementType type) {
        return this.elements.stream().anyMatch(element -> element.getType() == type);
    }
}
