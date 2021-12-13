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
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StructureCreatorItem extends Item {

    private static final Codec<List<List<String>>> PATTERN_CODEC = Codec.STRING.listOf().listOf();
    private static final Codec<Map<Character, PartialBlockState>> KEYS_CODEC = Codec.unboundedMap(Codecs.CHARACTER_CODEC, Codecs.PARTIAL_BLOCK_STATE_CODEC);

    public StructureCreatorItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if(context.getPlayer() == null)
            return ActionResultType.FAIL;
        if(context.getWorld().isRemote())
            return super.onItemUse(context);
        BlockPos pos = context.getPos();
        BlockState state = context.getWorld().getBlockState(pos);
        ItemStack stack = context.getItem();
        if(state.matchesBlock(Registration.CUSTOM_MACHINE_BLOCK.get())) {
            finishStructure(stack, pos, state.get(BlockStateProperties.HORIZONTAL_FACING), context.getPlayer());
            return ActionResultType.SUCCESS;
        } else if(!getSelectedBlocks(stack).contains(pos)) {
            addSelectedBlock(stack, pos);
            return ActionResultType.SUCCESS;
        } else if(getSelectedBlocks(stack).contains(pos)){
            removeSelectedBlock(stack, pos);
            return ActionResultType.SUCCESS;
        }
        return super.onItemUse(context);
    }

    public static List<BlockPos> getSelectedBlocks(ItemStack stack) {
        return Arrays.stream(stack.getOrCreateChildTag(CustomMachinery.MODID).getLongArray("blocks")).mapToObj(BlockPos::fromLong).collect(Collectors.toList());
    }

    public static void addSelectedBlock(ItemStack stack, BlockPos pos) {
        long packed = pos.toLong();
        long[] posList = stack.getOrCreateChildTag(CustomMachinery.MODID).getLongArray("blocks");
        long[] newList = new long[posList.length + 1];
        System.arraycopy(posList, 0, newList, 0, posList.length);
        newList[posList.length] = packed;
        stack.getOrCreateChildTag(CustomMachinery.MODID).putLongArray("blocks", newList);
    }

    public static void removeSelectedBlock(ItemStack stack, BlockPos pos) {
        long packed = pos.toLong();
        long[] posList = stack.getOrCreateChildTag(CustomMachinery.MODID).getLongArray("blocks");
        long[] newList = Arrays.stream(posList).filter(l -> l != packed).toArray();
        stack.getOrCreateChildTag(CustomMachinery.MODID).putLongArray("blocks", newList);
    }

    private void finishStructure(ItemStack stack, BlockPos machinePos, Direction machineFacing, PlayerEntity player) {
        List<BlockPos> blocks = getSelectedBlocks(stack);
        blocks.add(machinePos);
        if(blocks.size() <= 1) {
            player.sendMessage(new TranslationTextComponent("custommachinery.structure_creator.no_blocks"), Util.DUMMY_UUID);
            return;
        }
        World world = player.world;

        PartialBlockState[][][] states = getStructureArray(blocks, machineFacing, world);
        HashBiMap<Character, PartialBlockState> keys = HashBiMap.create();
        AtomicInteger charIndex = new AtomicInteger(97);
        Arrays.stream(states)
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .filter(state -> !state.getBlockState().matchesBlock(Registration.CUSTOM_MACHINE_BLOCK.get()) && state != PartialBlockState.ANY)
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
                    if(partial.getBlockState().matchesBlock(Registration.CUSTOM_MACHINE_BLOCK.get()))
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
        NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new SStructureCreatorPacket(keysJson, patternJson));
    }

    private PartialBlockState[][][] getStructureArray(List<BlockPos> blocks, Direction machineFacing, World world) {
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
        AxisAlignedBB box = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        Map<BlockState, PartialBlockState> cache = new HashMap<>();
        BlockPos.getAllInBox(box).forEach(p -> {
            BlockState state = world.getBlockState(p);
            PartialBlockState partial;
            if(!blocks.contains(p))
                partial = PartialBlockState.ANY;
            else if(cache.containsKey(state))
                partial = cache.get(state);
            else {
                partial = new PartialBlockState(state, Lists.newArrayList(state.getProperties()), Optional.ofNullable(world.getTileEntity(p)).map(tile -> tile.write(new CompoundNBT())).orElse(null));
                cache.put(state, partial);
            }
            //TODO: change "p.getY() - minY" to "maxY - p.getY()" in 1.18 to make the pattern from top to bottom instead of from bottom to top (current)
            switch (machineFacing) {
                case WEST:
                    states[p.getY() - minY][p.getX() - minX][p.getZ() - minZ] = partial;
                    break;
                case EAST:
                    states[p.getY() - minY][maxX - p.getX()][maxZ - p.getZ()] = partial;
                    break;
                case SOUTH:
                    states[p.getY() - minY][p.getZ() - minZ][p.getX() - minX] = partial;
                    break;
                case NORTH:
                    states[p.getY() - minY][maxZ - p.getZ()][maxX - p.getX()] = partial;
                    break;
            }
        });
        return states;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("custommachinery.structure_creator.amount", getSelectedBlocks(stack).size()));
        tooltip.add(new TranslationTextComponent("custommachinery.structure_creator.reset"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if(player.isCrouching() && player.getHeldItem(hand).getItem() == this) {
            ItemStack stack = player.getHeldItem(hand);
            stack.getOrCreateChildTag(CustomMachinery.MODID).remove("blocks");
            return ActionResult.resultSuccess(stack);
        }
        return super.onItemRightClick(world, player, hand);
    }
}
