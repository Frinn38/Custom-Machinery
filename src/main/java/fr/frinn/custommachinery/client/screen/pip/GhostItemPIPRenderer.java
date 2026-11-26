package fr.frinn.custommachinery.client.screen.pip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import fr.frinn.custommachinery.client.screen.pip.GhostItemPIPRenderer.GhostItemRenderState;
import fr.frinn.custommachinery.common.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiItemAtlas.SlotView;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.Set;

public class GhostItemPIPRenderer extends PictureInPictureRenderer<GhostItemRenderState> {

    private final GuiItemAtlas atlas;
    @Nullable
    private SlotView slotView = null;

    public GhostItemPIPRenderer(BufferSource bufferSource) {
        super(bufferSource);
        int slotTextureSize = 16 * Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.guiScale;
        this.atlas = new GuiItemAtlas(
                Minecraft.getInstance().gameRenderer.getSubmitNodeStorage(),
                Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher(),
                bufferSource,
                GuiItemAtlas.computeTextureSizeFor(slotTextureSize, 1),
                slotTextureSize);
    }

    @Override
    public Class<GhostItemRenderState> getRenderStateClass() {
        return GhostItemRenderState.class;
    }

    @Override
    protected void renderToTexture(GhostItemRenderState state, PoseStack pose) {
        TrackingItemStackRenderState itemRenderState = new TrackingItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().appendItemLayers(itemRenderState, state.stack, ItemDisplayContext.GUI, Minecraft.getInstance().level, null, 42);
        if(this.atlas.tryPrepareFor(Set.of(state.stack)))
            this.slotView = this.atlas.getOrUpdate(itemRenderState);
    }

    @Override
    protected void blitTexture(GhostItemRenderState state, GuiRenderState guiRenderState) {
        if(this.slotView != null) {
            guiRenderState.addBlitToCurrentLayer(new BlitRenderState(
                    RenderPipelines.GUI_TEXTURED,
                    TextureSetup.singleTexture(slotView.textureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
                    state.pose,
                    state.x0,
                    state.y0,
                    state.x1,
                    state.y1,
                    slotView.u0(),
                    slotView.u1(),
                    slotView.v0(),
                    slotView.v1(),
                    state.color.getARGB(),
                    state.scissorArea(),
                    state.bounds
            ));
        }
    }

    @Override
    protected String getTextureLabel() {
        return "Ghost item";
    }

    public record GhostItemRenderState(int x0, int x1, int y0, int y1, float scale, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds, ItemStack stack, Color color) implements PictureInPictureRenderState {
        public GhostItemRenderState(ScreenRectangle bounds, Matrix3x2f pose, ItemStack stack, Color color) {
            this(bounds.left(), bounds.right(), bounds.top(), bounds.bottom(), 1f, pose, null, bounds, stack, color);
        }
    }
}
