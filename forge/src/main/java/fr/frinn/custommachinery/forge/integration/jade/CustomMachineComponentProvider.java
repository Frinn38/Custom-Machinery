package fr.frinn.custommachinery.forge.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class CustomMachineComponentProvider implements IComponentProvider {

    public static final CustomMachineComponentProvider INSTANCE = new CustomMachineComponentProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if(accessor.getBlockEntity() instanceof CustomMachineTile) {
            CompoundTag nbt = accessor.getServerData().getCompound(CustomMachinery.MODID);
            if(nbt.isEmpty())
                return;

            boolean errored = false;

            if(nbt.contains("status", Tag.TAG_BYTE)) {
                MachineStatus machineStatus = MachineStatus.values()[nbt.getByte("status")];
                TranslatableComponent status = machineStatus.getTranslatedName();
                switch (machineStatus) {
                    case ERRORED -> status.withStyle(ChatFormatting.RED);
                    case RUNNING -> status.withStyle(ChatFormatting.GREEN);
                    case PAUSED -> status.withStyle(ChatFormatting.GOLD);
                }
                tooltip.add(status);
                if(machineStatus == MachineStatus.ERRORED)
                    errored = true;
            }
            if(nbt.contains("recipeProgressTime", Tag.TAG_DOUBLE) && nbt.contains("recipeTotalTime", Tag.TAG_DOUBLE)) {
                double recipeProgressTime = nbt.getDouble("recipeProgressTime");
                double recipeTotalTime = nbt.getDouble("recipeTotalTime");
                float progress = (float) (recipeProgressTime / recipeTotalTime);
                Component component = new TextComponent((int)recipeProgressTime + " / " + (int)recipeTotalTime);
                tooltip.add(tooltip.getElementHelper().progress(progress, component, tooltip.getElementHelper().progressStyle(), tooltip.getElementHelper().borderStyle()));
            }

            if(errored && nbt.contains("errorMessage", Tag.TAG_STRING))
                tooltip.add(new TextComponent(nbt.getString("errorMessage")));
        }
    }
}
