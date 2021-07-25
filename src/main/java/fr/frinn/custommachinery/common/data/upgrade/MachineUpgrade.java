package fr.frinn.custommachinery.common.data.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class MachineUpgrade {

    public static final Codec<MachineUpgrade> CODEC = RecordCodecBuilder.create(machineUpgradeInstance ->
            machineUpgradeInstance.group(
                    Codecs.ITEM_CODEC.fieldOf("item").forGetter(upgrade -> upgrade.item),
                    TextComponentUtils.TEXT_COMPONENT_CODEC.optionalFieldOf("tooltip", new TranslationTextComponent("custommachinery.upgrade.tooltip").mergeStyle(TextFormatting.AQUA)).forGetter(upgrade -> upgrade.tooltip),
                    ResourceLocation.CODEC.listOf().fieldOf("machines").forGetter(upgrade -> upgrade.machines),
                    RecipeModifier.CODEC.listOf().fieldOf("modifiers").forGetter(upgrade -> upgrade.modifiers),
                    Codec.INT.optionalFieldOf("max", 64).forGetter(upgrade -> upgrade.max)
            ).apply(machineUpgradeInstance, MachineUpgrade::new)
    );

    private Item item;
    private ITextComponent tooltip;
    private List<ResourceLocation> machines;
    private List<RecipeModifier> modifiers;
    private int max;

    public MachineUpgrade(Item item, ITextComponent tooltip, List<ResourceLocation> machines, List<RecipeModifier> modifiers, int max) {
        this.item = item;
        this.tooltip = tooltip;
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

    public ITextComponent getTooltip() {
        return this.tooltip;
    }

    public int getMaxAmount() {
        return this.max;
    }
}
