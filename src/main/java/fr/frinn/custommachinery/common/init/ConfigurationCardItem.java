package fr.frinn.custommachinery.common.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigurationCardItem extends Item {
    private static final String MACHINE_ID = "machineId";
    private static final String SIDE_CONFIG = "sideConfig";

    public ConfigurationCardItem(Properties properties) {
        super(properties);
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        if (!(level.getBlockEntity(pos) instanceof CustomMachineTile machine))
            return InteractionResult.FAIL;

        return copyConfiguration(level, player, machine, player.getItemInHand(context.getHand()));
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player.isCrouching()) {
            stack.remove(Registration.CONFIGURATION_CARD_DATA);

            player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.cleared").withStyle(ChatFormatting.GREEN));

            return InteractionResultHolder.success(stack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        getMachineId(stack)
                .flatMap(id -> Optional.ofNullable(CustomMachinery.MACHINES.get(id)))
                .ifPresent(machine -> {
                    tooltip.add(Component.translatable("custommachinery.configuration_card.configured", machine.getName()).withStyle(ChatFormatting.AQUA));
                });

        tooltip.add(Component.translatable("custommachinery.configuration_card.copy").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("custommachinery.configuration_card.paste").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("custommachinery.configuration_card.reset").withStyle(ChatFormatting.GOLD));
    }

    private InteractionResult copyConfiguration(Level level, Player player, CustomMachineTile machine, ItemStack stack) {
        if (!level.isClientSide && player.isCrouching()) {
            setMachineId(stack, machine.getId());

            for (ISideConfigComponent component : machine.getComponentManager().getConfigComponents())
                serializeSideConfig(stack, component);

            player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.copied").withStyle(ChatFormatting.GREEN));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static ItemInteractionResult pasteConfiguration(Level level, Player player, CustomMachineTile machine, ItemStack stack) {
        if (!level.isClientSide) {
            Optional<ResourceLocation> machineId = getMachineId(stack);

            if (!machineId.map(id -> id.equals(machine.getId())).orElse(false)) {
                player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.different_machine").withStyle(ChatFormatting.RED));
                return ItemInteractionResult.FAIL;
            }

            for (ISideConfigComponent component : machine.getComponentManager().getConfigComponents())
                deserializeSideConfig(stack, component);

            player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.pasted").withStyle(ChatFormatting.GREEN));
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static Optional<ResourceLocation> getMachineId(ItemStack stack) {
        return Optional.ofNullable(stack.get(Registration.CONFIGURATION_CARD_DATA)).map(ConfigurationCardData::machineId);
    }

    private static void setMachineId(ItemStack stack, ResourceLocation id) {
        stack.update(Registration.CONFIGURATION_CARD_DATA, ConfigurationCardData.EMPTY, data -> new ConfigurationCardData(id, data.configs()));
    }

    private static void deserializeSideConfig(ItemStack stack, ISideConfigComponent component) {
        ConfigurationCardData data = stack.get(Registration.CONFIGURATION_CARD_DATA);
        if(data != null && data.configs().containsKey(component.getId()))
            component.getConfig().deserialize(data.configs().get(component.getId()));
    }

    private static void serializeSideConfig(ItemStack stack, ISideConfigComponent component) {
        stack.update(Registration.CONFIGURATION_CARD_DATA, ConfigurationCardData.EMPTY, data -> {
            data.configs().put(component.getId(), component.getConfig().serialize());
            return data;
        });
    }

    public record ConfigurationCardData(ResourceLocation machineId, Map<String, CompoundTag> configs) {
        public static final Codec<ConfigurationCardData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ResourceLocation.CODEC.fieldOf("machineId").forGetter(ConfigurationCardData::machineId),
                        Codec.unboundedMap(Codec.STRING, CompoundTag.CODEC).fieldOf("config").forGetter(ConfigurationCardData::configs)
                ).apply(instance, ConfigurationCardData::new)
        );
        public static final StreamCodec<ByteBuf, ConfigurationCardData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
        public static final ConfigurationCardData EMPTY = new ConfigurationCardData(CustomMachine.DUMMY_ID, new HashMap<>());
    }
}
