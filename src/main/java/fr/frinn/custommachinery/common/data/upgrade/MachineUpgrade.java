package fr.frinn.custommachinery.common.data.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class MachineUpgrade {

    @SuppressWarnings("deprecation")
    public static final Codec<MachineUpgrade> CODEC = RecordCodecBuilder.create(machineUpgradeInstance ->
            machineUpgradeInstance.group(
                    Registry.ITEM.fieldOf("item").forGetter(upgrade -> upgrade.item),
                    ResourceLocation.CODEC.listOf().fieldOf("machines").forGetter(upgrade -> upgrade.machines),
                    RecipeModifier.CODEC.listOf().fieldOf("modifiers").forGetter(upgrade -> upgrade.modifiers),
                    Codec.INT.optionalFieldOf("max", 64).forGetter(upgrade -> upgrade.max)
            ).apply(machineUpgradeInstance, MachineUpgrade::new)
    );

    private Item item;
    private List<ResourceLocation> machines;
    private List<RecipeModifier> modifiers;
    private int max;

    public MachineUpgrade(Item item, List<ResourceLocation> machines, List<RecipeModifier> modifiers, int max) {
        this.item = item;
        this.machines = machines;
        this.modifiers = modifiers;
        this.max = max;
    }

    public Item getItem() {
        return this.item;
    }

    public List<ResourceLocation> getMachines() {
        return this.machines;
    }

    public List<RecipeModifier> getModifiers() {
        return this.modifiers;
    }

    public int getMaxAmount() {
        return this.max;
    }
}
