package fr.frinn.custommachinery.common.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CustomMachineServerDataProvider implements IServerDataProvider<TileEntity> {

    public static final CustomMachineServerDataProvider INSTANCE = new CustomMachineServerDataProvider();

    @Override
    public void appendServerData(CompoundNBT nbt, ServerPlayerEntity player, World level, TileEntity tile) {
        if(tile instanceof CustomMachineTile) {
            CraftingManager manager = ((CustomMachineTile)tile).craftingManager;
            CompoundNBT tag = new CompoundNBT();
            tag.putByte("status", (byte)manager.getStatus().ordinal());
            if(manager.getCurrentRecipe() != null) {
                tag.putDouble("recipeProgressTime", manager.recipeProgressTime);
                tag.putDouble("recipeTotalTime", manager.recipeTotalTime);
                tag.putString("errorMessage", manager.getErrorMessage().getString());
            }
            nbt.put(CustomMachinery.MODID, tag);
        }
    }
}