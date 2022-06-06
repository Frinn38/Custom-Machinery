package fr.frinn.custommachinery.common.integration.theoneprobe;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
                if(state.getBlock() == Registration.CUSTOM_MACHINE_BLOCK.get()) {
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
            MachineAppearance appearance = machine.getMachine().getAppearance(machine.getStatus());
            showHarvestInfo(info, appearance, Utils.canPlayerHarvestMachine(appearance, player, world, data.getPos()));
            showCraftingManagerInfo(machine.craftingManager, info);
        }
    }

    private void showCraftingManagerInfo(CraftingManager manager, IProbeInfo info) {
        TranslatableComponent status = manager.getStatus().getTranslatedName();
        switch (manager.getStatus()) {
            case ERRORED -> status.withStyle(ChatFormatting.RED);
            case RUNNING -> status.withStyle(ChatFormatting.GREEN);
            case PAUSED -> status.withStyle(ChatFormatting.GOLD);
        }
        info.mcText(status);
        if(manager.getCurrentRecipe() != null)
            info.progress((int)manager.getRecipeProgressTime(), manager.getRecipeTotalTime(), info.defaultProgressStyle().suffix("/" + manager.getRecipeTotalTime()));
        if(manager.getStatus() == MachineStatus.ERRORED)
            info.text(manager.getErrorMessage());
    }
    private static final ResourceLocation ICONS = new ResourceLocation("theoneprobe", "textures/gui/icons.png");

    private static void showHarvestInfo(IProbeInfo probeInfo, MachineAppearance appearance, boolean harvestable) {
        String tool = getTool(appearance.getTool().location());
        String level = "" + Config.getHarvestabilityTags().get(appearance.getMiningLevel().location());
        boolean v = true;
        int offs = v ? 16 : 0;
        int dim = v ? 13 : 16;
        ILayoutStyle alignment = probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER);
        IIconStyle iconStyle = probeInfo.defaultIconStyle().width(v ? 18 : 20).height(v ? 14 : 16).textureWidth(32).textureHeight(32);
        IProbeInfo horizontal = probeInfo.horizontal(alignment);
        if (harvestable) {
            horizontal.icon(ICONS, 0, offs, dim, dim, iconStyle).text(CompoundText.create().style(TextStyleClass.OK).text(tool.isEmpty() ? "No tool" : tool));
        } else if (level.isEmpty()) {
            horizontal.icon(ICONS, 16, offs, dim, dim, iconStyle).text(CompoundText.create().style(TextStyleClass.WARNING).text(tool.isEmpty() ? "No tool" : tool));
        } else {
            IProbeInfo var10000 = horizontal.icon(ICONS, 16, offs, dim, dim, iconStyle);
            CompoundText var10001 = CompoundText.create().style(TextStyleClass.WARNING);
            String var10002 = tool.isEmpty() ? "No tool" : tool;
            var10000.text(var10001.text(var10002 + " (level " + level + ")"));
        }
    }

    private static String getTool(ResourceLocation tool) {
        if(Config.getTooltypeTags().containsKey(tool))
            return Config.getTooltypeTags().get(tool);

        if(tool.getPath().equals("hand"))
            return "Hand";

        return tool.toString();
    }
}
