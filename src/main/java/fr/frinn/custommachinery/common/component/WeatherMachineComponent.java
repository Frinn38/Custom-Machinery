package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.CMRegistration;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Locale;

public class WeatherMachineComponent extends AbstractMachineComponent {

    public WeatherMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<WeatherMachineComponent> getType() {
        return CMRegistration.WEATHER_MACHINE_COMPONENT.get();
    }

    public boolean hasWeather(WeatherType weather, boolean onTile) {
        Level level = this.getManager().getLevel();
        BlockPos pos = this.getManager().getTile().getBlockPos();
        if(onTile) {
            if(weather == WeatherType.RAIN)
                return level.isRainingAt(pos.above());
            else if(weather == WeatherType.SNOW)
                return level.isRaining() && level.canSeeSky(pos.above()) && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.above()).getY() > pos.above().getY() && level.getBiome(pos).value().coldEnoughToSnow(pos.above(), level.getSeaLevel());
            else if(weather == WeatherType.THUNDER)
                return level.isRainingAt(pos.above()) && level.isThundering();
            else if(weather == WeatherType.CLEAR)
                return !level.isRaining();
        } else {
            if(weather == WeatherType.RAIN)
                return level.isRaining();
            else if(weather == WeatherType.SNOW)
                return level.isRaining() && level.canSeeSky(pos.above()) && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos.above()).getY() > pos.above().getY() && level.getBiome(pos).value().coldEnoughToSnow(pos.above(), level.getSeaLevel());
            else if(weather == WeatherType.THUNDER)
                return level.isThundering();
            else if(weather == WeatherType.CLEAR)
                return !level.isRaining();
        }
        return false;
    }

    public enum WeatherType {
        CLEAR,
        RAIN,
        SNOW,
        THUNDER;

        public static final NamedCodec<WeatherType> CODEC = NamedCodec.enumCodec(WeatherType.class);

        public static WeatherType value(String value) {
            return valueOf(value.toUpperCase(Locale.ENGLISH));
        }

        public Component getText() {
            return Component.translatable("custommachinery.component.weather." + this.toString().toLowerCase(Locale.ENGLISH));
        }
    }
}
