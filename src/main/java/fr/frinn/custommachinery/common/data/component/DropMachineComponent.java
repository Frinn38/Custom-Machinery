package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DropMachineComponent extends AbstractMachineComponent {

    public DropMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.BOTH);
    }

    @Override
    public MachineComponentType<DropMachineComponent> getType() {
        return Registration.DROP_MACHINE_COMPONENT.get();
    }

    public int getItemAmount(List<IIngredient<Item>> items, double radius, boolean whitelist) {
        List<Item> filter = items.stream().flatMap(ingredient -> ingredient.getAll().stream()).toList();
        AABB box = new AABB(getManager().getTile().getBlockPos().offset(radius, radius, radius), getManager().getTile().getBlockPos().offset(-radius, -radius, -radius));
        return getManager().getWorld()
                .getEntitiesOfClass(ItemEntity.class, box, entity -> filter.contains(entity.getItem().getItem()) == whitelist && entity.blockPosition().closerThan(getManager().getTile().getBlockPos(), radius))
                .stream()
                .mapToInt(entity -> entity.getItem().getCount())
                .sum();
    }

    public void consumeItem(List<IIngredient<Item>> items, int amount, double radius, boolean whitelist) {
        List<Item> filter = items.stream().flatMap(ingredient -> ingredient.getAll().stream()).toList();
        AtomicInteger toRemove = new AtomicInteger(amount);
        AABB box = new AABB(getManager().getTile().getBlockPos().offset(radius, radius, radius), getManager().getTile().getBlockPos().offset(-radius, -radius, -radius));
        getManager().getWorld()
                .getEntitiesOfClass(ItemEntity.class, box, entity -> filter.contains(entity.getItem().getItem()) == whitelist && entity.blockPosition().closerThan(getManager().getTile().getBlockPos(), radius))
                .forEach(entity -> {
                    int maxRemove = Math.min(toRemove.get(), entity.getItem().getCount());
                    if(maxRemove == entity.getItem().getCount())
                        entity.remove(RemovalReason.DISCARDED);
                    else
                        entity.getItem().shrink(maxRemove);
                    toRemove.addAndGet(-maxRemove);
                });
    }

    public boolean produceItem(ItemStack stack) {
        Level world = getManager().getWorld();
        BlockPos pos = getManager().getTile().getBlockPos().above();
        ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        return world.addFreshEntity(entity);
    }
}
