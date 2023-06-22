package fr.frinn.custommachinery.forge.integration.buildinggadgets;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public class CustomMachineTileDataSerializer implements ITileDataSerializer {

    private static final String MACHINE_ID = "machineID";

    @Override
    public CompoundTag serialize(ITileEntityData data, boolean b) {
        if(!(data instanceof CustomMachineTileData machineData))
            throw new IllegalArgumentException("Custom Machinery can't serialize this type of data : " + data.getClass().getName());
        CompoundTag nbt = new CompoundTag();
        nbt.putString(MACHINE_ID, machineData.getMachineID().toString());
        return nbt;
    }

    @Override
    public ITileEntityData deserialize(CompoundTag nbt, boolean b) {
        if(!nbt.contains(MACHINE_ID, Tag.TAG_STRING))
            throw new IllegalArgumentException("Invalid nbt received by custom machinery data serializer : " + nbt);
        return new CustomMachineTileData(new ResourceLocation(nbt.getString(MACHINE_ID)));
    }
}
