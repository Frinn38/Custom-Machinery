package fr.frinn.custommachinery.common.integration.jei;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RequirementDisplayInfo {

    private ResourceLocation icon = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/create_icon.png");
    private int u;
    private int v;
    private List<ITextComponent> tooltips = new ArrayList<>();
    private BiConsumer<CustomMachine, Integer> clickAction;
    private boolean visible = true;

    public RequirementDisplayInfo setIcon(ResourceLocation icon, int u, int v) {
        this.icon = icon;
        this.u = u;
        this.v = v;
        return this;
    }

    public RequirementDisplayInfo setIcon(ResourceLocation icon) {
        return this.setIcon(icon, 0, 0);
    }

    public RequirementDisplayInfo addTooltip(ITextComponent tooltip) {
        this.tooltips.add(tooltip);
        return this;
    }

    public ResourceLocation getIcon() {
        return this.icon;
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

    public void setClickAction(BiConsumer<CustomMachine, Integer> clickAction) {
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

    public RequirementDisplayInfo setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
