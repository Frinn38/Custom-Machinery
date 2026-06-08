package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Color;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig.ConfigGuiData;
import fr.frinn.custommachinery.impl.component.config.ToggleSideConfig;
import fr.frinn.custommachinery.impl.component.config.ToggleSideMode;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class ToggleSideConfigData extends Data<ToggleSideConfig> {

    public ToggleSideConfigData(Short id, ToggleSideConfig value) {
        super(Registration.TOGGLE_SIDE_CONFIG_DATA.get(), id, value);
    }

    public static ToggleSideConfigData readData(short id, RegistryFriendlyByteBuf buffer) {
        Map<RelativeSide, ToggleSideMode> map = new HashMap<>();
        for(RelativeSide side : RelativeSide.values())
            map.put(side, buffer.readBoolean() ? ToggleSideMode.ENABLED : ToggleSideMode.DISABLED);
        return new ToggleSideConfigData(id, new ToggleSideConfig(null, map, true, Color.fromARGB(buffer.readVarInt()), ConfigGuiData.CODEC.fromNetwork(buffer)));
    }

    @Override
    public void writeData(RegistryFriendlyByteBuf buffer) {
        super.writeData(buffer);
        for(RelativeSide side : RelativeSide.values())
            buffer.writeBoolean(getValue().getSideMode(side) == ToggleSideMode.ENABLED);
        buffer.writeVarInt(getValue().getColor().getARGB());
        ConfigGuiData.CODEC.toNetwork(getValue().getGuiData(), buffer);
    }
}
