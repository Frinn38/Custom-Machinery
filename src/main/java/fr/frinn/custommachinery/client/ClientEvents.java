package fr.frinn.custommachinery.client;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.client.render.BoxCreatorRenderer;
import fr.frinn.custommachinery.client.render.StructureCreatorRenderer;
import fr.frinn.custommachinery.client.screen.creation.MachineEditScreen;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Iterator;

@EventBusSubscriber(modid = CustomMachinery.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(final ItemTooltipEvent event) {
        CustomMachinery.UPGRADES.getUpgradesForItem(event.getItemStack().getItem())
                .forEach(upgrade -> {
                    event.getToolTip().addAll(upgrade.tooltips());

                    if(Screen.hasControlDown() || Screen.hasShiftDown()) {
                        MutableComponent machines = Component.empty();
                        for(Iterator<ResourceLocation> iterator = upgrade.machines().iterator(); iterator.hasNext();) {
                            CustomMachine machine = CustomMachinery.MACHINES.get(iterator.next());
                            if(machine == null)
                                continue;
                            machines.append(machine.getName());
                            if(iterator.hasNext())
                                machines.append(", ");
                        }
                        if(!machines.getString().isEmpty())
                            event.getToolTip().add(machines);
                        upgrade.recipeModifiers().stream().map(modifier -> Component.literal("  ").append(modifier.tooltip())).forEach(event.getToolTip()::add);
                        upgrade.components().stream().map(modifier -> Component.literal("  ").append(modifier.tooltip())).forEach(event.getToolTip()::add);
                        upgrade.coreModifier().ifPresent(modifier -> event.getToolTip().add(Component.literal("  ").append(modifier.tooltip())));
                    }
                });
    }

    @SubscribeEvent
    public static void renderLevel(final RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            BoxCreatorRenderer.renderSelectedBlocks(event.getPoseStack());
            StructureCreatorRenderer.renderSelectedBlocks(event.getPoseStack());
        }
    }

    //If for some reason the game is stopped (crash, alt+F4...) and the machine editor is currently opened
    //save the current editing machine to a temp file and ask for restoring it the next time the gui is opened
    @SubscribeEvent
    public static void playerLoggedOut(final ClientPlayerNetworkEvent.LoggingOut event) {
        if(event.getPlayer() != null && Minecraft.getInstance().screen instanceof MachineEditScreen screen && screen.isChanged())
            FileUtils.writeTempMachineJson(Minecraft.getInstance().gameDirectory, screen.getBuilder());
    }
}
