package fr.frinn.custommachinery.client.render;

import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class CustomMachineOverrideList extends ItemOverrideList {

    @ParametersAreNonnullByDefault
    @Nullable
    @Override
    public IBakedModel getOverrideModel(IBakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity livingEntity) {
        if(stack.getItem() != Registration.CUSTOM_MACHINE_ITEM.get() || !(model instanceof CustomMachineBakedModel))
            return super.getOverrideModel(model, stack, world, livingEntity);
        CustomMachineBakedModel machineModel = (CustomMachineBakedModel) model;

        return CustomMachineItem.getMachine(stack).map(machine -> machineModel.getMachineItemModel(machine.getAppearance(MachineStatus.IDLE))).orElse(model);
    }


}
