package fr.frinn.custommachinery.common.init;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.ISideConfigComponent;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
        var player = context.getPlayer();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        var pos = context.getClickedPos();
        var level = context.getLevel();

        if (!(level.getBlockEntity(pos) instanceof CustomMachineTile machine)) {
            return InteractionResult.FAIL;
        }

        return copyConfiguration(level, player, machine, player.getItemInHand(context.getHand()));
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (!level.isClientSide && player.isCrouching()) {
            stack.removeTagKey(CustomMachinery.MODID);

            player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.cleared").withStyle(ChatFormatting.GREEN));

            return InteractionResultHolder.success(stack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (hasMachineId(stack)) {
            var machine = Optional
                    .ofNullable(CustomMachinery.MACHINES.get(getMachineId(stack)))
                    .orElse(CustomMachine.DUMMY);

            tooltip.add(Component.translatable("custommachinery.configuration_card.configured", machine.getName()).withStyle(ChatFormatting.AQUA));
        }

        tooltip.add(Component.translatable("custommachinery.configuration_card.copy").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("custommachinery.configuration_card.paste").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("custommachinery.configuration_card.reset").withStyle(ChatFormatting.GOLD));
    }

    private InteractionResult copyConfiguration(Level level, Player player, CustomMachineTile machine, ItemStack stack) {
        if (!level.isClientSide && player.isCrouching()) {
            setMachineId(stack, machine.getId());

            for (var component : machine.getComponentManager().getConfigComponents()) {
                serializeSideConfig(stack, component);
            }

            player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.copied").withStyle(ChatFormatting.GREEN));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static InteractionResult pasteConfiguration(Level level, Player player, CustomMachineTile machine, ItemStack stack) {
        if (!level.isClientSide) {
            var machineId = getMachineId(stack);

            if (machineId.getPath().isEmpty()) {
                return InteractionResult.FAIL;
            }
            if (!machineId.equals(machine.getId())) {
                player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.different_machine").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            for (var component : machine.getComponentManager().getConfigComponents()) {
                deserializeSideConfig(stack, component);
            }

            player.sendSystemMessage(Component.translatable("custommachinery.configuration_card.pasted").withStyle(ChatFormatting.GREEN));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static CompoundTag getDataHolder(ItemStack stack) {
        return stack.getOrCreateTagElement(CustomMachinery.MODID);
    }

    private static boolean hasMachineId(ItemStack stack) {
        return getDataHolder(stack).contains(MACHINE_ID);
    }

    private static ResourceLocation getMachineId(ItemStack stack) {
        return new ResourceLocation(getDataHolder(stack).getString(MACHINE_ID));
    }

    private static void setMachineId(ItemStack stack, ResourceLocation id) {
        getDataHolder(stack).putString(MACHINE_ID, id.toString());
    }

    private static void deserializeSideConfig(ItemStack stack, ISideConfigComponent component) {
        var dataHolder = getDataHolder(stack);
        var sideConfig = dataHolder.getCompound(SIDE_CONFIG);

        if (sideConfig.contains(component.getId())) {
            component.getConfig().deserialize(sideConfig.get(component.getId()));
        }
    }

    private static void serializeSideConfig(ItemStack stack, ISideConfigComponent component) {
        var dataHolder = getDataHolder(stack);

        if (!dataHolder.contains(SIDE_CONFIG)) {
            dataHolder.put(SIDE_CONFIG, new CompoundTag());
        }

        var sideConfig = dataHolder.getCompound(SIDE_CONFIG);

        sideConfig.put(component.getId(), component.getConfig().serialize());
    }
}
