package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

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
        Level world = this.getManager().getLevel();
        BlockPos pos = this.getManager().getTile().getBlockPos();
        if(onTile) {
            if(weather == WeatherType.RAIN)
                return world.isRainingAt(pos.above());
            else if(weather == WeatherType.SNOW)
                return world.isRaining() && world.canSeeSky(pos.above()) && world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.above()).getY() > pos.above().getY() && world.getBiome(pos).value().getPrecipitation() == Biome.Precipitation.SNOW;
            else if(weather == WeatherType.THUNDER)
                return world.isRainingAt(pos.above()) && world.isThundering();
            else if(weather == WeatherType.CLEAR)
                return !world.isRaining();
        } else {
            if(weather == WeatherType.RAIN)
                return world.isRaining();
            else if(weather == WeatherType.SNOW)
                return world.isRaining() && world.canSeeSky(pos.above()) && world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.above()).getY() > pos.above().getY() && world.getBiome(pos).value().getPrecipitation() == Biome.Precipitation.SNOW;
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

        public Component getText() {
            return new TranslatableComponent("custommachinery.component.weather." + this.toString().toLowerCase(Locale.ENGLISH));
        }
    }
}
