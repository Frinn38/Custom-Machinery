package fr.frinn.custommachinery.fabric.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class CustomMachineComponentProvider implements IBlockComponentProvider {

    public static final CustomMachineComponentProvider INSTANCE = new CustomMachineComponentProvider();
    public static final ResourceLocation ID = new ResourceLocation(CustomMachinery.MODID, "machine_component_provider");

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if(accessor.getBlockEntity() instanceof CustomMachineTile) {
            CompoundTag nbt = accessor.getServerData().getCompound(CustomMachinery.MODID);
            if(nbt.isEmpty())
                return;

            boolean errored = false;

            if(nbt.contains("status", Tag.TAG_BYTE)) {
                MachineStatus machineStatus = MachineStatus.values()[nbt.getByte("status")];
                MutableComponent status = machineStatus.getTranslatedName();
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
                Component component = Component.literal((int)recipeProgressTime + " / " + (int)recipeTotalTime);
                tooltip.add(tooltip.getElementHelper().progress(progress, component, tooltip.getElementHelper().progressStyle(), tooltip.getElementHelper().borderStyle()));
            }

            if(errored && nbt.contains("errorMessage", Tag.TAG_STRING))
                tooltip.add(Component.Serializer.fromJson(nbt.getString("errorMessage")));
        }
    }
}
