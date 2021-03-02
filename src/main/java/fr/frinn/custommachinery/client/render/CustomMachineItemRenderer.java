package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.data.MachineAppearance;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;

public class CustomMachineItemRenderer extends ItemStackTileEntityRenderer {

    private CustomMachineTile dummyTile;

    @ParametersAreNonnullByDefault
    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if(stack.hasTag() && stack.getTag().contains("id", Constants.NBT.TAG_STRING)) {
            ResourceLocation id = new ResourceLocation(stack.getTag().getString("id"));
            if(!CustomMachinery.MACHINES.containsKey(id))
                return;

            CustomMachine machine = CustomMachinery.MACHINES.get(id);

            ResourceLocation texture = machine.getAppearance().getItemTexture();
            if(texture == null || texture == MachineAppearance.DEFAULT_ITEM) {
                if(this.dummyTile == null) {
                    dummyTile = new CustomMachineTile();
                    dummyTile.setId(id);
                }
                matrix.push();
                this.handleTransformations(matrix, transformType);
                TileEntityRendererDispatcher.instance.getRenderer(dummyTile).render(dummyTile, 0, matrix, buffer, combinedLight, combinedOverlay);
                matrix.pop();
            }
        }
    }

    private void handleTransformations(MatrixStack matrix, ItemCameraTransforms.TransformType transformType) {
        if(transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
            matrix.translate(0.4F, -0.1F, 0.05F);
            matrix.scale(0.625F, 0.625F, 0.625F);
            matrix.rotate(Vector3f.YP.rotationDegrees(45));
        }
        else if(transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
            matrix.translate(-0.3F, -0.1F, 0.0F);
            matrix.scale(0.625F, 0.625F, 0.625F);
            matrix.rotate(Vector3f.YP.rotationDegrees(45));
        }
        else if(transformType == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
            matrix.translate(0.25F, 0.6F, 0.35F);
            matrix.scale(0.375F, 0.375F, 0.375F);
            matrix.rotate(Vector3f.XP.rotationDegrees(75));
            matrix.rotate(Vector3f.YP.rotationDegrees(45));
        }
        else if(transformType == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND) {
            matrix.translate(0.25F, 0.6F, 0.35F);
            matrix.scale(0.375F, 0.375F, 0.375F);
            matrix.rotate(Vector3f.XP.rotationDegrees(75));
            matrix.rotate(Vector3f.YP.rotationDegrees(45));
        }
        else if(transformType == ItemCameraTransforms.TransformType.GUI) {
            matrix.translate(0.94F, 0.24F, 0.0F);
            matrix.scale(0.625F, 0.625F, 0.625F);
            matrix.rotate(Vector3f.XP.rotationDegrees(30));
            matrix.rotate(Vector3f.YP.rotationDegrees(225));
        }
        else if(transformType == ItemCameraTransforms.TransformType.FIXED) {
            matrix.translate(0.25F, 0.25F, 0.25F);
            matrix.scale(0.5F, 0.5F, 0.5F);
        }
        else if(transformType == ItemCameraTransforms.TransformType.GROUND) {
            matrix.translate(0.4F, 0.4F, 0.4F);
            matrix.scale(0.25F, 0.25F, 0.25F);
        }
    }
}
