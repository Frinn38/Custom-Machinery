package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.LightMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import net.minecraft.item.Items;
import net.minecraft.util.text.TranslationTextComponent;

public class LightRequirement extends AbstractRequirement<LightMachineComponent> implements ITickableRequirement<LightMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<LightRequirement> CODEC = RecordCodecBuilder.create(lightRequirementInstance ->
            lightRequirementInstance.group(
                    Codec.INT.fieldOf("light").forGetter(requirement -> requirement.light),
                    CodecLogger.loggedOptional(Codecs.COMPARATOR_MODE_CODEC,"comparator", ComparatorMode.GREATER_OR_EQUALS).forGetter(requirement -> requirement.comparator),
                    CodecLogger.loggedOptional(Codec.BOOL,"sky", false).forGetter(requirement -> requirement.sky),
                    CodecLogger.loggedOptional(Codec.BOOL,"jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(lightRequirementInstance, (light, comparator, sky, jei) -> {
                    LightRequirement requirement = new LightRequirement(light, comparator, sky);
                    requirement.setJeiVisible(jei);
                    return requirement;
            })
    );

    private final int light;
    private final ComparatorMode comparator;
    private final boolean sky;
    private boolean jeiVisible = true;

    public LightRequirement(int light, ComparatorMode comparator, boolean sky) {
        super(RequirementIOMode.INPUT);
        this.light = light;
        this.comparator = comparator;
        this.sky = sky;
    }

    @Override
    public RequirementType<?> getType() {
        return Registration.LIGHT_REQUIREMENT.get();
    }

    @Override
    public boolean test(LightMachineComponent component, ICraftingContext context) {
        int light = (int)context.getModifiedValue(this.light, this, null);
        if(this.sky)
            return this.comparator.compare(component.getSkyLight(), light);
        return this.comparator.compare(component.getBlockLight(), light);
    }

    @Override
    public CraftingResult processStart(LightMachineComponent component, ICraftingContext context) {
        int light = (int)context.getModifiedValue(this.light, this, null);
        if(this.test(component, context))
            return CraftingResult.success();
        if(this.sky)
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.light.sky.error", this.comparator.getPrefix(), light));
        else
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.light.block.error", this.comparator.getPrefix(), light));
    }

    @Override
    public CraftingResult processEnd(LightMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<LightMachineComponent> getComponentType() {
        return Registration.LIGHT_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(LightMachineComponent component, ICraftingContext context) {
        int light = (int)context.getModifiedValue(this.light, this, null);
        if(this.test(component, context))
            return CraftingResult.success();
        if(this.sky)
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.light.sky.error", this.comparator.getPrefix(), light));
        else
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.light.block.error", this.comparator.getPrefix(), light));
    }

    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.setVisible(this.jeiVisible);
        if(this.sky)
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.light.sky.info", new TranslationTextComponent(this.comparator.getTranslationKey()), this.light));
        else
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.light.block.info", new TranslationTextComponent(this.comparator.getTranslationKey()), this.light));
        info.setItemIcon(Items.TORCH);
    }
}