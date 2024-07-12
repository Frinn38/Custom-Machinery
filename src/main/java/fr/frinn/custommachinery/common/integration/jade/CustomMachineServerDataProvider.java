package fr.frinn.custommachinery.common.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class CustomMachineServerDataProvider implements IServerDataProvider<BlockAccessor> {

    public static final CustomMachineServerDataProvider INSTANCE = new CustomMachineServerDataProvider();
    public static final ResourceLocation ID = CustomMachinery.rl("machine_server_data_provider");

    @Override
    public void appendServerData(CompoundTag nbt, BlockAccessor accessor) {
        if(accessor.getBlockEntity() instanceof CustomMachineTile machine && machine.getLevel() != null) {
            IProcessor processor = machine.getProcessor();
            CompoundTag tag = new CompoundTag();
            if(machine.getOwnerName() != null)
                tag.putString("owner", TextComponentUtils.toJsonString(machine.getOwnerName()));
            tag.putByte("status", (byte)machine.getStatus().ordinal());
            if(processor instanceof MachineProcessor machineProcessor) {
                ListTag cores = new ListTag();
                machineProcessor.getCores().forEach(core -> {
                    CompoundTag coreNbt = new CompoundTag();

                    if(core.getCurrentRecipe() != null) {
                        coreNbt.putDouble("recipeProgressTime", core.getRecipeProgressTime());
                        coreNbt.putInt("recipeTotalTime", core.getCurrentRecipe().value().getRecipeTime());
                    }
                    if(core.getError() != null)
                        coreNbt.putString("errorMessage", Component.Serializer.toJson(core.getError(), machine.getLevel().registryAccess()));
                });
                nbt.put("cores", cores);
            }
            nbt.put(CustomMachinery.MODID, tag);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
