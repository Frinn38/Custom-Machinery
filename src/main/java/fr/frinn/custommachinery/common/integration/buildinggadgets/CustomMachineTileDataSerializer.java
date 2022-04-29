package fr.frinn.custommachinery.common.integration.buildinggadgets;

import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileEntityData;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CustomMachineTileDataSerializer implements ITileDataSerializer {

    private static final String MACHINE_ID = "machineID";

    private ResourceLocation registryName;

    @Override
    public CompoundNBT serialize(ITileEntityData data, boolean b) {
        if(!(data instanceof CustomMachineTileData))
            throw new IllegalArgumentException("Custom Machinery can't serialize this type of data : " + data.getClass().getName());
        CustomMachineTileData machineData = (CustomMachineTileData)data;
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(MACHINE_ID, machineData.getMachineID().toString());
        return nbt;
    }

    @Override
    public ITileEntityData deserialize(CompoundNBT nbt, boolean b) {
        if(!nbt.contains(MACHINE_ID, Constants.NBT.TAG_STRING))
            throw new IllegalArgumentException("Invalid nbt received by custom machinery data serializer : " + nbt);
        return new CustomMachineTileData(new ResourceLocation(nbt.getString(MACHINE_ID)));
    }

    @Override
    public ITileDataSerializer setRegistryName(ResourceLocation name) {
        this.registryName = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    @Override
    public Class<ITileDataSerializer> getRegistryType() {
        return ITileDataSerializer.class;
    }
}
