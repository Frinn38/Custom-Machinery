package fr.frinn.custommachinery.client;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class RenderTypes {

    public static final RenderType PHANTOM = RenderType.create("phantom", RenderSetup.builder(RenderPipelines.TRANSLUCENT_BLOCK).createRenderSetup());
    public static final RenderType NOPE = RenderType.create("nope", RenderSetup.builder(RenderPipelines.TRANSLUCENT_BLOCK).createRenderSetup());
}
