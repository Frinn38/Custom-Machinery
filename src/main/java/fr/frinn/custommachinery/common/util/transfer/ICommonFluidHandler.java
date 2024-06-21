package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public interface ICommonFluidHandler {

    void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode);

    void invalidate();

    void tick();

    boolean interactWithFluidHandler(Player player, InteractionHand hand);
}
