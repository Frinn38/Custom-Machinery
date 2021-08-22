package fr.frinn.custommachinery.common.integration.theoneprobe;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.function.Function;

public class TOPInfoProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {

    public Void apply(ITheOneProbe probe) {
        probe.registerProvider(this);
        probe.registerBlockDisplayOverride((mode, info, player, world, state, data) -> {
            if(state.getBlock() == Registration.CUSTOM_MACHINE_BLOCK.get()) {
                TileEntity tile = world.getTileEntity(data.getPos());
                if(tile instanceof CustomMachineTile) {
                    CustomMachineTile customMachineTile = (CustomMachineTile)tile;
                    CustomMachine machine = customMachineTile.getMachine();
                    if(machine != null)
                        info.horizontal().item(data.getPickBlock()).vertical().text(new StringTextComponent(machine.getName())).text(CompoundText.create().style(TextStyleClass.MODNAME).text("Custom Machinery"));
                }
                return true;
            }
            return false;
        });
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
            machine.componentManager.getProbeInfoComponents().forEach(component -> component.addProbeInfo(info));
            machine.craftingManager.addProbeInfo(info);
        }
    }
}
