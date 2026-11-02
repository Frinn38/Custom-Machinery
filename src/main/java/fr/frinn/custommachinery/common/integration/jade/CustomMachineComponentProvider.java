package fr.frinn.custommachinery.common.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public class CustomMachineComponentProvider implements IBlockComponentProvider {

    public static final CustomMachineComponentProvider INSTANCE = new CustomMachineComponentProvider();
    public static final ResourceLocation ID = CustomMachinery.rl("machine_component_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        IElementHelper helper = IElementHelper.get();
        if(accessor.getBlockEntity() instanceof CustomMachineTile tile) {
            CompoundTag nbt = accessor.getServerData().getCompound(CustomMachinery.MODID);
            if(nbt.isEmpty() || tile.getLevel() == null)
                return;

            if(nbt.contains("owner", Tag.TAG_STRING)) {
                Component ownerName = Component.Serializer.fromJson(nbt.getString("owner"), tile.getLevel().registryAccess());
                if(ownerName != null && !ownerName.getString().isEmpty())
                    tooltip.add(Component.translatable("custommachinery.machine.info.owner", ownerName));
            }

            if(nbt.contains("status", Tag.TAG_BYTE)) {
                MachineStatus machineStatus = MachineStatus.values()[nbt.getByte("status")];
                MutableComponent status = machineStatus.getTranslatedName();
                switch (machineStatus) {
                    case ERRORED -> status.withStyle(ChatFormatting.RED);
                    case RUNNING -> status.withStyle(ChatFormatting.GREEN);
                    case PAUSED -> status.withStyle(ChatFormatting.GOLD);
                }
                tooltip.add(status);
            }
            if(nbt.contains("cores", Tag.TAG_LIST)) {
                ListTag cores = nbt.getList("cores", Tag.TAG_COMPOUND);
                cores.forEach(tag -> {
                    if(!(tag instanceof CompoundTag coreNbt))
                        return;
                    if(coreNbt.contains("recipeProgressTime", Tag.TAG_DOUBLE) && coreNbt.contains("recipeTotalTime", Tag.TAG_INT)) {
                        double recipeProgressTime = coreNbt.getDouble("recipeProgressTime");
                        int recipeTotalTime = coreNbt.getInt("recipeTotalTime");
                        float progress = (float) (recipeProgressTime / recipeTotalTime);
                        Component component = Component.literal((int)recipeProgressTime + " / " + recipeTotalTime).withStyle(ChatFormatting.WHITE);
                        tooltip.add(helper.progress(progress, component, helper.progressStyle(), BoxStyle.getNestedBox(), true));
                    }
                    if(coreNbt.contains("errorMessage", Tag.TAG_STRING) && tile.getLevel() != null)
                        tooltip.add(Component.Serializer.fromJson(coreNbt.getString("errorMessage"), tile.getLevel().registryAccess()));
                });
            }
            if(nbt.contains("durability", Tag.TAG_INT) && nbt.contains("max_durability", Tag.TAG_INT)) {
                int durability = nbt.getInt("durability");
                int maxDurability = nbt.getInt("max_durability");
                float progress = (float) (durability / (double)maxDurability);
                Component component = Component.translatable("custommachinery.machine.component.durability").append(": " + durability + " / " + maxDurability).withStyle(ChatFormatting.WHITE);
                int color = ARGB32.color(255, Mth.hsvToRgb(Math.max(0.0F, (float)durability / maxDurability) / 3.0F, 1.0F, 1.0F));
                tooltip.add(helper.progress(progress, component, helper.progressStyle().color(color), BoxStyle.getNestedBox(), true));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
