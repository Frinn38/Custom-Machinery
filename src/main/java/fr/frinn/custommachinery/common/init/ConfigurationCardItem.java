package fr.frinn.custommachinery.common.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ConfigurationCardItem extends Item {

    public ConfigurationCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        if(player == null)
            return InteractionResult.FAIL;

        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        if(!(level.getBlockEntity(pos) instanceof CustomMachineTile machine))
            return InteractionResult.FAIL;

        return copyConfiguration(level, player, machine, player.getItemInHand(context.getHand()));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if(!level.isClientSide() && player.isCrouching() && stack.has(CMRegistration.CONFIGURATION_CARD_DATA)) {
            stack.remove(CMRegistration.CONFIGURATION_CARD_DATA);
            player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.cleared").withStyle(ChatFormatting.GREEN));
            return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        getMachineId(stack)
                .flatMap(id -> Optional.ofNullable(CustomMachinery.MACHINES.get(id)))
                .ifPresent(machine -> builder.accept(Component.translatable("custommachinery.configuration_card.configured", machine.getName()).withStyle(ChatFormatting.AQUA)));

        builder.accept(Component.translatable("custommachinery.configuration_card.copy").withStyle(ChatFormatting.GREEN));
        builder.accept(Component.translatable("custommachinery.configuration_card.paste").withStyle(ChatFormatting.GREEN));
        builder.accept(Component.translatable("custommachinery.configuration_card.reset").withStyle(ChatFormatting.GOLD));
    }

    private InteractionResult copyConfiguration(Level level, Player player, CustomMachineTile machine, ItemStack stack) {
        if(!level.isClientSide() && player.isCrouching()) {
            ConfigurationCardData data = new ConfigurationCardData(machine.getId(), new HashMap<>());

            for(ISideConfigComponent component : machine.getComponentManager().getConfigComponents()) {
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, Provider.create(level.registryAccess().listRegistries()));
                component.getConfig().serialize(output);
                data.configs().put(component.getId(), output.buildResult());
            }

            stack.set(CMRegistration.CONFIGURATION_CARD_DATA, data);

            player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.copied", machine.getMachine().getName()).withStyle(ChatFormatting.GREEN));
        }

        return InteractionResult.SUCCESS;
    }

    public static InteractionResult pasteConfiguration(Level level, Player player, CustomMachineTile machine, ItemStack stack) {
        if(!level.isClientSide()) {
            Optional<Identifier> machineId = getMachineId(stack);

            if(!machineId.map(id -> id.equals(machine.getId())).orElse(false)) {
                player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.different_machine").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            for(ISideConfigComponent component : machine.getComponentManager().getConfigComponents())
                deserializeSideConfig(stack, component);

            player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.pasted", machine.getMachine().getName()).withStyle(ChatFormatting.GREEN));
        }

        return InteractionResult.SUCCESS;
    }

    private static Optional<Identifier> getMachineId(ItemStack stack) {
        return Optional.ofNullable(stack.get(CMRegistration.CONFIGURATION_CARD_DATA)).map(ConfigurationCardData::machineId);
    }

    private static void deserializeSideConfig(ItemStack stack, ISideConfigComponent component) {
        ConfigurationCardData data = stack.get(CMRegistration.CONFIGURATION_CARD_DATA);
        if(data != null && data.configs().containsKey(component.getId()))
            component.getConfig().deserialize(TagValueInput.create(ProblemReporter.DISCARDING, component.getManager().getLevel().registryAccess(), data.configs().get(component.getId())));
    }

    public record ConfigurationCardData(Identifier machineId, Map<String, CompoundTag> configs) {
        public static final Codec<ConfigurationCardData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("machineId").forGetter(ConfigurationCardData::machineId),
                        Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC).fieldOf("config").forGetter(ConfigurationCardData::configs)
                ).apply(instance, ConfigurationCardData::new)
        );
        public static final StreamCodec<ByteBuf, ConfigurationCardData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    }
}
