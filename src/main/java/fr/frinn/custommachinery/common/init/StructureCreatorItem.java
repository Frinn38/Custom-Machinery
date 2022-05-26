package fr.frinn.custommachinery.common.init;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SStructureCreatorPacket;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StructureCreatorItem extends Item {

    private static final Codec<List<List<String>>> PATTERN_CODEC = Codec.STRING.listOf().listOf();
    private static final Codec<Map<Character, PartialBlockState>> KEYS_CODEC = Codec.unboundedMap(Codecs.CHARACTER_CODEC, Codecs.PARTIAL_BLOCK_STATE_CODEC);

    public StructureCreatorItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(context.getPlayer() == null)
            return InteractionResult.FAIL;
        if(context.getLevel().isClientSide())
            return super.useOn(context);
        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        if(state.is(Registration.CUSTOM_MACHINE_BLOCK.get())) {
            finishStructure(stack, pos, state.getValue(BlockStateProperties.HORIZONTAL_FACING), context.getPlayer());
            return InteractionResult.SUCCESS;
        } else if(!getSelectedBlocks(stack).contains(pos)) {
            addSelectedBlock(stack, pos);
            return InteractionResult.SUCCESS;
        } else if(getSelectedBlocks(stack).contains(pos)){
            removeSelectedBlock(stack, pos);
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    public static List<BlockPos> getSelectedBlocks(ItemStack stack) {
        return Arrays.stream(stack.getOrCreateTagElement(CustomMachinery.MODID).getLongArray("blocks")).mapToObj(BlockPos::of).toList();
    }

    public static void addSelectedBlock(ItemStack stack, BlockPos pos) {
        long packed = pos.asLong();
        long[] posList = stack.getOrCreateTagElement(CustomMachinery.MODID).getLongArray("blocks");
        long[] newList = new long[posList.length + 1];
        System.arraycopy(posList, 0, newList, 0, posList.length);
        newList[posList.length] = packed;
        stack.getOrCreateTagElement(CustomMachinery.MODID).putLongArray("blocks", newList);
    }

    public static void removeSelectedBlock(ItemStack stack, BlockPos pos) {
        long packed = pos.asLong();
        long[] posList = stack.getOrCreateTagElement(CustomMachinery.MODID).getLongArray("blocks");
        long[] newList = Arrays.stream(posList).filter(l -> l != packed).toArray();
        stack.getOrCreateTagElement(CustomMachinery.MODID).putLongArray("blocks", newList);
    }

    private void finishStructure(ItemStack stack, BlockPos machinePos, Direction machineFacing, Player player) {
        List<BlockPos> blocks = getSelectedBlocks(stack);
        blocks.add(machinePos);
        if(blocks.size() <= 1) {
            player.sendMessage(new TranslatableComponent("custommachinery.structure_creator.no_blocks"), Util.NIL_UUID);
            return;
        }
        Level world = player.level;

        PartialBlockState[][][] states = getStructureArray(blocks, machineFacing, world);
        HashBiMap<Character, PartialBlockState> keys = HashBiMap.create();
        AtomicInteger charIndex = new AtomicInteger(97);
        Arrays.stream(states)
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .filter(state -> !state.getBlockState().is(Registration.CUSTOM_MACHINE_BLOCK.get()) && state != PartialBlockState.ANY)
                .distinct()
                .forEach(state -> {
                    if(charIndex.get() == 109) charIndex.incrementAndGet(); //Avoid 'm' as it's reserved for the machine.
                    keys.put((char)charIndex.getAndIncrement(), state);
                    if(charIndex.get() == 122) charIndex.set(65); //All lowercase are used, so switch to uppercase.
                });
        List<List<String>> pattern = new ArrayList<>();
        for(int i = 0; i < states.length; i++) {
            List<String> floor = new ArrayList<>();
            for(int j = 0; j < states[i].length; j++) {
                StringBuilder row = new StringBuilder();
                for (int k = 0; k < states[i][j].length; k++) {
                    PartialBlockState partial = states[i][j][k];
                    char key;
                    if(partial.getBlockState().is(Registration.CUSTOM_MACHINE_BLOCK.get()))
                        key = 'm';
                    else if(partial == PartialBlockState.ANY)
                        key = ' ';
                    else if(keys.containsValue(partial))
                        key = keys.inverse().get(partial);
                    else
                        key = '?';
                    row.append(key);
                }
                floor.add(row.toString());
            }
            pattern.add(floor);
        }
        JsonElement keysJson = KEYS_CODEC.encodeStart(JsonOps.INSTANCE, keys).result().orElseThrow(IllegalStateException::new);
        JsonElement patternJson = PATTERN_CODEC.encodeStart(JsonOps.INSTANCE, pattern).result().orElseThrow(IllegalStateException::new);
        NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new SStructureCreatorPacket(keysJson, patternJson));
    }

    private PartialBlockState[][][] getStructureArray(List<BlockPos> blocks, Direction machineFacing, Level world) {
        int minX = blocks.stream().mapToInt(BlockPos::getX).min().orElseThrow(IllegalStateException::new);
        int maxX = blocks.stream().mapToInt(BlockPos::getX).max().orElseThrow(IllegalStateException::new);
        int minY = blocks.stream().mapToInt(BlockPos::getY).min().orElseThrow(IllegalStateException::new);
        int maxY = blocks.stream().mapToInt(BlockPos::getY).max().orElseThrow(IllegalStateException::new);
        int minZ = blocks.stream().mapToInt(BlockPos::getZ).min().orElseThrow(IllegalStateException::new);
        int maxZ = blocks.stream().mapToInt(BlockPos::getZ).max().orElseThrow(IllegalStateException::new);
        PartialBlockState[][][] states;
        if(machineFacing.getAxis() == Direction.Axis.X)
            states = new PartialBlockState[maxY - minY + 1][maxX - minX + 1][maxZ - minZ + 1];
        else
            states = new PartialBlockState[maxY - minY + 1][maxZ - minZ + 1][maxX - minX + 1];
        AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        Map<BlockState, PartialBlockState> cache = new HashMap<>();
        BlockPos.betweenClosedStream(box).forEach(p -> {
            BlockState state = world.getBlockState(p);
            PartialBlockState partial;
            if(!blocks.contains(p))
                partial = PartialBlockState.ANY;
            else if(cache.containsKey(state))
                partial = cache.get(state);
            else {
                partial = new PartialBlockState(state, Lists.newArrayList(state.getProperties()), Optional.ofNullable(world.getBlockEntity(p)).map(BlockEntity::saveWithFullMetadata).orElse(null));
                cache.put(state, partial);
            }
            //TODO: change "p.getY() - minY" to "maxY - p.getY()" in 1.18 to make the pattern from top to bottom instead of from bottom to top (current)
            switch (machineFacing) {
                case WEST -> states[p.getY() - minY][p.getX() - minX][p.getZ() - minZ] = partial;
                case EAST -> states[p.getY() - minY][maxX - p.getX()][maxZ - p.getZ()] = partial;
                case SOUTH -> states[p.getY() - minY][p.getZ() - minZ][p.getX() - minX] = partial;
                case NORTH -> states[p.getY() - minY][maxZ - p.getZ()][maxX - p.getX()] = partial;
            }
        });
        return states;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TranslatableComponent("custommachinery.structure_creator.amount", getSelectedBlocks(stack).size()));
        tooltip.add(new TranslatableComponent("custommachinery.structure_creator.reset"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if(player.isCrouching() && player.getItemInHand(hand).getItem() == this) {
            ItemStack stack = player.getItemInHand(hand);
            stack.getOrCreateTagElement(CustomMachinery.MODID).remove("blocks");
            return InteractionResultHolder.success(stack);
        }
        return super.use(world, player, hand);
    }
}
