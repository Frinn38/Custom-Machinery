package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import fr.frinn.custommachinery.impl.component.config.IOSideMode;
import fr.frinn.custommachinery.impl.network.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class IOSideConfigData extends Data<IOSideConfig> {

    public IOSideConfigData(Short id, IOSideConfig value) {
        super(Registration.IO_SIDE_CONFIG_DATA.get(), id, value);
    }

    public static IOSideConfigData readData(short id, RegistryFriendlyByteBuf buffer) {
        Map<RelativeSide, IOSideMode> map = new HashMap<>();
        for(RelativeSide side : RelativeSide.values())
            map.put(side, IOSideMode.values()[buffer.readByte()]);
        return new IOSideConfigData(id, new IOSideConfig(null, map, buffer.readBoolean(), buffer.readBoolean(), true));
    }

    @Override
    public void writeData(RegistryFriendlyByteBuf buffer) {
        super.writeData(buffer);
        for(RelativeSide side : RelativeSide.values())
            buffer.writeByte(getValue().getSideMode(side).ordinal());
        buffer.writeBoolean(getValue().isAutoInput());
        buffer.writeBoolean(getValue().isAutoOutput());
    }
}
