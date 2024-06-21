package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class CTUtils {

    @Nullable
    public static CompoundTag getNBT(@Nullable IData data) {
        if(data == null || !(data.getInternal() instanceof CompoundTag))
            return null;
        return (CompoundTag) data.getInternal();
    }

    @Nullable
    public static CompoundTag nbtFromStack(IItemStack stack) {
        return nbtFromStack(stack.getInternal());
    }

    @Nullable
    public static CompoundTag nbtFromStack(ItemStack stack) {
        CompoundTag nbt = null;//stack.getTag();
        if(nbt == null || nbt.isEmpty())
            return null;
        if(nbt.contains("Damage", Tag.TAG_INT) && nbt.getInt("Damage") == 0)
            nbt.remove("Damage");
        if(nbt.isEmpty())
            return null;
        return nbt;
    }

    public static ResourceLocation biomeID(Biome biome) {
        return Services.REGISTRY.biomes().getKey(biome);
    }

    public static void resetRecipesIDs() {
        CustomMachineRecipeCTBuilder.IDS.clear();
        CustomCraftRecipeCTBuilder.IDS.clear();
    }
}
