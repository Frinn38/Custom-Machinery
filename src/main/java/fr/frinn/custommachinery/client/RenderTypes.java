package fr.frinn.custommachinery.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class RenderTypes extends RenderType {

    public static final RenderType PHANTOM = create("phantom", DefaultVertexFormat.BLOCK, Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_TRANSLUCENT_SHADER).setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(true));
    public static final RenderType NOPE = create("nope", DefaultVertexFormat.BLOCK, Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_TRANSLUCENT_SHADER).setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).setDepthTestState(LEQUAL_DEPTH_TEST).createCompositeState(true));
    public static final RenderType THICK_LINES = create("thick_lines", DefaultVertexFormat.POSITION_COLOR, Mode.LINES, 256, true, false, RenderType.CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(10.0D))).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false));

    public RenderTypes(String nameIn, VertexFormat formatIn, Mode drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }
}
