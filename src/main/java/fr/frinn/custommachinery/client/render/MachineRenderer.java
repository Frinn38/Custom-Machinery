package fr.frinn.custommachinery.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fr.frinn.custommachinery.common.data.CustomMachine;
import fr.frinn.custommachinery.common.util.Color3F;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class MachineRenderer {

    public static final MachineRenderer INSTANCE = new MachineRenderer();

    private final ThreadLocal<Cache> CACHE_COMBINED_LIGHT = ThreadLocal.withInitial(Cache::new);
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

    public void renderMachineBlock(IBlockDisplayReader world, BlockPos pos, Direction machineFacing, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedOverlay, IModelData data) {
        IBakedModel model = CustomMachineBakedModel.INSTANCE;
        BlockState state = world.getBlockState(pos);
        IVertexBuilder builder = buffer.getBuffer(RenderType.getSolid());

        Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelFlat(world, model, state, pos, matrix, builder, true, this.random, 42L, combinedOverlay, data);
        /*
        Vector3d vector3d = state.getOffset(world, pos);
        matrix.translate(vector3d.x, vector3d.y, vector3d.z);
        data = model.getModelData(world, pos, state, data);

        float[] afloat = new float[Direction.values().length * 2];
        BitSet bitset = new BitSet(3);
        AmbientOcclusionFace aof = new AmbientOcclusionFace();

        for(Direction direction : Direction.values()) {
            this.random.setSeed(42L);
            Direction rotatedDirection = getRotatedDirection(machineFacing, direction);
            List<BakedQuad> list = model.getQuads(state, rotatedDirection, this.random, data);
            if (!list.isEmpty() && Block.shouldSideBeRendered(state, world, pos, direction)) {
                this.renderQuadsSmooth(world, state, pos, direction, matrix, builder, list, afloat, bitset, aof, combinedOverlay);
            }
        }

        this.random.setSeed(42L);
        List<BakedQuad> list = model.getQuads(state, null, this.random, data);
        if (!list.isEmpty()) {
            this.renderQuadsSmooth(world, state, pos, null, matrix, builder, list, afloat, bitset, aof, combinedOverlay);
        }
        */
    }

    public Direction getRotatedDirection(Direction machineFacing, Direction quad) {
        if(quad.getAxis() == Direction.Axis.Y)
            return quad;
        switch(machineFacing) {
            case SOUTH:
                return quad.getOpposite();
            case WEST:
                return Direction.byHorizontalIndex((quad.getHorizontalIndex() + 1) % 4);
            case EAST:
                return Direction.byHorizontalIndex((quad.getHorizontalIndex() + 3) % 4);
            default:
                return quad;
        }
    }

    private void renderQuadsSmooth(IBlockDisplayReader world, BlockState state, BlockPos pos, Direction trueDirection, MatrixStack matrix, IVertexBuilder builder, List<BakedQuad> quads, float[] afloat, BitSet bitSet, AmbientOcclusionFace aoFace, int combinedOverlay) {
        for(BakedQuad bakedquad : quads) {
            this.fillQuadBounds(world, state, pos, bakedquad.getVertexData(), trueDirection, afloat, bitSet);
            aoFace.renderBlockModel(world, state, pos, trueDirection, afloat, bitSet, bakedquad.applyDiffuseLighting());
            this.renderQuadSmooth(world, state, pos, builder, matrix.getLast(), bakedquad, aoFace.vertexColorMultiplier[0], aoFace.vertexColorMultiplier[1], aoFace.vertexColorMultiplier[2], aoFace.vertexColorMultiplier[3], aoFace.vertexBrightness[0], aoFace.vertexBrightness[1], aoFace.vertexBrightness[2], aoFace.vertexBrightness[3], combinedOverlay);
        }
    }

    private void renderQuadSmooth(IBlockDisplayReader blockAccessIn, BlockState stateIn, BlockPos posIn, IVertexBuilder buffer, MatrixStack.Entry matrixEntry, BakedQuad quadIn, float colorMul0, float colorMul1, float colorMul2, float colorMul3, int brightness0, int brightness1, int brightness2, int brightness3, int combinedOverlayIn) {
        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        if (quadIn.hasTintIndex()) {
            Color3F color = Color3F.of(Minecraft.getInstance().getBlockColors().getColor(stateIn, blockAccessIn, posIn, quadIn.getTintIndex()));
            red = color.getRed();
            green = color.getGreen();
            blue = color.getBlue();
        }

        buffer.addQuad(matrixEntry, quadIn, new float[]{colorMul0, colorMul1, colorMul2, colorMul3}, red, green, blue, new int[]{brightness0, brightness1, brightness2, brightness3}, combinedOverlayIn, true);
    }

    private void fillQuadBounds(IBlockDisplayReader blockReaderIn, BlockState stateIn, BlockPos posIn, int[] vertexData, Direction face, @Nullable float[] quadBounds, BitSet boundsFlags) {
        float f = 32.0F;
        float f1 = 32.0F;
        float f2 = 32.0F;
        float f3 = -32.0F;
        float f4 = -32.0F;
        float f5 = -32.0F;

        for(int i = 0; i < 4; ++i) {
            float f6 = Float.intBitsToFloat(vertexData[i * 8]);
            float f7 = Float.intBitsToFloat(vertexData[i * 8 + 1]);
            float f8 = Float.intBitsToFloat(vertexData[i * 8 + 2]);
            f = Math.min(f, f6);
            f1 = Math.min(f1, f7);
            f2 = Math.min(f2, f8);
            f3 = Math.max(f3, f6);
            f4 = Math.max(f4, f7);
            f5 = Math.max(f5, f8);
        }

        if (quadBounds != null) {
            quadBounds[Direction.WEST.getIndex()] = f;
            quadBounds[Direction.EAST.getIndex()] = f3;
            quadBounds[Direction.DOWN.getIndex()] = f1;
            quadBounds[Direction.UP.getIndex()] = f4;
            quadBounds[Direction.NORTH.getIndex()] = f2;
            quadBounds[Direction.SOUTH.getIndex()] = f5;
            int j = Direction.values().length;
            quadBounds[Direction.WEST.getIndex() + j] = 1.0F - f;
            quadBounds[Direction.EAST.getIndex() + j] = 1.0F - f3;
            quadBounds[Direction.DOWN.getIndex() + j] = 1.0F - f1;
            quadBounds[Direction.UP.getIndex() + j] = 1.0F - f4;
            quadBounds[Direction.NORTH.getIndex() + j] = 1.0F - f2;
            quadBounds[Direction.SOUTH.getIndex() + j] = 1.0F - f5;
        }

        float f9 = 1.0E-4F;
        float f10 = 0.9999F;
        switch(face) {
            case DOWN:
                boundsFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, f1 == f4 && (f1 < 1.0E-4F || stateIn.hasOpaqueCollisionShape(blockReaderIn, posIn)));
                break;
            case UP:
                boundsFlags.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, f1 == f4 && (f4 > 0.9999F || stateIn.hasOpaqueCollisionShape(blockReaderIn, posIn)));
                break;
            case NORTH:
                boundsFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
                boundsFlags.set(0, f2 == f5 && (f2 < 1.0E-4F || stateIn.hasOpaqueCollisionShape(blockReaderIn, posIn)));
                break;
            case SOUTH:
                boundsFlags.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
                boundsFlags.set(0, f2 == f5 && (f5 > 0.9999F || stateIn.hasOpaqueCollisionShape(blockReaderIn, posIn)));
                break;
            case WEST:
                boundsFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, f == f3 && (f < 1.0E-4F || stateIn.hasOpaqueCollisionShape(blockReaderIn, posIn)));
                break;
            case EAST:
                boundsFlags.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
                boundsFlags.set(0, f == f3 && (f3 > 0.9999F || stateIn.hasOpaqueCollisionShape(blockReaderIn, posIn)));
        }

    }

    class AmbientOcclusionFace {
        private final float[] vertexColorMultiplier = new float[4];
        private final int[] vertexBrightness = new int[4];

        public AmbientOcclusionFace() {
        }

        public void renderBlockModel(IBlockDisplayReader reader, BlockState state, BlockPos pos, Direction direction, float[] vertexes, BitSet bitSet, boolean applyDiffuseLighting) {
            BlockPos blockpos = bitSet.get(0) ? pos.offset(direction) : pos;
            NeighborInfo blockmodelrenderer$neighborinfo = NeighborInfo.getNeighbourInfo(direction);
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
            Cache blockmodelrenderer$cache = CACHE_COMBINED_LIGHT.get();
            blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[0]);
            BlockState blockstate = reader.getBlockState(blockpos$mutable);
            int i = blockmodelrenderer$cache.getPackedLight(blockstate, reader, blockpos$mutable);
            float f = blockmodelrenderer$cache.getBrightness(blockstate, reader, blockpos$mutable);
            blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[1]);
            BlockState blockstate1 = reader.getBlockState(blockpos$mutable);
            int j = blockmodelrenderer$cache.getPackedLight(blockstate1, reader, blockpos$mutable);
            float f1 = blockmodelrenderer$cache.getBrightness(blockstate1, reader, blockpos$mutable);
            blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[2]);
            BlockState blockstate2 = reader.getBlockState(blockpos$mutable);
            int k = blockmodelrenderer$cache.getPackedLight(blockstate2, reader, blockpos$mutable);
            float f2 = blockmodelrenderer$cache.getBrightness(blockstate2, reader, blockpos$mutable);
            blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[3]);
            BlockState blockstate3 = reader.getBlockState(blockpos$mutable);
            int l = blockmodelrenderer$cache.getPackedLight(blockstate3, reader, blockpos$mutable);
            float f3 = blockmodelrenderer$cache.getBrightness(blockstate3, reader, blockpos$mutable);
            blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[0]).move(direction);
            boolean flag = reader.getBlockState(blockpos$mutable).getOpacity(reader, blockpos$mutable) == 0;
            blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[1]).move(direction);
            boolean flag1 = reader.getBlockState(blockpos$mutable).getOpacity(reader, blockpos$mutable) == 0;
            blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[2]).move(direction);
            boolean flag2 = reader.getBlockState(blockpos$mutable).getOpacity(reader, blockpos$mutable) == 0;
            blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[3]).move(direction);
            boolean flag3 = reader.getBlockState(blockpos$mutable).getOpacity(reader, blockpos$mutable) == 0;
            float f4;
            int i1;
            if (!flag2 && !flag) {
                f4 = f;
                i1 = i;
            } else {
                blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[0]).move(blockmodelrenderer$neighborinfo.corners[2]);
                BlockState blockstate4 = reader.getBlockState(blockpos$mutable);
                f4 = blockmodelrenderer$cache.getBrightness(blockstate4, reader, blockpos$mutable);
                i1 = blockmodelrenderer$cache.getPackedLight(blockstate4, reader, blockpos$mutable);
            }

            float f5;
            int j1;
            if (!flag3 && !flag) {
                f5 = f;
                j1 = i;
            } else {
                blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[0]).move(blockmodelrenderer$neighborinfo.corners[3]);
                BlockState blockstate6 = reader.getBlockState(blockpos$mutable);
                f5 = blockmodelrenderer$cache.getBrightness(blockstate6, reader, blockpos$mutable);
                j1 = blockmodelrenderer$cache.getPackedLight(blockstate6, reader, blockpos$mutable);
            }

            float f6;
            int k1;
            if (!flag2 && !flag1) {
                f6 = f;
                k1 = i;
            } else {
                blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[1]).move(blockmodelrenderer$neighborinfo.corners[2]);
                BlockState blockstate7 = reader.getBlockState(blockpos$mutable);
                f6 = blockmodelrenderer$cache.getBrightness(blockstate7, reader, blockpos$mutable);
                k1 = blockmodelrenderer$cache.getPackedLight(blockstate7, reader, blockpos$mutable);
            }

            float f7;
            int l1;
            if (!flag3 && !flag1) {
                f7 = f;
                l1 = i;
            } else {
                blockpos$mutable.setAndMove(blockpos, blockmodelrenderer$neighborinfo.corners[1]).move(blockmodelrenderer$neighborinfo.corners[3]);
                BlockState blockstate8 = reader.getBlockState(blockpos$mutable);
                f7 = blockmodelrenderer$cache.getBrightness(blockstate8, reader, blockpos$mutable);
                l1 = blockmodelrenderer$cache.getPackedLight(blockstate8, reader, blockpos$mutable);
            }

            int i3 = blockmodelrenderer$cache.getPackedLight(state, reader, pos);
            blockpos$mutable.setAndMove(pos, direction);
            BlockState blockstate5 = reader.getBlockState(blockpos$mutable);
            if (bitSet.get(0) || !blockstate5.isOpaqueCube(reader, blockpos$mutable)) {
                i3 = blockmodelrenderer$cache.getPackedLight(blockstate5, reader, blockpos$mutable);
            }

            float f8 = bitSet.get(0) ? blockmodelrenderer$cache.getBrightness(reader.getBlockState(blockpos), reader, blockpos) : blockmodelrenderer$cache.getBrightness(reader.getBlockState(pos), reader, pos);
            VertexTranslations blockmodelrenderer$vertextranslations = VertexTranslations.getVertexTranslations(direction);
            if (bitSet.get(1) && blockmodelrenderer$neighborinfo.doNonCubicWeight) {
                float f29 = (f3 + f + f5 + f8) * 0.25F;
                float f31 = (f2 + f + f4 + f8) * 0.25F;
                float f32 = (f2 + f1 + f6 + f8) * 0.25F;
                float f33 = (f3 + f1 + f7 + f8) * 0.25F;
                float f13 = vertexes[blockmodelrenderer$neighborinfo.vert0Weights[0].shape] * vertexes[blockmodelrenderer$neighborinfo.vert0Weights[1].shape];
                float f14 = vertexes[blockmodelrenderer$neighborinfo.vert0Weights[2].shape] * vertexes[blockmodelrenderer$neighborinfo.vert0Weights[3].shape];
                float f15 = vertexes[blockmodelrenderer$neighborinfo.vert0Weights[4].shape] * vertexes[blockmodelrenderer$neighborinfo.vert0Weights[5].shape];
                float f16 = vertexes[blockmodelrenderer$neighborinfo.vert0Weights[6].shape] * vertexes[blockmodelrenderer$neighborinfo.vert0Weights[7].shape];
                float f17 = vertexes[blockmodelrenderer$neighborinfo.vert1Weights[0].shape] * vertexes[blockmodelrenderer$neighborinfo.vert1Weights[1].shape];
                float f18 = vertexes[blockmodelrenderer$neighborinfo.vert1Weights[2].shape] * vertexes[blockmodelrenderer$neighborinfo.vert1Weights[3].shape];
                float f19 = vertexes[blockmodelrenderer$neighborinfo.vert1Weights[4].shape] * vertexes[blockmodelrenderer$neighborinfo.vert1Weights[5].shape];
                float f20 = vertexes[blockmodelrenderer$neighborinfo.vert1Weights[6].shape] * vertexes[blockmodelrenderer$neighborinfo.vert1Weights[7].shape];
                float f21 = vertexes[blockmodelrenderer$neighborinfo.vert2Weights[0].shape] * vertexes[blockmodelrenderer$neighborinfo.vert2Weights[1].shape];
                float f22 = vertexes[blockmodelrenderer$neighborinfo.vert2Weights[2].shape] * vertexes[blockmodelrenderer$neighborinfo.vert2Weights[3].shape];
                float f23 = vertexes[blockmodelrenderer$neighborinfo.vert2Weights[4].shape] * vertexes[blockmodelrenderer$neighborinfo.vert2Weights[5].shape];
                float f24 = vertexes[blockmodelrenderer$neighborinfo.vert2Weights[6].shape] * vertexes[blockmodelrenderer$neighborinfo.vert2Weights[7].shape];
                float f25 = vertexes[blockmodelrenderer$neighborinfo.vert3Weights[0].shape] * vertexes[blockmodelrenderer$neighborinfo.vert3Weights[1].shape];
                float f26 = vertexes[blockmodelrenderer$neighborinfo.vert3Weights[2].shape] * vertexes[blockmodelrenderer$neighborinfo.vert3Weights[3].shape];
                float f27 = vertexes[blockmodelrenderer$neighborinfo.vert3Weights[4].shape] * vertexes[blockmodelrenderer$neighborinfo.vert3Weights[5].shape];
                float f28 = vertexes[blockmodelrenderer$neighborinfo.vert3Weights[6].shape] * vertexes[blockmodelrenderer$neighborinfo.vert3Weights[7].shape];
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert0] = f29 * f13 + f31 * f14 + f32 * f15 + f33 * f16;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert1] = f29 * f17 + f31 * f18 + f32 * f19 + f33 * f20;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert2] = f29 * f21 + f31 * f22 + f32 * f23 + f33 * f24;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert3] = f29 * f25 + f31 * f26 + f32 * f27 + f33 * f28;
                int i2 = this.getAoBrightness(l, i, j1, i3);
                int j2 = this.getAoBrightness(k, i, i1, i3);
                int k2 = this.getAoBrightness(k, j, k1, i3);
                int l2 = this.getAoBrightness(l, j, l1, i3);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert0] = this.getVertexBrightness(i2, j2, k2, l2, f13, f14, f15, f16);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert1] = this.getVertexBrightness(i2, j2, k2, l2, f17, f18, f19, f20);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert2] = this.getVertexBrightness(i2, j2, k2, l2, f21, f22, f23, f24);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert3] = this.getVertexBrightness(i2, j2, k2, l2, f25, f26, f27, f28);
            } else {
                float f9 = (f3 + f + f5 + f8) * 0.25F;
                float f10 = (f2 + f + f4 + f8) * 0.25F;
                float f11 = (f2 + f1 + f6 + f8) * 0.25F;
                float f12 = (f3 + f1 + f7 + f8) * 0.25F;
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert0] = this.getAoBrightness(l, i, j1, i3);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert1] = this.getAoBrightness(k, i, i1, i3);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert2] = this.getAoBrightness(k, j, k1, i3);
                this.vertexBrightness[blockmodelrenderer$vertextranslations.vert3] = this.getAoBrightness(l, j, l1, i3);
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert0] = f9;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert1] = f10;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert2] = f11;
                this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert3] = f12;
            }

            float f30 = reader.func_230487_a_(direction, applyDiffuseLighting);

            for(int j3 = 0; j3 < this.vertexColorMultiplier.length; ++j3) {
                this.vertexColorMultiplier[j3] *= f30;
            }

        }

        /**
         * Get ambient occlusion brightness
         */
        private int getAoBrightness(int br1, int br2, int br3, int br4) {
            if (br1 == 0) {
                br1 = br4;
            }

            if (br2 == 0) {
                br2 = br4;
            }

            if (br3 == 0) {
                br3 = br4;
            }

            return br1 + br2 + br3 + br4 >> 2 & 16711935;
        }

        private int getVertexBrightness(int b1, int b2, int b3, int b4, float w1, float w2, float w3, float w4) {
            int i = (int)((float)(b1 >> 16 & 255) * w1 + (float)(b2 >> 16 & 255) * w2 + (float)(b3 >> 16 & 255) * w3 + (float)(b4 >> 16 & 255) * w4) & 255;
            int j = (int)((float)(b1 & 255) * w1 + (float)(b2 & 255) * w2 + (float)(b3 & 255) * w3 + (float)(b4 & 255) * w4) & 255;
            return i << 16 | j;
        }
    }

    static class Cache {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap packedLightCache = Util.make(() -> {
            Long2IntLinkedOpenHashMap long2intlinkedopenhashmap = new Long2IntLinkedOpenHashMap(100, 0.25F) {
                protected void rehash(int p_rehash_1_) {
                }
            };
            long2intlinkedopenhashmap.defaultReturnValue(Integer.MAX_VALUE);
            return long2intlinkedopenhashmap;
        });
        private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = new Long2FloatLinkedOpenHashMap(100, 0.25F) {
                protected void rehash(int p_rehash_1_) {
                }
            };
            long2floatlinkedopenhashmap.defaultReturnValue(Float.NaN);
            return long2floatlinkedopenhashmap;
        });

        private Cache() {
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
            this.packedLightCache.clear();
            this.brightnessCache.clear();
        }

        public int getPackedLight(BlockState blockStateIn, IBlockDisplayReader lightReaderIn, BlockPos blockPosIn) {
            long i = blockPosIn.toLong();
            if (this.enabled) {
                int j = this.packedLightCache.get(i);
                if (j != Integer.MAX_VALUE) {
                    return j;
                }
            }

            int k = WorldRenderer.getPackedLightmapCoords(lightReaderIn, blockStateIn, blockPosIn);
            if (this.enabled) {
                if (this.packedLightCache.size() == 100) {
                    this.packedLightCache.removeFirstInt();
                }

                this.packedLightCache.put(i, k);
            }

            return k;
        }

        public float getBrightness(BlockState blockStateIn, IBlockDisplayReader lightReaderIn, BlockPos blockPosIn) {
            long i = blockPosIn.toLong();
            if (this.enabled) {
                float f = this.brightnessCache.get(i);
                if (!Float.isNaN(f)) {
                    return f;
                }
            }

            float f1 = blockStateIn.getAmbientOcclusionLightValue(lightReaderIn, blockPosIn);
            if (this.enabled) {
                if (this.brightnessCache.size() == 100) {
                    this.brightnessCache.removeFirstFloat();
                }

                this.brightnessCache.put(i, f1);
            }

            return f1;
        }
    }

    public enum NeighborInfo {
        DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5F, true, new Orientation[]{Orientation.FLIP_WEST, Orientation.SOUTH, Orientation.FLIP_WEST, Orientation.FLIP_SOUTH, Orientation.WEST, Orientation.FLIP_SOUTH, Orientation.WEST, Orientation.SOUTH}, new Orientation[]{Orientation.FLIP_WEST, Orientation.NORTH, Orientation.FLIP_WEST, Orientation.FLIP_NORTH, Orientation.WEST, Orientation.FLIP_NORTH, Orientation.WEST, Orientation.NORTH}, new Orientation[]{Orientation.FLIP_EAST, Orientation.NORTH, Orientation.FLIP_EAST, Orientation.FLIP_NORTH, Orientation.EAST, Orientation.FLIP_NORTH, Orientation.EAST, Orientation.NORTH}, new Orientation[]{Orientation.FLIP_EAST, Orientation.SOUTH, Orientation.FLIP_EAST, Orientation.FLIP_SOUTH, Orientation.EAST, Orientation.FLIP_SOUTH, Orientation.EAST, Orientation.SOUTH}),
        UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0F, true, new Orientation[]{Orientation.EAST, Orientation.SOUTH, Orientation.EAST, Orientation.FLIP_SOUTH, Orientation.FLIP_EAST, Orientation.FLIP_SOUTH, Orientation.FLIP_EAST, Orientation.SOUTH}, new Orientation[]{Orientation.EAST, Orientation.NORTH, Orientation.EAST, Orientation.FLIP_NORTH, Orientation.FLIP_EAST, Orientation.FLIP_NORTH, Orientation.FLIP_EAST, Orientation.NORTH}, new Orientation[]{Orientation.WEST, Orientation.NORTH, Orientation.WEST, Orientation.FLIP_NORTH, Orientation.FLIP_WEST, Orientation.FLIP_NORTH, Orientation.FLIP_WEST, Orientation.NORTH}, new Orientation[]{Orientation.WEST, Orientation.SOUTH, Orientation.WEST, Orientation.FLIP_SOUTH, Orientation.FLIP_WEST, Orientation.FLIP_SOUTH, Orientation.FLIP_WEST, Orientation.SOUTH}),
        NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8F, true, new Orientation[]{Orientation.UP, Orientation.FLIP_WEST, Orientation.UP, Orientation.WEST, Orientation.FLIP_UP, Orientation.WEST, Orientation.FLIP_UP, Orientation.FLIP_WEST}, new Orientation[]{Orientation.UP, Orientation.FLIP_EAST, Orientation.UP, Orientation.EAST, Orientation.FLIP_UP, Orientation.EAST, Orientation.FLIP_UP, Orientation.FLIP_EAST}, new Orientation[]{Orientation.DOWN, Orientation.FLIP_EAST, Orientation.DOWN, Orientation.EAST, Orientation.FLIP_DOWN, Orientation.EAST, Orientation.FLIP_DOWN, Orientation.FLIP_EAST}, new Orientation[]{Orientation.DOWN, Orientation.FLIP_WEST, Orientation.DOWN, Orientation.WEST, Orientation.FLIP_DOWN, Orientation.WEST, Orientation.FLIP_DOWN, Orientation.FLIP_WEST}),
        SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8F, true, new Orientation[]{Orientation.UP, Orientation.FLIP_WEST, Orientation.FLIP_UP, Orientation.FLIP_WEST, Orientation.FLIP_UP, Orientation.WEST, Orientation.UP, Orientation.WEST}, new Orientation[]{Orientation.DOWN, Orientation.FLIP_WEST, Orientation.FLIP_DOWN, Orientation.FLIP_WEST, Orientation.FLIP_DOWN, Orientation.WEST, Orientation.DOWN, Orientation.WEST}, new Orientation[]{Orientation.DOWN, Orientation.FLIP_EAST, Orientation.FLIP_DOWN, Orientation.FLIP_EAST, Orientation.FLIP_DOWN, Orientation.EAST, Orientation.DOWN, Orientation.EAST}, new Orientation[]{Orientation.UP, Orientation.FLIP_EAST, Orientation.FLIP_UP, Orientation.FLIP_EAST, Orientation.FLIP_UP, Orientation.EAST, Orientation.UP, Orientation.EAST}),
        WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new Orientation[]{Orientation.UP, Orientation.SOUTH, Orientation.UP, Orientation.FLIP_SOUTH, Orientation.FLIP_UP, Orientation.FLIP_SOUTH, Orientation.FLIP_UP, Orientation.SOUTH}, new Orientation[]{Orientation.UP, Orientation.NORTH, Orientation.UP, Orientation.FLIP_NORTH, Orientation.FLIP_UP, Orientation.FLIP_NORTH, Orientation.FLIP_UP, Orientation.NORTH}, new Orientation[]{Orientation.DOWN, Orientation.NORTH, Orientation.DOWN, Orientation.FLIP_NORTH, Orientation.FLIP_DOWN, Orientation.FLIP_NORTH, Orientation.FLIP_DOWN, Orientation.NORTH}, new Orientation[]{Orientation.DOWN, Orientation.SOUTH, Orientation.DOWN, Orientation.FLIP_SOUTH, Orientation.FLIP_DOWN, Orientation.FLIP_SOUTH, Orientation.FLIP_DOWN, Orientation.SOUTH}),
        EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new Orientation[]{Orientation.FLIP_DOWN, Orientation.SOUTH, Orientation.FLIP_DOWN, Orientation.FLIP_SOUTH, Orientation.DOWN, Orientation.FLIP_SOUTH, Orientation.DOWN, Orientation.SOUTH}, new Orientation[]{Orientation.FLIP_DOWN, Orientation.NORTH, Orientation.FLIP_DOWN, Orientation.FLIP_NORTH, Orientation.DOWN, Orientation.FLIP_NORTH, Orientation.DOWN, Orientation.NORTH}, new Orientation[]{Orientation.FLIP_UP, Orientation.NORTH, Orientation.FLIP_UP, Orientation.FLIP_NORTH, Orientation.UP, Orientation.FLIP_NORTH, Orientation.UP, Orientation.NORTH}, new Orientation[]{Orientation.FLIP_UP, Orientation.SOUTH, Orientation.FLIP_UP, Orientation.FLIP_SOUTH, Orientation.UP, Orientation.FLIP_SOUTH, Orientation.UP, Orientation.SOUTH});

        private final Direction[] corners;
        private final boolean doNonCubicWeight;
        private final Orientation[] vert0Weights;
        private final Orientation[] vert1Weights;
        private final Orientation[] vert2Weights;
        private final Orientation[] vert3Weights;
        private static final NeighborInfo[] VALUES = Util.make(new NeighborInfo[6], (array) -> {
            array[Direction.DOWN.getIndex()] = DOWN;
            array[Direction.UP.getIndex()] = UP;
            array[Direction.NORTH.getIndex()] = NORTH;
            array[Direction.SOUTH.getIndex()] = SOUTH;
            array[Direction.WEST.getIndex()] = WEST;
            array[Direction.EAST.getIndex()] = EAST;
        });

        NeighborInfo(Direction[] cornersIn, float brightness, boolean doNonCubicWeightIn, Orientation[] vert0WeightsIn, Orientation[] vert1WeightsIn, Orientation[] vert2WeightsIn, Orientation[] vert3WeightsIn) {
            this.corners = cornersIn;
            this.doNonCubicWeight = doNonCubicWeightIn;
            this.vert0Weights = vert0WeightsIn;
            this.vert1Weights = vert1WeightsIn;
            this.vert2Weights = vert2WeightsIn;
            this.vert3Weights = vert3WeightsIn;
        }

        public static NeighborInfo getNeighbourInfo(Direction facing) {
            return VALUES[facing.getIndex()];
        }
    }

    enum Orientation {
        DOWN(Direction.DOWN, false),
        UP(Direction.UP, false),
        NORTH(Direction.NORTH, false),
        SOUTH(Direction.SOUTH, false),
        WEST(Direction.WEST, false),
        EAST(Direction.EAST, false),
        FLIP_DOWN(Direction.DOWN, true),
        FLIP_UP(Direction.UP, true),
        FLIP_NORTH(Direction.NORTH, true),
        FLIP_SOUTH(Direction.SOUTH, true),
        FLIP_WEST(Direction.WEST, true),
        FLIP_EAST(Direction.EAST, true);

        private final int shape;

        Orientation(Direction facingIn, boolean flip) {
            this.shape = facingIn.getIndex() + (flip ? Direction.values().length : 0);
        }
    }

    enum VertexTranslations {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        private final int vert0;
        private final int vert1;
        private final int vert2;
        private final int vert3;
        private static final VertexTranslations[] VALUES = Util.make(new VertexTranslations[6], (array) -> {
            array[Direction.DOWN.getIndex()] = DOWN;
            array[Direction.UP.getIndex()] = UP;
            array[Direction.NORTH.getIndex()] = NORTH;
            array[Direction.SOUTH.getIndex()] = SOUTH;
            array[Direction.WEST.getIndex()] = WEST;
            array[Direction.EAST.getIndex()] = EAST;
        });

        VertexTranslations(int vert0In, int vert1In, int vert2In, int vert3In) {
            this.vert0 = vert0In;
            this.vert1 = vert1In;
            this.vert2 = vert2In;
            this.vert3 = vert3In;
        }

        public static VertexTranslations getVertexTranslations(Direction facingIn) {
            return VALUES[facingIn.getIndex()];
        }
    }
}
