package fr.frinn.custommachinery.fabric.client;

import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class CustomMachineOverrideList extends ItemOverrides {

    public static final CustomMachineOverrideList INSTANCE = new CustomMachineOverrideList();

    @Nullable
    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity, int seed) {
        if(stack.getItem() != Registration.CUSTOM_MACHINE_ITEM.get() || !(model instanceof CustomMachineBakedModel machineModel))
            return super.resolve(model, stack, world, livingEntity, seed);

        return CustomMachineItem.getMachine(stack).map(machine -> machineModel.getMachineItemModel(machine.getAppearance(MachineStatus.IDLE))).orElse(model);
    }


}
