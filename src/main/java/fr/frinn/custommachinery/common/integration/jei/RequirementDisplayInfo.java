package fr.frinn.custommachinery.common.integration.jei;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.machine.ICustomMachine;
import fr.frinn.custommachinery.common.data.CustomMachine;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RequirementDisplayInfo implements IDisplayInfo {

    private ResourceLocation icon = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/create_icon.png");
    private int width = 10;
    private int height = 10;
    private int u = 0;
    private int v = 0;
    private final List<ITextComponent> tooltips = new ArrayList<>();
    private BiConsumer<ICustomMachine, Integer> clickAction;
    private boolean visible = true;

    @Override
    public RequirementDisplayInfo setIcon(ResourceLocation icon, int width, int height, int u, int v) {
        this.icon = icon;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public RequirementDisplayInfo addTooltip(ITextComponent tooltip) {
        this.tooltips.add(tooltip);
        return this;
    }

    public ResourceLocation getIcon() {
        return this.icon;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getU() {
        return this.u;
    }

    public int getV() {
        return this.v;
    }

    public List<ITextComponent> getTooltips() {
        return this.tooltips;
    }

    @Override
    public void setClickAction(BiConsumer<ICustomMachine, Integer> clickAction) {
        this.clickAction = clickAction;
    }

    public boolean hasClickAction() {
        return this.clickAction != null;
    }

    public boolean handleClick(CustomMachine machine, int mouseButton) {
        if(hasClickAction()) {
            this.clickAction.accept(machine, mouseButton);
            return true;
        }
        return false;
    }

    @Override
    public RequirementDisplayInfo setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
