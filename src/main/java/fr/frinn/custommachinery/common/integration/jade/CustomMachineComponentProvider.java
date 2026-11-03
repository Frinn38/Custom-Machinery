package fr.frinn.custommachinery.common.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ProgressView.Part;

public class CustomMachineComponentProvider implements IBlockComponentProvider {

    public static final CustomMachineComponentProvider INSTANCE = new CustomMachineComponentProvider();
    public static final Identifier ID = CustomMachinery.rl("machine_component_provider");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if(accessor.getBlockEntity() instanceof CustomMachineTile tile) {
            CompoundTag nbt = accessor.getServerData().getCompoundOrEmpty(CustomMachinery.MODID);
            if(nbt.isEmpty() || tile.getLevel() == null)
                return;

            if(nbt.contains("owner")) {
                Component ownerName = TextComponentUtils.fromJSON(nbt.getStringOr("owner", ""));
                if(!ownerName.getString().isEmpty())
                    tooltip.add(Component.translatable("custommachinery.machine.info.owner", ownerName));
            }

            if(nbt.contains("status")) {
                MachineStatus machineStatus = MachineStatus.values()[nbt.getByteOr("status", (byte)0)];
                MutableComponent status = machineStatus.getTranslatedName();
                switch (machineStatus) {
                    case ERRORED -> status.withStyle(ChatFormatting.RED);
                    case RUNNING -> status.withStyle(ChatFormatting.GREEN);
                    case PAUSED -> status.withStyle(ChatFormatting.GOLD);
                }
                tooltip.add(status);
            }
            if(nbt.contains("cores")) {
                ListTag cores = nbt.getListOrEmpty("cores");
                cores.forEach(tag -> {
                    if(!(tag instanceof CompoundTag coreNbt))
                        return;
                    if(coreNbt.contains("recipeProgressTime") && coreNbt.contains("recipeTotalTime")) {
                        double recipeProgressTime = coreNbt.getDoubleOr("recipeProgressTime", 0);
                        int recipeTotalTime = coreNbt.getIntOr("recipeTotalTime", 0);
                        float progress = (float) (recipeProgressTime / recipeTotalTime);
                        Component component = Component.literal((int)recipeProgressTime + " / " + recipeTotalTime).withStyle(ChatFormatting.WHITE);
                        ProgressView view = new ProgressView(Part.of(progress), component, JadeUI.progressStyle(), BoxStyle.nestedBox());
                        tooltip.add(JadeUI.progress(view));
                    }
                    if(coreNbt.contains("errorMessage") && tile.getLevel() != null)
                        tooltip.add(TextComponentUtils.fromJSON(coreNbt.getStringOr("errorMessage", "")));
                });
            }
            if(nbt.contains("durability") && nbt.contains("max_durability")) {
                int durability = nbt.getIntOr("durability", 0);
                int maxDurability = nbt.getIntOr("max_durability", 0);
                if(maxDurability == 0)
                    return;
                float progress = (float) (durability / (double)maxDurability);
                Component component = Component.translatable("custommachinery.machine.component.durability").append(": " + durability + " / " + maxDurability).withStyle(ChatFormatting.WHITE);
                int color = ARGB.color(255, Mth.hsvToRgb(Math.max(0.0F, (float)durability / maxDurability) / 3.0F, 1.0F, 1.0F));
                ProgressView view = new ProgressView(Part.of(progress, color), component, JadeUI.progressStyle(), BoxStyle.nestedBox());
                tooltip.add(JadeUI.progress(view));
            }
        }
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}
