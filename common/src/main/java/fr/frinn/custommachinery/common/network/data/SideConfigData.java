package fr.frinn.custommachinery.common.network.data;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideConfig;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class SideConfigData extends Data<SideConfig> {

    public SideConfigData(Short id, SideConfig value) {
        super(Registration.SIDE_CONFIG_DATA.get(), id, value);
    }

    public static SideConfigData readData(short id, FriendlyByteBuf buffer) {
        Map<RelativeSide, SideMode> map = new HashMap<>();
        for(RelativeSide side : RelativeSide.values())
            map.put(side, SideMode.values()[buffer.readByte()]);
        return new SideConfigData(id, new SideConfig(null, map, buffer.readBoolean(), buffer.readBoolean(), true));
    }

    @Override
    public void writeData(FriendlyByteBuf buffer) {
        super.writeData(buffer);
        for(RelativeSide side : RelativeSide.values())
            buffer.writeByte(getValue().getSideMode(side).ordinal());
        buffer.writeBoolean(getValue().isAutoInput());
        buffer.writeBoolean(getValue().isAutoOutput());
    }
}
