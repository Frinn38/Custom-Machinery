package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import dev.latvian.mods.kubejs.script.ScriptType;
import fr.frinn.custommachinery.common.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.requirement.WeatherRequirement;

public interface WeatherRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder requireWeather(String weather) {
        try {
            return this.addRequirement(new WeatherRequirement(WeatherMachineComponent.WeatherType.value(weather), false));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn("Invalid weather type: " + weather);
        }
        return this;
    }

    default RecipeJSBuilder requireWeatherOnMachine(String weather) {
        try {
            return this.addRequirement(new WeatherRequirement(WeatherMachineComponent.WeatherType.value(weather), true));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn("Invalid weather type: " + weather);
        }
        return this;
    }
}
