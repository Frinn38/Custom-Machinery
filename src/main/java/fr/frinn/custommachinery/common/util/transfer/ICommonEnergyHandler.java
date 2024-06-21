package fr.frinn.custommachinery.common.util.transfer;

import fr.frinn.custommachinery.impl.component.config.RelativeSide;
import fr.frinn.custommachinery.impl.component.config.SideMode;

public interface ICommonEnergyHandler {

    void configChanged(RelativeSide side, SideMode oldMode, SideMode newMode);

    void invalidate();

    void tick();
}
