package fr.frinn.custommachinery.client;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.render.BoxCreatorRenderer;
import fr.frinn.custommachinery.client.render.StructureCreatorRenderer;
import fr.frinn.custommachinery.common.upgrade.RecipeModifier;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = CustomMachinery.MODID, bus = Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(final ItemTooltipEvent event) {
        CustomMachinery.UPGRADES.getUpgradesForItem(event.getItemStack().getItem())
                .forEach(upgrade -> {
                    event.getToolTip().addAll(upgrade.getTooltips());

                    if(Screen.hasControlDown() || Screen.hasShiftDown())
                        upgrade.getModifiers().stream().map(RecipeModifier::getTooltip).forEach(event.getToolTip()::add);
                });
    }

    @SubscribeEvent
    public static void renderLevel(final RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            BoxCreatorRenderer.renderSelectedBlocks(event.getPoseStack());
            StructureCreatorRenderer.renderSelectedBlocks(event.getPoseStack());
        }
    }
}
