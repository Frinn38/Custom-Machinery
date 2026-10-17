package fr.frinn.custommachinery.impl.network;

import fr.frinn.custommachinery.api.network.DataType;
import fr.frinn.custommachinery.impl.component.config.IOSideConfig;
import fr.frinn.custommachinery.impl.component.config.ToggleSideConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SyncableList {

    private final List<Syncable<?>> list;

    public SyncableList(List<Syncable<?>> list) {
        this.list = list;
    }

    public <T> void sync(Syncable<T> syncable) {
        this.list.add(syncable);
    }

    public <T> void sync(Class<T> clazz, Supplier<T> getter, Consumer<T> setter) {
        this.sync(DataType.createSyncable(clazz, getter, setter));
    }

    public void syncBool(Supplier<Boolean> getter, Consumer<Boolean> setter) {
        this.sync(Boolean.class, getter, setter);
    }

    public void syncFloat(Supplier<Float> getter, Consumer<Float> setter) {
        this.sync(Float.class, getter, setter);
    }

    public void syncDouble(Supplier<Double> getter, Consumer<Double> setter) {
        this.sync(Double.class, getter, setter);
    }

    public void syncInt(Supplier<Integer> getter, Consumer<Integer> setter) {
        this.sync(Integer.class, getter, setter);
    }

    public void syncString(Supplier<String> getter, Consumer<String> setter) {
        this.sync(String.class, getter, setter);
    }

    public void syncLong(Supplier<Long> getter, Consumer<Long> setter) {
        this.sync(Long.class, getter, setter);
    }

    public void syncNBT(Supplier<CompoundTag> getter, Consumer<CompoundTag> setter) {
        this.sync(CompoundTag.class, getter, setter);
    }

    public void syncComponent(Supplier<Component> getter, Consumer<Component> setter) {
        this.sync(Component.class, getter, setter);
    }

    public void syncItemStack(Supplier<ItemStack> getter, Consumer<ItemStack> setter) {
        this.sync(ItemStack.class, getter, setter);
    }

    public void syncFluidStack(Supplier<FluidStack> getter, Consumer<FluidStack> setter) {
        this.sync(FluidStack.class, getter, setter);
    }

    public void syncIOSideConfig(Supplier<IOSideConfig> getter, Consumer<IOSideConfig> setter) {
        this.sync(IOSideConfig.class, getter, setter);
    }

    public void syncToggleSideConfig(Supplier<ToggleSideConfig> getter, Consumer<ToggleSideConfig> setter) {
        this.sync(ToggleSideConfig.class, getter, setter);
    }
}
