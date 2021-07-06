package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.frinn.custommachinery.common.data.CustomMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

import java.util.Random;

public class MachineRenderer {

    public static final MachineRenderer INSTANCE = new MachineRenderer();
    private final Random random = new Random();

    public void renderMachineItem(CustomMachine machine, ItemStack stack, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        IBakedModel model = CustomMachineBakedModel.INSTANCE;
        IVertexBuilder builder = ItemRenderer.getEntityGlintVertexBuilder(buffer, RenderTypeLookup.func_239219_a_(stack, false), true, stack.hasEffect());
        IModelData data = new ModelDataMap.Builder().withInitial(CustomMachineBakedModel.APPEARANCE, machine.getAppearance()).build();

        for(Direction direction : Direction.values()) {
            this.random.setSeed(42L);
            Minecraft.getInstance().getItemRenderer().renderQuads(matrix, builder, model.getQuads(null, direction, this.random, data), stack, combinedLight, combinedOverlay);
        }
        this.random.setSeed(0L);
        Minecraft.getInstance().getItemRenderer().renderQuads(matrix, builder, model.getQuads(null, null, this.random, data), stack, combinedLight, combinedOverlay);
    }
}
