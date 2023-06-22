package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;

public class MachineUpgrade {

    public static final NamedCodec<MachineUpgrade> CODEC = NamedCodec.record(machineUpgradeInstance ->
            machineUpgradeInstance.group(
                    RegistrarCodec.ITEM.fieldOf("item").forGetter(upgrade -> upgrade.item),
                    DefaultCodecs.RESOURCE_LOCATION.listOf().fieldOf("machines").forGetter(upgrade -> upgrade.machines),
                    RecipeModifier.CODEC.listOf().fieldOf("modifiers").forGetter(upgrade -> upgrade.modifiers),
                    TextComponentUtils.CODEC.listOf().optionalFieldOf("tooltip", Collections.singletonList(Component.translatable("custommachinery.upgrade.tooltip").withStyle(ChatFormatting.AQUA))).forGetter(upgrade -> upgrade.tooltips),
                    NamedCodec.INT.optionalFieldOf("max", 64).forGetter(upgrade -> upgrade.max)
            ).apply(machineUpgradeInstance, MachineUpgrade::new), "Machine upgrade"
    );

    private final Item item;
    private final List<Component> tooltips;
    private final List<ResourceLocation> machines;
    private final List<RecipeModifier> modifiers;
    private final int max;

    public MachineUpgrade(Item item, List<ResourceLocation> machines, List<RecipeModifier> modifiers, List<Component> tooltips, int max) {
        this.item = item;
        this.tooltips = tooltips;
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

    public List<Component> getTooltips() {
        return this.tooltips;
    }

    public int getMaxAmount() {
        return this.max;
    }
}
