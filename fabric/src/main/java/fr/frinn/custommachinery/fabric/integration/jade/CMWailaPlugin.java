package fr.frinn.custommachinery.fabric.integration.jade;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import java.util.Set;

@WailaPlugin
public class CMWailaPlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        //Can't use that, the block may not be created at that point, see method below.
        //registration.usePickedResult(Registration.CUSTOM_MACHINE_BLOCK.get());
        registration.registerBlockComponent(CustomMachineComponentProvider.INSTANCE, CustomMachineBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CustomMachineServerDataProvider.INSTANCE, CustomMachineTile.class);
    }

    /**
     * Ugly hack to add the machine block to the jade picked result list.
     * Needed as jade and CM load at the same time but cm register its block using arch which defer registration,
     * so the block is created and registered after jade need it, throwing a "Registry object not present" error.
     * This happened randomly depending on which one of cm and jade load faster, which is not reliable.
     * So we add the block to jade list when we are sure it is created and registered.
     */
    @SuppressWarnings("unchecked")
    public static void addMachineBlockToPickedResults() {
        try {
            Class<?> clazz = Class.forName("snownee.jade.impl.WailaClientRegistration");
            Object instance = clazz.getDeclaredField("INSTANCE").get(null);
            Set<Block> pickBlocks = (Set<Block>) clazz.getDeclaredField("pickBlocks").get(instance);
            pickBlocks.add(Registration.CUSTOM_MACHINE_BLOCK.get());
        } catch (Exception e) {
            CustomMachinery.LOGGER.error("Could not add Custom Machine block to jade picked blocks list");
        }
    }
}