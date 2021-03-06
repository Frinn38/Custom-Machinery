package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.CustomMachineDamageSource;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Lazy;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class EntityMachineComponent extends AbstractMachineComponent {

    private Lazy<CustomMachineDamageSource> damageSource;

    public EntityMachineComponent(MachineComponentManager manager) {
        super(manager, Mode.BOTH);
        this.damageSource = Lazy.of(() -> new CustomMachineDamageSource(manager.getTile().getMachine().getName()));
    }

    @Override
    public MachineComponentType<EntityMachineComponent> getType() {
        return Registration.ENTITY_MACHINE_COMPONENT.get();
    }

    public int getEntitiesInRadius(int radius, Predicate<Entity> filter) {
        if(getManager().getTile().getWorld() == null)
            return 0;
        BlockPos pos = getManager().getTile().getPos();
        AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
        return getManager().getTile().getWorld()
                .getEntitiesWithinAABB(Entity.class, bb, entity -> entity.getDistanceSq(Utils.vec3dFromBlockPos(pos)) <= radius * radius && filter.test(entity))
                .size();
    }

    public double getEntitiesInRadiusHealth(int radius, Predicate<Entity> filter) {
        if(getManager().getTile().getWorld() == null)
            return 0;
        BlockPos pos = getManager().getTile().getPos();
        AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
        return getManager().getTile().getWorld()
                .getEntitiesWithinAABB(LivingEntity.class, bb, entity -> filter.test(entity) && entity.getDistanceSq(Utils.vec3dFromBlockPos(pos)) <= radius * radius)
                .stream()
                .mapToDouble(LivingEntity::getHealth)
                .sum();
    }

    public void removeEntitiesHealth(int radius, Predicate<Entity> filter, int amount) {
        if(getManager().getTile().getWorld() == null)
            return;
        BlockPos pos = getManager().getTile().getPos();
        AtomicInteger toRemove = new AtomicInteger(amount);
        AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
        getManager().getTile().getWorld()
                .getEntitiesWithinAABB(LivingEntity.class, bb, entity -> filter.test(entity) && entity.getDistanceSq(Utils.vec3dFromBlockPos(pos)) <= radius * radius)
                .forEach(entity -> {
                    int maxRemove = Math.min((int)entity.getHealth(), toRemove.get());
                    entity.attackEntityFrom(this.damageSource.get(), maxRemove);
                    toRemove.addAndGet(-maxRemove);
                });
    }

    public void killEntities(int radius, Predicate<Entity> filter, int amount) {
        if(getManager().getTile().getWorld() == null)
            return;
        BlockPos pos = getManager().getTile().getPos();
        AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius, pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
        getManager().getTile().getWorld()
                .getEntitiesWithinAABB(LivingEntity.class, bb, entity -> filter.test(entity) && entity.getDistanceSq(Utils.vec3dFromBlockPos(pos)) <= radius * radius)
                .stream()
                .limit(amount)
                .forEach(entity -> entity.attackEntityFrom(this.damageSource.get(), Float.MAX_VALUE));
    }
}
