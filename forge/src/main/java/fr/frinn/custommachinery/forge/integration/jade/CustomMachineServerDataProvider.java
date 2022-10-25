package fr.frinn.custommachinery.forge.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.crafting.IProcessor;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CustomMachineServerDataProvider implements IServerDataProvider<BlockEntity> {

    public static final CustomMachineServerDataProvider INSTANCE = new CustomMachineServerDataProvider();

    @Override
    public void appendServerData(CompoundTag nbt, ServerPlayer player, Level level, BlockEntity tile, boolean b) {
        if(tile instanceof CustomMachineTile machine) {
            IProcessor processor = machine.getProcessor();
            CompoundTag tag = new CompoundTag();
            tag.putByte("status", (byte)machine.getStatus().ordinal());
            if(processor instanceof MachineProcessor machineProcessor && processor.getCurrentContext() != null) {
                tag.putDouble("recipeProgressTime", machineProcessor.getRecipeProgressTime());
                tag.putDouble("recipeTotalTime", machineProcessor.getRecipeTotalTime());
                tag.putString("errorMessage", Component.Serializer.toJson(machine.getMessage()));
            }
            nbt.put(CustomMachinery.MODID, tag);
        }
    }
}
