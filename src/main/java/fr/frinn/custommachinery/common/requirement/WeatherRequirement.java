package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.component.WeatherMachineComponent.WeatherType;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public record WeatherRequirement(WeatherType weather, boolean onMachine) implements IRequirement<WeatherMachineComponent> {

    public static final NamedCodec<WeatherRequirement> CODEC = NamedCodec.record(weatherRequirementInstance ->
            weatherRequirementInstance.group(
                    WeatherType.CODEC.fieldOf("weather").forGetter(requirement -> requirement.weather),
                    NamedCodec.BOOL.optionalFieldOf("onmachine", true).forGetter(requirement -> requirement.onMachine)
            ).apply(weatherRequirementInstance, WeatherRequirement::new), "Weather requirement"
    );

    @Override
    public RequirementType<WeatherRequirement> getType() {
        return Registration.WEATHER_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<WeatherMachineComponent> getComponentType() {
        return Registration.WEATHER_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(WeatherMachineComponent component, ICraftingContext context) {
        return component.hasWeather(this.weather, this.onMachine);
    }

    @Override
    public void gatherRequirements(IRequirementList<WeatherMachineComponent> list) {
        list.worldCondition(this::check);
    }

    public CraftingResult check(WeatherMachineComponent component, ICraftingContext context) {
        if(component.hasWeather(this.weather, this.onMachine))
            return CraftingResult.success();
        return CraftingResult.error(Component.translatable("custommachinery.requirements.weather.error", this.weather));
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        info.addTooltip(Component.translatable("custommachinery.requirements.weather.info", this.weather.getText()));
        if(this.onMachine)
            info.addTooltip(Component.translatable("custommachinery.requirements.weather.info.sky"));
        info.setItemIcon(Items.SUNFLOWER);
    }
}
