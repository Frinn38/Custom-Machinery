package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.LightMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import net.minecraft.util.text.TranslationTextComponent;

public class LightRequirement extends AbstractTickableRequirement<LightMachineComponent> implements IDisplayInfoRequirement<LightMachineComponent> {

    public static final Codec<LightRequirement> CODEC = RecordCodecBuilder.create(lightRequirementInstance ->
            lightRequirementInstance.group(
                    Codec.INT.fieldOf("light").forGetter(requirement -> requirement.light),
                    Codecs.COMPARATOR_MODE_CODEC.optionalFieldOf("comparator", ComparatorMode.GREATER_OR_EQUALS).forGetter(requirement -> requirement.comparator),
                    Codec.BOOL.optionalFieldOf("sky", false).forGetter(requirement -> requirement.sky)
            ).apply(lightRequirementInstance, LightRequirement::new)
    );

    private int light;
    private ComparatorMode comparator;
    private boolean sky;

    public LightRequirement(int light, ComparatorMode comparator, boolean sky) {
        super(MODE.INPUT);
        this.light = light;
        this.comparator = comparator;
        this.sky = sky;
    }

    @Override
    public RequirementType<?> getType() {
        return Registration.LIGHT_REQUIREMENT.get();
    }

    @Override
    public boolean test(LightMachineComponent component, CraftingContext context) {
        int light = (int)context.getModifiedvalue(this.light, this, null);
        if(this.sky)
            return this.comparator.compare(component.getSkyLight(), light);
        return this.comparator.compare(component.getBlockLight(), light);
    }

    @Override
    public CraftingResult processStart(LightMachineComponent component, CraftingContext context) {
        int light = (int)context.getModifiedvalue(this.light, this, null);
        if(this.test(component, context))
            return CraftingResult.success();
        if(this.sky)
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.light.sky.error", this.comparator.getPrefix(), light));
        else
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.light.block.error", this.comparator.getPrefix(), light));
    }

    @Override
    public CraftingResult processEnd(LightMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<LightMachineComponent> getComponentType() {
        return Registration.LIGHT_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(LightMachineComponent component, CraftingContext context) {
        int light = (int)context.getModifiedvalue(this.light, this, null);
        if(this.test(component, context))
            return CraftingResult.success();
        if(this.sky)
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.light.sky.error", this.comparator.getPrefix(), light));
        else
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.light.block.error", this.comparator.getPrefix(), light));
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        RequirementDisplayInfo info = new RequirementDisplayInfo();
        if(this.sky)
            return info.addTooltip(new TranslationTextComponent("custommachinery.requirements.light.sky.info", new TranslationTextComponent(this.comparator.getTranslationKey()), this.light));
        else
            return info.addTooltip(new TranslationTextComponent("custommachinery.requirements.light.block.info", new TranslationTextComponent(this.comparator.getTranslationKey()), this.light));
    }
}
