package fr.frinn.custommachinery.forge.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CustomMachineServerDataProvider implements IServerDataProvider<BlockEntity> {

    public static final CustomMachineServerDataProvider INSTANCE = new CustomMachineServerDataProvider();

    @Override
    public void appendServerData(CompoundTag nbt, ServerPlayer player, Level level, BlockEntity tile, boolean b) {
        if(tile instanceof CustomMachineTile machine) {
            CraftingManager manager = machine.craftingManager;
            CompoundTag tag = new CompoundTag();
            tag.putByte("status", (byte)manager.getStatus().ordinal());
            if(manager.getCurrentRecipe() != null) {
                tag.putDouble("recipeProgressTime", manager.getRecipeProgressTime());
                tag.putDouble("recipeTotalTime", manager.getRecipeTotalTime());
                tag.putString("errorMessage", manager.getErrorMessage().getString());
            }
            nbt.put(CustomMachinery.MODID, tag);
        }
    }
}
