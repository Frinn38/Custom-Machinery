package fr.frinn.custommachinery.client;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

import java.util.OptionalDouble;

public class RenderTypes extends RenderType {

    public static final RenderType PHANTOM = makeType("phantom", DefaultVertexFormats.BLOCK, 7, 2097152, true, false, RenderType.State.getBuilder().shadeModel(SHADE_ENABLED).lightmap(LIGHTMAP_ENABLED).texture(BLOCK_SHEET_MIPPED).transparency(TRANSLUCENT_TRANSPARENCY).build(true));
    public static final RenderType NOPE = makeType("nope", DefaultVertexFormats.BLOCK, 7, 2097152, true, false, RenderType.State.getBuilder().shadeModel(SHADE_ENABLED).lightmap(LIGHTMAP_ENABLED).texture(BLOCK_SHEET_MIPPED).alpha(HALF_ALPHA).depthTest(DEPTH_LEQUAL).build(true));
    public static final RenderType THICK_LINES = makeType("thick_lines", DefaultVertexFormats.POSITION_COLOR, 1, 256, RenderType.State.getBuilder().line(new RenderState.LineState(OptionalDouble.of(10.0D))).layer(VIEW_OFFSET_Z_LAYERING).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_ENTITY_TARGET).writeMask(COLOR_DEPTH_WRITE).build(false));

    public RenderTypes(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }
}
