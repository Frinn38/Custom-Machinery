package fr.frinn.custommachinery.common.integration.theoneprobe;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

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
            machine.componentManager.getComponents().forEach(component -> component.addProbeInfo(info));
        }
    }
}
