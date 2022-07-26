package fr.frinn.custommachinery.common.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class CustomMachineComponentProvider implements IComponentProvider {

    public static final CustomMachineComponentProvider INSTANCE = new CustomMachineComponentProvider();

    @Override
    public ItemStack getStack(IDataAccessor accessor, IPluginConfig config) {
        if(accessor.getTileEntity() instanceof CustomMachineTile)
            return CustomMachineItem.makeMachineItem(((CustomMachineTile)accessor.getTileEntity()).getId());
        return ItemStack.EMPTY;
    }

    @Override
    public void appendHead(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if(!tooltip.isEmpty())
            tooltip.remove(0);
        tooltip.add(accessor.getStack().getDisplayName());
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        if(accessor.getTileEntity() instanceof CustomMachineTile) {
            CompoundNBT nbt = accessor.getServerData().getCompound(CustomMachinery.MODID);
            if(nbt.isEmpty())
                return;

            boolean errored = false;

            if(nbt.contains("status", Constants.NBT.TAG_BYTE)) {
                MachineStatus machineStatus = MachineStatus.values()[nbt.getByte("status")];
                TranslationTextComponent status = machineStatus.getTranslatedName();
                switch (machineStatus) {
                    case ERRORED:
                        status.mergeStyle(TextFormatting.RED);
                        break;
                    case RUNNING:
                        status.mergeStyle(TextFormatting.GREEN);
                        break;
                    case PAUSED:
                        status.mergeStyle(TextFormatting.GOLD);
                        break;
                }
                tooltip.add(status);
                if(machineStatus == MachineStatus.ERRORED)
                    errored = true;
            }
            if(nbt.contains("recipeProgressTime", Constants.NBT.TAG_DOUBLE) && nbt.contains("recipeTotalTime", Constants.NBT.TAG_DOUBLE)) {
                double recipeProgressTime = nbt.getDouble("recipeProgressTime");
                double recipeTotalTime = nbt.getDouble("recipeTotalTime");
                ITextComponent component = new StringTextComponent((int)recipeProgressTime + " / " + (int)recipeTotalTime);
                tooltip.add(component);
            }

            if(errored && nbt.contains("errorMessage", Constants.NBT.TAG_STRING))
                tooltip.add(new StringTextComponent(nbt.getString("errorMessage")));
        }
    }
}
