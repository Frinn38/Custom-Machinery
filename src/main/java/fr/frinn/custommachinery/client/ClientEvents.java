package fr.frinn.custommachinery.client;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.data.upgrade.MachineUpgrade;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = CustomMachinery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(final ItemTooltipEvent event) {
        List<MachineUpgrade> upgrades = CustomMachinery.UPGRADES.stream()
                .filter(upgrade -> upgrade.getItem() == event.getItemStack().getItem())
                .collect(Collectors.toList());
        if(!upgrades.isEmpty()) {
            upgrades.forEach(upgrade -> {
                event.getToolTip().add(upgrade.getTooltip());

                if(Screen.hasControlDown() || Screen.hasShiftDown())
                    upgrade.getModifiers().stream().flatMap(modifier -> modifier.getTooltip().stream()).forEach(event.getToolTip()::add);
            });
        }
    }
}
