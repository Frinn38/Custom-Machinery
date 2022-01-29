package fr.frinn.custommachinery.apiimpl.requirement;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.requirement.IDelayedRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import net.minecraft.util.math.MathHelper;

public abstract class AbstractDelayedRequirement<T extends IMachineComponent> extends AbstractRequirement<T> implements IDelayedRequirement<T> {

    private double delay = 0.0D;

    public AbstractDelayedRequirement(RequirementIOMode mode) {
        super(mode);
    }

    @Override
    public void setDelay(double delay) {
        this.delay = MathHelper.clamp(delay, 0.0D, 1.0D);
    }

    @Override
    public double getDelay() {
        return this.delay;
    }

    public boolean isDelayed() {
        return this.delay != 0.0D;
    }
}
