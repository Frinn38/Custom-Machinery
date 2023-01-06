package fr.frinn.custommachinery.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.client.screen.BaseScreen.DragType;
import fr.frinn.custommachinery.client.screen.BaseScreen.Rectangle;
import net.minecraft.util.FastColor;

import java.util.List;

public class GuiDebugUtils {

    public static void showDragAreas(PoseStack pose, List<Pair<DragType, Rectangle>> areas) {
        for(Pair<DragType, Rectangle> pair : areas) {
            int color = switch (pair.getFirst()) {
                case MOVE -> FastColor.ARGB32.color(255, 0, 255, 0);
                case TOP, LEFT, RIGHT, BOTTOM -> FastColor.ARGB32.color(255, 255, 0, 0);
                case TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT -> FastColor.ARGB32.color(255, 0, 0, 255);
                case NONE -> 0;
            };
            pair.getSecond().render(pose, color);
        }
    }
}
