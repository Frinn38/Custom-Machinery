package fr.frinn.custommachinery.fabric.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IServerDataProvider;

public class CustomMachineServerDataProvider implements IServerDataProvider<BlockEntity> {

    public static final CustomMachineServerDataProvider INSTANCE = new CustomMachineServerDataProvider();
    public static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "machine_server_data_provider");

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public void appendServerData(CompoundTag nbt, ServerPlayer player, Level level, BlockEntity tile, boolean b) {
        if(tile instanceof CustomMachineTile machine) {
            CraftingManager manager = machine.craftingManager;
            CompoundTag tag = new CompoundTag();
            tag.putByte("status", (byte)manager.getStatus().ordinal());
            if(manager.getCurrentRecipe() != null) {
                tag.putDouble("recipeProgressTime", manager.getRecipeProgressTime());
                tag.putDouble("recipeTotalTime", manager.getRecipeTotalTime());
                tag.putString("errorMessage", Component.Serializer.toJson(manager.getErrorMessage()));
            }
            nbt.put(CustomMachinery.MODID, tag);
        }
    }
}
