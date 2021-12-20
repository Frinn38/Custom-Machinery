package fr.frinn.custommachinery.apiimpl.requirement;

import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.requirement.IChanceableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public abstract class AbstractDelayedChanceableRequirement<T extends IMachineComponent> extends AbstractDelayedRequirement<T> implements IChanceableRequirement<T> {

    private double chance = 1.0D;

    public AbstractDelayedChanceableRequirement(RequirementIOMode mode) {
        super(mode);
    }

    @Override
    public void setChance(double chance) {
        this.chance = MathHelper.clamp(chance, 0.0D, 1.0D);
    }

    @Override
    public boolean shouldSkip(T component, Random rand, ICraftingContext context) {
        double chance = context.getModifiedValue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }
}
