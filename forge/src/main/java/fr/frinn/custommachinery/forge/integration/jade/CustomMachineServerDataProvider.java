package fr.frinn.custommachinery.forge.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class CustomMachineServerDataProvider implements IServerDataProvider<BlockAccessor> {

    public static final CustomMachineServerDataProvider INSTANCE = new CustomMachineServerDataProvider();
    public static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "machine_server_data_provider");

    @Override
    public void appendServerData(CompoundTag nbt, BlockAccessor accessor) {
        if(accessor.getBlockEntity() instanceof CustomMachineTile machine) {
            IProcessor processor = machine.getProcessor();
            CompoundTag tag = new CompoundTag();
            if(machine.getOwnerName() != null)
                tag.putString("owner", TextComponentUtils.toJsonString(machine.getOwnerName()));
            tag.putByte("status", (byte)machine.getStatus().ordinal());
            if(processor instanceof MachineProcessor machineProcessor && processor.getCurrentContext() != null) {
                tag.putDouble("recipeProgressTime", machineProcessor.getRecipeProgressTime());
                tag.putDouble("recipeTotalTime", machineProcessor.getRecipeTotalTime());
                tag.putString("errorMessage", Component.Serializer.toJson(machine.getMessage()));
            }
            nbt.put(CustomMachinery.MODID, tag);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
