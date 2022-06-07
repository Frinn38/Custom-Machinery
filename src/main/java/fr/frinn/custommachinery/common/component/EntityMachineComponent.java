package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.CustomMachineDamageSource;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.Lazy;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class EntityMachineComponent extends AbstractMachineComponent {

    private Lazy<CustomMachineDamageSource> damageSource;

    public EntityMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.BOTH);
        this.damageSource = Lazy.of(() -> new CustomMachineDamageSource(manager.getTile().getMachine().getName().getString()));
    }

    @Override
    public MachineComponentType<EntityMachineComponent> getType() {
        return Registration.ENTITY_MACHINE_COMPONENT.get();
    }

    public int getEntitiesInRadius(int radius, Predicate<Entity> filter) {
        BlockPos pos = getManager().getTile().getBlockPos();
        AABB bb = new AABB(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
        return getManager().getWorld()
                .getEntitiesOfClass(Entity.class, bb, entity -> entity.distanceToSqr(Utils.vec3dFromBlockPos(pos)) <= radius * radius && filter.test(entity))
                .size();
    }

    public double getEntitiesInRadiusHealth(int radius, Predicate<Entity> filter) {
        BlockPos pos = getManager().getTile().getBlockPos();
        AABB bb = new AABB(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
        return getManager().getWorld()
                .getEntitiesOfClass(LivingEntity.class, bb, entity -> filter.test(entity) && entity.distanceToSqr(Utils.vec3dFromBlockPos(pos)) <= radius * radius)
                .stream()
                .mapToDouble(LivingEntity::getHealth)
                .sum();
    }

    public void removeEntitiesHealth(int radius, Predicate<Entity> filter, int amount) {
        BlockPos pos = getManager().getTile().getBlockPos();
        AtomicInteger toRemove = new AtomicInteger(amount);
        AABB bb = new AABB(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
        getManager().getWorld()
                .getEntitiesOfClass(LivingEntity.class, bb, entity -> filter.test(entity) && entity.distanceToSqr(Utils.vec3dFromBlockPos(pos)) <= radius * radius)
                .forEach(entity -> {
                    int maxRemove = Math.min((int)entity.getHealth(), toRemove.get());
                    entity.hurt(this.damageSource.get(), maxRemove);
                    toRemove.addAndGet(-maxRemove);
                });
    }

    public void killEntities(int radius, Predicate<Entity> filter, int amount) {
        BlockPos pos = getManager().getTile().getBlockPos();
        AABB bb = new AABB(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
        getManager().getWorld()
                .getEntitiesOfClass(LivingEntity.class, bb, entity -> filter.test(entity) && entity.distanceToSqr(Utils.vec3dFromBlockPos(pos)) <= radius * radius)
                .stream()
                .limit(amount)
                .forEach(entity -> entity.hurt(this.damageSource.get(), Float.MAX_VALUE));
    }
}
