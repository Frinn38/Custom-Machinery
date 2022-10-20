package fr.frinn.custommachinery.common.integration.theoneprobe;

import fr.frinn.custommachinery.CustomMachinery;
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
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import java.util.function.Function;

import static mcjty.theoneprobe.api.TextStyleClass.OK;
import static mcjty.theoneprobe.api.TextStyleClass.WARNING;

public class TOPInfoProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {

    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        probe.registerProbeConfigProvider(new IProbeConfigProvider() {
            @Override
            public void getProbeConfig(IProbeConfig config, PlayerEntity player, World world, Entity entity, IProbeHitEntityData data) {

            }

            @Override
            public void getProbeConfig(IProbeConfig config, PlayerEntity player, World world, BlockState state, IProbeHitData data) {
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
    public String getID() {
        return CustomMachinery.MODID;
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, PlayerEntity player, World world, BlockState state, IProbeHitData data) {
        TileEntity tile = world.getTileEntity(data.getPos());
        if(tile instanceof CustomMachineTile) {
            CustomMachineTile machine = (CustomMachineTile)tile;
            MachineAppearance appearance = machine.getMachine().getAppearance(machine.getStatus());
            addHarvestInfo(info, appearance, Utils.canPlayerHarvestMachine(appearance, player, world, data.getPos()));
            machine.componentManager.getProbeInfoComponents().forEach(component -> component.addProbeInfo(info));
            machine.craftingManager.addProbeInfo(info);
        }
    }

    private static final String[] harvestLevels = new String[]{
            "stone",
            "iron",
            "diamond",
            "obsidian",
            "cobalt"
    };
    private static final ResourceLocation ICONS = new ResourceLocation("theoneprobe", "textures/gui/icons.png");

    private static void addHarvestInfo(IProbeInfo info, MachineAppearance appearance, boolean canHarvest) {
        ToolType harvestTool = appearance.getTool();
        String harvestName = null;

        if (harvestTool != null) {
            int harvestLevel = appearance.getMiningLevel();
            if (harvestLevel < 0) {
                // NOTE: When a block doesn't have an explicitly-set harvest tool, getHarvestLevel will return -1 for ANY tool. (Expected behavior)
//                TheOneProbe.logger.info("HarvestLevel out of bounds (less than 0). Found " + harvestLevel);
            } else if (harvestLevel >= harvestLevels.length) {
//                TheOneProbe.logger.info("HarvestLevel out of bounds (Max value " + harvestLevels.length + "). Found " + harvestLevel);
            } else {
                harvestName = harvestLevels[harvestLevel];
            }
        }

        boolean v = true;
        int offs = v ? 16 : 0;
        int dim = v ? 13 : 16;

        ILayoutStyle alignment = info.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER);
        IIconStyle iconStyle = info.defaultIconStyle().width(v ? 18 : 20).height(v ? 14 : 16).textureWidth(32).textureHeight(32);
        IProbeInfo horizontal = info.horizontal(alignment);
        if (canHarvest) {
            horizontal.icon(ICONS, 0, offs, dim, dim, iconStyle)
                    .text(CompoundText.create().style(OK).text(((harvestTool != null) ? harvestTool.getName() : "No tool")));
        } else {
            if (harvestName == null || harvestName.isEmpty()) {
                horizontal.icon(ICONS, 16, offs, dim, dim, iconStyle)
                        .text(CompoundText.create().style(WARNING).text(((harvestTool != null) ? harvestTool.getName() : "No tool")));
            } else {
                horizontal.icon(ICONS, 16, offs, dim, dim, iconStyle)
                        .text(CompoundText.create().style(WARNING).text(((harvestTool != null) ? harvestTool.getName() : "No tool") + " (level " + harvestName + ")"));
            }
        }
    }
}
