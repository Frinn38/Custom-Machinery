package fr.frinn.custommachinery.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fr.frinn.custommachinery.CustomMachinery;
import net.minecraft.client.renderer.RenderPipelines;

public class CMRenderPipelines {

    public static final RenderPipeline GUI_RADIAL_FILL = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET).withFragmentShader(CustomMachinery.rl("radial_fill")).withLocation(CustomMachinery.rl("pipeline/gui_radial_fill")).build();
}
