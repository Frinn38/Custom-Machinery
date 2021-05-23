package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.screen.MachineLoadingScreen;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.util.Color3F;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.util.Constants;

import javax.annotation.ParametersAreNonnullByDefault;

public class CustomMachineItemRenderer extends ItemStackTileEntityRenderer {

    @ParametersAreNonnullByDefault
    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if(stack.hasTag() && stack.getTag().contains("id", Constants.NBT.TAG_STRING)) {
            ResourceLocation id = new ResourceLocation(stack.getTag().getString("id"));
            if(!CustomMachinery.MACHINES.containsKey(id) && !MachineLoadingScreen.INSTANCE.getBuilders().containsKey(id))
                return;

            CustomMachine machine = CustomMachinery.MACHINES.get(id);
            if(machine != null) {
                matrix.push();
                matrix.translate(0.5, 0.5, 0.5);
                if(transformType.isFirstPerson())
                    matrix.rotate(Vector3f.YP.rotationDegrees(90));
                ForgeHooksClient.handleCameraTransforms(matrix, CustomMachineRenderer.getMachineModel(machine.getAppearance()), transformType, transformType == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND || transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
                matrix.translate(-0.5, -0.5, -0.5);
                CustomMachineRenderer.renderMachine(machine, 0, matrix, buffer, combinedLight, combinedOverlay, tintIndex -> Color3F.of(Minecraft.getInstance().getItemColors().getColor(stack, tintIndex)));
                matrix.pop();
            }
        }
    }
}
