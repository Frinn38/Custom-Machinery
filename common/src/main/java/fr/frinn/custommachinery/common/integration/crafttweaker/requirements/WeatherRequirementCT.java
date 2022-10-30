package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.common.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.common.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.WeatherRequirement;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

@ZenRegister
@Name(CTConstants.REQUIREMENT_WEATHER)
public interface WeatherRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T requireWeather(String weatherType) {
        return addRequirement(new WeatherRequirement(WeatherMachineComponent.WeatherType.value(weatherType), false));
    }

    @Method
    default T requireWeatherOnMachine(String weatherType) {
        return addRequirement(new WeatherRequirement(WeatherMachineComponent.WeatherType.value(weatherType), true));
    }
}
