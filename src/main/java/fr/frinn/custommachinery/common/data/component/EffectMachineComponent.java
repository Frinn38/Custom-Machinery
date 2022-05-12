package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

public class EffectMachineComponent extends AbstractMachineComponent {

    public EffectMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<EffectMachineComponent> getType() {
        return Registration.EFFECT_MACHINE_COMPONENT.get();
    }

    public void applyEffect(MobEffectInstance effect, int radius, Predicate<Entity> filter) {
        BlockPos machinePos = getManager().getTile().getBlockPos();
        AABB bb = new AABB(machinePos).inflate(radius);
        getManager().getWorld().getEntitiesOfClass(LivingEntity.class, bb, filter).stream()
                .filter(entity -> entity.distanceToSqr(machinePos.getX(), machinePos.getY(), machinePos.getZ()) < radius * radius)
                .forEach(entity -> entity.addEffect(effect));
    }
}
