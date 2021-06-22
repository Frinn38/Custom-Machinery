package fr.frinn.custommachinery.common.integration.jei;

import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class RequirementDisplayInfo {

    private ResourceLocation icon = new ResourceLocation(CustomMachinery.MODID, "textures/gui/creation/create_icon.png");
    private int u;
    private int v;
    private List<ITextComponent> tooltips = new ArrayList<>();

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
}
