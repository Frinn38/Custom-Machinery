package fr.frinn.custommachinery.common.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.codec.CodecLogger;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.component.LightMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class LightRequirement extends AbstractRequirement<LightMachineComponent> implements ITickableRequirement<LightMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<LightRequirement> CODEC = RecordCodecBuilder.create(lightRequirementInstance ->
            lightRequirementInstance.group(
                    Codec.INT.fieldOf("light").forGetter(requirement -> requirement.light),
                    CodecLogger.loggedOptional(Codecs.COMPARATOR_MODE_CODEC,"comparator", ComparatorMode.GREATER_OR_EQUALS).forGetter(requirement -> requirement.comparator),
                    CodecLogger.loggedOptional(Codec.BOOL,"sky", false).forGetter(requirement -> requirement.sky)
            ).apply(lightRequirementInstance, LightRequirement::new)
    );

    private final int light;
    private final ComparatorMode comparator;
    private final boolean sky;

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
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.light.sky.error", this.comparator.getPrefix(), light));
        else
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.light.block.error", this.comparator.getPrefix(), light));
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
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.light.sky.error", this.comparator.getPrefix(), light));
        else
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.light.block.error", this.comparator.getPrefix(), light));
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        if(this.sky)
            info.addTooltip(new TranslatableComponent("custommachinery.requirements.light.sky.info", new TranslatableComponent(this.comparator.getTranslationKey()), this.light));
        else
            info.addTooltip(new TranslatableComponent("custommachinery.requirements.light.block.info", new TranslatableComponent(this.comparator.getTranslationKey()), this.light));
        info.setItemIcon(Items.TORCH);
    }
}
