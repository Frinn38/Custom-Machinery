package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class EffectMachineComponent extends AbstractMachineComponent {

    public EffectMachineComponent(MachineComponentManager manager) {
        super(manager, Mode.NONE);
    }

    @Override
    public MachineComponentType<EffectMachineComponent> getType() {
        return Registration.EFFECT_MACHINE_COMPONENT.get();
    }

    public void applyEffect(EffectInstance effect, int radius, Predicate<Entity> filter) {
        if(getManager().getTile().getWorld() == null)
            return;
        BlockPos machinePos = getManager().getTile().getPos();
        AxisAlignedBB bb = new AxisAlignedBB(machinePos).grow(radius);
        getManager().getTile().getWorld().getEntitiesWithinAABB(LivingEntity.class, bb, filter).stream()
                .filter(entity -> entity.getDistanceSq(machinePos.getX(), machinePos.getY(), machinePos.getZ()) < radius * radius)
                .filter(entity -> entity.getActivePotionEffect(effect.getPotion()) == null)
                .forEach(entity -> entity.addPotionEffect(effect));
    }
}
