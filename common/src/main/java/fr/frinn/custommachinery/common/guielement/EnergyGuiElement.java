package fr.frinn.custommachinery.common.guielement;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.guielement.GuiElementType;
import fr.frinn.custommachinery.api.guielement.IComponentGuiElement;
import fr.frinn.custommachinery.common.component.EnergyMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.guielement.AbstractTexturedGuiElement;
import net.minecraft.resources.ResourceLocation;

public class EnergyGuiElement extends AbstractTexturedGuiElement implements IComponentGuiElement<EnergyMachineComponent> {

    private static final ResourceLocation BASE_ENERGY_STORAGE_EMPTY_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_energy_storage_empty.png");
    private static final ResourceLocation BASE_ENERGY_STORAGE_FILLED_TEXTURE = new ResourceLocation(CustomMachinery.MODID, "textures/gui/base_energy_storage_filled.png");

    public static final NamedCodec<EnergyGuiElement> CODEC = NamedCodec.record(energyGuiElement ->
            makeBaseCodec(energyGuiElement).and(
                    energyGuiElement.group(
                            DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("emptytexture", BASE_ENERGY_STORAGE_EMPTY_TEXTURE).forGetter(EnergyGuiElement::getEmptyTexture),
                            DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("filledtexture", BASE_ENERGY_STORAGE_FILLED_TEXTURE).forGetter(EnergyGuiElement::getFilledTexture),
                            NamedCodec.BOOL.optionalFieldOf("highlight", true).forGetter(EnergyGuiElement::highlight)
                    )
            ).apply(energyGuiElement, EnergyGuiElement::new),
            "Energy gui element"
    );

    private final ResourceLocation emptyTexture;
    private final ResourceLocation filledTexture;
    private final boolean highlight;

    public EnergyGuiElement(int x, int y, int width, int height, int priority, ResourceLocation emptyTexture, ResourceLocation filledTexture, boolean highlight) {
        super(x, y, width, height, priority, emptyTexture);
        this.emptyTexture = emptyTexture;
        this.filledTexture = filledTexture;
        this.highlight = highlight;
    }

    public ResourceLocation getEmptyTexture() {
        return this.emptyTexture;
    }

    public ResourceLocation getFilledTexture() {
        return this.filledTexture;
    }

    public boolean highlight() {
        return this.highlight;
    }

    @Override
    public GuiElementType<EnergyGuiElement> getType() {
        return Registration.ENERGY_GUI_ELEMENT.get();
    }

    @Override
    public MachineComponentType<EnergyMachineComponent> getComponentType() {
        return Registration.ENERGY_MACHINE_COMPONENT.get();
    }

    @Override
    public String getID() {
        return "";
    }
}
