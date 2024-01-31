package fr.frinn.custommachinery.common.machine;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.api.crafting.IProcessorTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.common.upgrade.RecipeModifier;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class UpgradedCustomMachine extends CustomMachine {

    public static NamedCodec<UpgradedCustomMachine> makeCodec(CustomMachine parent) {
        return NamedCodec.record(upgradedMachineCodec ->
                        upgradedMachineCodec.group(
                                TextComponentUtils.CODEC.optionalFieldOf("name", Component.literal("Upgraded ").setStyle(parent.getName().getStyle()).append(parent.getName())).forGetter(CustomMachine::getName),
                                MachineAppearanceManager.CODEC.optionalFieldOf("appearance", parent.getAppearanceManager()).forGetter(CustomMachine::getAppearanceManager),
                                TextComponentUtils.CODEC.listOf().optionalFieldOf("tooltips", parent.getTooltips()).forGetter(CustomMachine::getTooltips),
                                IGuiElement.CODEC.listOf().optionalFieldOf("gui", parent.getGuiElements()).forGetter(CustomMachine::getGuiElements),
                                IGuiElement.CODEC.listOf().optionalFieldOf("jei", parent.getJeiElements()).forGetter(CustomMachine::getJeiElements),
                                DefaultCodecs.RESOURCE_LOCATION.listOf().optionalFieldOf("catalysts", Collections.emptyList()).forGetter(CustomMachine::getCatalysts),
                                IMachineComponentTemplate.CODEC.listOf().optionalFieldOf("components", parent.getComponentTemplates()).forGetter(CustomMachine::getComponentTemplates),
                                IProcessorTemplate.CODEC.optionalFieldOf("processor", parent.getProcessorTemplate()).forGetter(CustomMachine::getProcessorTemplate),
                                RecipeModifier.CODEC.listOf().optionalFieldOf("modifiers", Collections.emptyList()).forGetter(UpgradedCustomMachine::getModifiers)
                        ).apply(upgradedMachineCodec, (name, appearance, tooltips, gui, jei, catalysts, components, processor, modifiers) ->
                                new UpgradedCustomMachine(name, appearance, tooltips, gui, jei, catalysts, components, processor, modifiers, parent.getId())),
                "Custom machine"
        );
    }

    private final List<RecipeModifier> modifiers;
    private final ResourceLocation parentId;

    public UpgradedCustomMachine(Component name, MachineAppearanceManager appearance, List<Component> tooltips, List<IGuiElement> guiElements, List<IGuiElement> jeiElements, List<ResourceLocation> catalysts, List<IMachineComponentTemplate<? extends IMachineComponent>> componentTemplates, IProcessorTemplate<? extends IProcessor> processorTemplate, List<RecipeModifier> modifiers, ResourceLocation parentId) {
        super(name, appearance, tooltips, guiElements, jeiElements, catalysts, componentTemplates, processorTemplate);
        this.modifiers = modifiers;
        this.parentId = parentId;
    }

    public List<RecipeModifier> getModifiers() {
        return this.modifiers;
    }

    public ResourceLocation getParentId() {
        return this.parentId;
    }

    @Override
    public List<ResourceLocation> getRecipeIds() {
        return Lists.asList(this.getId(), CustomMachinery.MACHINES.get(this.parentId).getRecipeIds().toArray(new ResourceLocation[]{}));
    }
}
