package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DropMachineComponent extends AbstractMachineComponent {

    public DropMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.BOTH);
    }

    @Override
    public MachineComponentType<DropMachineComponent> getType() {
        return Registration.DROP_MACHINE_COMPONENT.get();
    }

    public int getItemAmount(List<IIngredient<Item>> items, double radius, boolean whitelist) {
        List<Item> filter = items.stream().flatMap(ingredient -> ingredient.getAll().stream()).collect(Collectors.toList());
        AxisAlignedBB box = new AxisAlignedBB(getManager().getTile().getPos().add(radius, radius, radius), getManager().getTile().getPos().add(-radius, -radius, -radius));
        return getManager().getWorld()
                .getEntitiesWithinAABB(ItemEntity.class, box, entity -> filter.contains(entity.getItem().getItem()) == whitelist && entity.getPosition().withinDistance(getManager().getTile().getPos(), radius))
                .stream()
                .mapToInt(entity -> entity.getItem().getCount())
                .sum();
    }

    public void consumeItem(List<IIngredient<Item>> items, int amount, double radius, boolean whitelist) {
        List<Item> filter = items.stream().flatMap(ingredient -> ingredient.getAll().stream()).collect(Collectors.toList());
        AtomicInteger toRemove = new AtomicInteger(amount);
        AxisAlignedBB box = new AxisAlignedBB(getManager().getTile().getPos().add(radius, radius, radius), getManager().getTile().getPos().add(-radius, -radius, -radius));
        getManager().getWorld()
                .getEntitiesWithinAABB(ItemEntity.class, box, entity -> filter.contains(entity.getItem().getItem()) == whitelist && entity.getPosition().withinDistance(getManager().getTile().getPos(), radius))
                .forEach(entity -> {
                    int maxRemove = Math.min(toRemove.get(), entity.getItem().getCount());
                    if(maxRemove == entity.getItem().getCount())
                        entity.remove();
                    else
                        entity.getItem().shrink(maxRemove);
                    toRemove.addAndGet(-maxRemove);
                });
    }

    public boolean produceItem(ItemStack stack) {
        World world = getManager().getWorld();
        BlockPos pos = getManager().getTile().getPos().up();
        ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        return world.addEntity(entity);
    }
}
