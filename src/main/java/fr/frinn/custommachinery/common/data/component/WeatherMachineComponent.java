package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;

import java.util.Locale;

public class WeatherMachineComponent extends AbstractMachineComponent {

    public WeatherMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<WeatherMachineComponent> getType() {
        return Registration.WEATHER_MACHINE_COMPONENT.get();
    }

    public boolean hasWeather(WeatherType weather, boolean onTile) {
        World world = this.getManager().getWorld();
        BlockPos pos = this.getManager().getTile().getPos();
        if(onTile) {
            if(weather == WeatherType.RAIN)
                return world.isRainingAt(pos.up());
            else if(weather == WeatherType.SNOW)
                return world.isRaining() && world.canSeeSky(pos.up()) && world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos.up()).getY() > pos.up().getY() && world.getBiome(pos).getPrecipitation() == Biome.RainType.SNOW;
            else if(weather == WeatherType.THUNDER)
                return world.isRainingAt(pos.up()) && world.isThundering();
            else if(weather == WeatherType.CLEAR)
                return !world.isRaining();
        } else {
            if(weather == WeatherType.RAIN)
                return world.isRaining();
            else if(weather == WeatherType.SNOW)
                return world.isRaining() && world.getBiome(pos).getPrecipitation() == Biome.RainType.SNOW && world.getBiome(pos).getTemperature(pos) < 0.15;
            else if(weather == WeatherType.THUNDER)
                return world.isThundering();
            else if(weather == WeatherType.CLEAR)
                return !world.isRaining();
        }
        return false;
    }

    public enum WeatherType {
        CLEAR,
        RAIN,
        SNOW,
        THUNDER;

        public static WeatherType value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }

        public ITextComponent getText() {
            return new TranslationTextComponent("custommachinery.component.weather." + this.toString().toLowerCase(Locale.ENGLISH));
        }
    }
}
