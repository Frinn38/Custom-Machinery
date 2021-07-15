package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.text.TranslationTextComponent;

public class WeatherRequirement extends AbstractTickableRequirement<WeatherMachineComponent> implements IDisplayInfoRequirement<WeatherMachineComponent> {

    public static final Codec<WeatherRequirement> CODEC = RecordCodecBuilder.create(weatherRequirementInstance ->
            weatherRequirementInstance.group(
                    Codecs.WEATHER_TYPE_CODEC.fieldOf("weather").forGetter(requirement -> requirement.weather),
                    Codec.BOOL.optionalFieldOf("onmachine", true).forGetter(requirement -> requirement.onMachine)
            ).apply(weatherRequirementInstance, WeatherRequirement::new)
    );

    private WeatherMachineComponent.WeatherType weather;
    private boolean onMachine;

    public WeatherRequirement(WeatherMachineComponent.WeatherType weather, boolean onMachine) {
        super(MODE.INPUT);
        this.weather = weather;
        this.onMachine = onMachine;
    }

    @Override
    public RequirementType<WeatherRequirement> getType() {
        return Registration.WEATHER_REQUIREMENT.get();
    }

    @Override
    public boolean test(WeatherMachineComponent component, CraftingContext context) {
        return component.hasWeather(this.weather, this.onMachine);
    }

    @Override
    public CraftingResult processStart(WeatherMachineComponent component, CraftingContext context) {
        if(component.hasWeather(this.weather, this.onMachine))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.weather.error", this.weather));
    }

    @Override
    public CraftingResult processEnd(WeatherMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<WeatherMachineComponent> getComponentType() {
        return Registration.WEATHER_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(WeatherMachineComponent component, CraftingContext context) {
        if(component.hasWeather(this.weather, this.onMachine))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.weather.error", this.weather));
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        RequirementDisplayInfo info = new RequirementDisplayInfo();
        info.addTooltip(new TranslationTextComponent("custommachinery.requirements.weather.info", this.weather.getText()));
        if(this.onMachine)
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.weather.info.sky"));
        return info;
    }
}
