package fr.frinn.custommachinery.common.upgrade;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record MachineUpgrade(Item item, List<ResourceLocation> machines, List<RecipeModifier> recipeModifiers,
                             List<ComponentModifier> components, Optional<CoreModifier> coreModifier, List<Component> tooltips, int max) {

    public static final Component DEFAULT_TOOLTIP = Component.translatable("custommachinery.upgrade.tooltip").withStyle(ChatFormatting.AQUA);

    public static final NamedCodec<MachineUpgrade> CODEC = NamedCodec.record(machineUpgradeInstance ->
            machineUpgradeInstance.group(
                    RegistrarCodec.ITEM.fieldOf("item").forGetter(upgrade -> upgrade.item),
                    DefaultCodecs.RESOURCE_LOCATION.listOf().fieldOf("machines").forGetter(upgrade -> upgrade.machines),
                    RecipeModifier.CODEC.listOf().optionalFieldOf("requirements", Collections.emptyList()).aliases("modifiers").forGetter(upgrade -> upgrade.recipeModifiers),
                    ComponentModifier.CODEC.listOf().optionalFieldOf("components", Collections.emptyList()).forGetter(upgrade -> upgrade.components),
                    CoreModifier.CODEC.optionalFieldOf("core").forGetter(upgrade -> upgrade.coreModifier),
                    TextComponentUtils.CODEC.listOf().optionalFieldOf("tooltip", Collections.singletonList(DEFAULT_TOOLTIP)).forGetter(upgrade -> upgrade.tooltips),
                    NamedCodec.INT.optionalFieldOf("max", 64).forGetter(upgrade -> upgrade.max)
            ).apply(machineUpgradeInstance, MachineUpgrade::new), "Machine upgrade"
    );
}
