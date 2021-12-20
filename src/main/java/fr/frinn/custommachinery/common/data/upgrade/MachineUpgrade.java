package fr.frinn.custommachinery.common.data.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.codec.RegistryCodec;
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
                    RegistryCodec.ITEM.fieldOf("item").forGetter(upgrade -> upgrade.item),
                    Codecs.list(ResourceLocation.CODEC).fieldOf("machines").forGetter(upgrade -> upgrade.machines),
                    Codecs.list(RecipeModifier.CODEC).fieldOf("modifiers").forGetter(upgrade -> upgrade.modifiers),
                    CodecLogger.loggedOptional(TextComponentUtils.TEXT_COMPONENT_CODEC,"tooltip", new TranslationTextComponent("custommachinery.upgrade.tooltip").mergeStyle(TextFormatting.AQUA)).forGetter(upgrade -> upgrade.tooltip),
                    CodecLogger.loggedOptional(Codec.INT,"max", 64).forGetter(upgrade -> upgrade.max)
            ).apply(machineUpgradeInstance, MachineUpgrade::new)
    );

    private Item item;
    private ITextComponent tooltip;
    private List<ResourceLocation> machines;
    private List<RecipeModifier> modifiers;
    private int max;

    public MachineUpgrade(Item item, List<ResourceLocation> machines, List<RecipeModifier> modifiers, ITextComponent tooltip, int max) {
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
