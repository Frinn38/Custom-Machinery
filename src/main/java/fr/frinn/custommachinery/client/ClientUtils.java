package fr.frinn.custommachinery.client;

import fr.frinn.custommachinery.common.init.CustomMachineContainer;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientUtils {

    public static CustomMachineTile getClientSideCustomMachineTile(BlockPos pos) {
        if(Minecraft.getInstance().level != null) {
            BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile)
                return (CustomMachineTile)tile;
        }
        throw new IllegalStateException("Trying to open a Custom Machine container without clicking on a Custom Machine block");
    }

    public static void refreshMachineContainer() {
        if(Minecraft.getInstance().player instanceof Player player && player.containerMenu instanceof CustomMachineContainer container)
            container.init();
    }

    public static void addToast(Component title) {
        Minecraft.getInstance().getToastManager().addToast(new TutorialToast(Minecraft.getInstance().font, TutorialToast.Icons.MOUSE, title, null, false, 50));
    }
}
