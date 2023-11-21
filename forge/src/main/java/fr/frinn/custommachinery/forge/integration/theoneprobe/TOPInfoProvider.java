package fr.frinn.custommachinery.forge.integration.theoneprobe;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.util.MachineBlockState;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IIconStyle;
import mcjty.theoneprobe.api.ILayoutStyle;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeConfigProvider;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcjty.theoneprobe.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Function;

public class TOPInfoProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {

    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        probe.registerProbeConfigProvider(new IProbeConfigProvider() {
            @Override
            public void getProbeConfig(IProbeConfig config, Player player, Level world, Entity entity, IProbeHitEntityData data) {

            }

            @Override
            public void getProbeConfig(IProbeConfig config, Player player, Level world, BlockState state, IProbeHitData data) {
                if(state.getBlock() instanceof CustomMachineBlock) {
                    config.setRFMode(1);
                    config.setTankMode(1);
                    config.showTankSetting(IProbeConfig.ConfigMode.NORMAL);
                    config.showChestContents(IProbeConfig.ConfigMode.NORMAL);
                    config.showCanBeHarvested(IProbeConfig.ConfigMode.NOT);
                    config.showHarvestLevel(IProbeConfig.ConfigMode.NOT);
                }
            }
        });
        return null;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(CustomMachinery.MODID, "machine_info_provider");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level world, BlockState state, IProbeHitData data) {
        BlockEntity tile = world.getBlockEntity(data.getPos());
        if(tile instanceof CustomMachineTile machine) {
            if(machine.getOwnerName() != null)
                info.text(CompoundText.create().label(Component.translatable("custommachinery.machine.info.owner", machine.getOwnerName())));
            MachineAppearance appearance = machine.getAppearance();
            showHarvestInfo(info, appearance, player.hasCorrectToolForDrops(MachineBlockState.CACHE.getUnchecked(appearance)));
            showCraftingManagerInfo(machine, info);
        }
    }

    private void showCraftingManagerInfo(CustomMachineTile tile, IProbeInfo info) {
        MutableComponent status = tile.getStatus().getTranslatedName();
        switch (tile.getStatus()) {
            case ERRORED -> status.withStyle(ChatFormatting.RED);
            case RUNNING -> status.withStyle(ChatFormatting.GREEN);
            case PAUSED -> status.withStyle(ChatFormatting.GOLD);
        }
        info.mcText(status);
        if(tile.getProcessor() instanceof MachineProcessor machineProcessor && machineProcessor.getCurrentContext() != null) {
            info.progress((int)machineProcessor.getRecipeProgressTime(), machineProcessor.getRecipeTotalTime(), info.defaultProgressStyle().suffix("/" + machineProcessor.getRecipeTotalTime()));
            if(tile.getStatus() == MachineStatus.ERRORED)
                info.text(tile.getMessage());
        }
    }
    private static final ResourceLocation ICONS = new ResourceLocation("theoneprobe", "textures/gui/icons.png");

    private static void showHarvestInfo(IProbeInfo probeInfo, MachineAppearance appearance, boolean harvestable) {
        List<String> tools = appearance.getTool().stream().map(TagKey::location).map(TOPInfoProvider::getTool).toList();
        String level = "" + Config.getHarvestabilityTags().get(appearance.getMiningLevel().location());
        boolean v = true;
        int offs = v ? 16 : 0;
        int dim = v ? 13 : 16;
        ILayoutStyle alignment = probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER);
        IIconStyle iconStyle = probeInfo.defaultIconStyle().width(v ? 18 : 20).height(v ? 14 : 16).textureWidth(32).textureHeight(32);
        IProbeInfo horizontal = probeInfo.horizontal(alignment);
        if (harvestable) {
            horizontal.icon(ICONS, 0, offs, dim, dim, iconStyle).text(CompoundText.create().style(TextStyleClass.OK).text(getToolText(tools)));
        } else if (level.isEmpty()) {
            horizontal.icon(ICONS, 16, offs, dim, dim, iconStyle).text(CompoundText.create().style(TextStyleClass.WARNING).text(getToolText(tools)));
        } else {
            IProbeInfo icon = horizontal.icon(ICONS, 16, offs, dim, dim, iconStyle);
            CompoundText style = CompoundText.create().style(TextStyleClass.WARNING);
            icon.text(style.text(getToolText(tools) + " (level " + level + ")"));
        }
    }

    private static String getTool(ResourceLocation tool) {
        if(Config.getTooltypeTags().containsKey(tool))
            return Config.getTooltypeTags().get(tool);

        if(tool.getPath().equals("hand"))
            return "Hand";

        return tool.toString();
    }

    private static String getToolText(List<String> tools) {
        if(tools.isEmpty())
            return "No tool";
        else if(tools.size() == 1)
            return tools.get(0);
        else
            return tools.toString();
    }
}
