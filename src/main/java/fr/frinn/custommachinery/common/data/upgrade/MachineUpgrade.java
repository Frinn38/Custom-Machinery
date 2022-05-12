package fr.frinn.custommachinery.common.data.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.TextComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class MachineUpgrade {

    public static final Codec<MachineUpgrade> CODEC = RecordCodecBuilder.create(machineUpgradeInstance ->
            machineUpgradeInstance.group(
                    ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(upgrade -> upgrade.item),
                    Codecs.list(ResourceLocation.CODEC).fieldOf("machines").forGetter(upgrade -> upgrade.machines),
                    Codecs.list(RecipeModifier.CODEC).fieldOf("modifiers").forGetter(upgrade -> upgrade.modifiers),
                    CodecLogger.loggedOptional(TextComponentUtils.TEXT_COMPONENT_CODEC,"tooltip", new TranslatableComponent("custommachinery.upgrade.tooltip").withStyle(ChatFormatting.AQUA)).forGetter(upgrade -> upgrade.tooltip),
                    CodecLogger.loggedOptional(Codec.INT,"max", 64).forGetter(upgrade -> upgrade.max)
            ).apply(machineUpgradeInstance, MachineUpgrade::new)
    );

    private final Item item;
    private final Component tooltip;
    private final List<ResourceLocation> machines;
    private final List<RecipeModifier> modifiers;
    private final int max;

    public MachineUpgrade(Item item, List<ResourceLocation> machines, List<RecipeModifier> modifiers, Component tooltip, int max) {
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

    public Component getTooltip() {
        return this.tooltip;
    }

    public int getMaxAmount() {
        return this.max;
    }
}
