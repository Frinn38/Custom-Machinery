package fr.frinn.custommachinery.common.init;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class StructureCreatorItem extends Item {

    private static final NamedCodec<List<List<String>>> PATTERN_CODEC = NamedCodec.STRING.listOf().listOf();
    private static final NamedCodec<Map<Character, PartialBlockState>> KEYS_CODEC = NamedCodec.unboundedMap(DefaultCodecs.CHARACTER, PartialBlockState.CODEC, "Map<Character, Block>");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        if(state.getBlock() instanceof CustomMachineBlock) {
            if(!context.getLevel().isClientSide())
                finishStructure(stack, pos, state.getValue(BlockStateProperties.HORIZONTAL_FACING), (ServerPlayer) context.getPlayer());
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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int amount = getSelectedBlocks(stack).size();
        if(amount <= 0)
            tooltip.add(Component.translatable("custommachinery.structure_creator.no_blocks").withStyle(ChatFormatting.RED));
        else
            tooltip.add(Component.translatable("custommachinery.structure_creator.amount", getSelectedBlocks(stack).size()).withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("custommachinery.structure_creator.select").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("custommachinery.structure_creator.reset").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(player.isCrouching() && player.getItemInHand(hand).getItem() == this) {
            ItemStack stack = player.getItemInHand(hand);
            stack.remove(Registration.STRUCTURE_CREATOR_DATA);
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }

    public static List<BlockPos> getSelectedBlocks(ItemStack stack) {
        return Optional.ofNullable(stack.get(Registration.STRUCTURE_CREATOR_DATA)).orElse(new ArrayList<>());
    }

    public static void addSelectedBlock(ItemStack stack, BlockPos pos) {
        stack.update(Registration.STRUCTURE_CREATOR_DATA, new ArrayList<>(), list -> {
            list.add(pos);
            return list;
        });
    }

    public static void removeSelectedBlock(ItemStack stack, BlockPos pos) {
        stack.update(Registration.STRUCTURE_CREATOR_DATA, new ArrayList<>(), list -> {
            list.remove(pos);
            return list;
        });
    }

    private void finishStructure(ItemStack stack, BlockPos machinePos, Direction machineFacing, ServerPlayer player) {
        List<BlockPos> blocks = getSelectedBlocks(stack);
        blocks.add(machinePos);
        if(blocks.size() <= 1) {
            player.sendSystemMessage(Component.translatable("custommachinery.structure_creator.no_blocks"));
            return;
        }
        Level world = player.level();

        PartialBlockState[][][] states = getStructureArray(blocks, machineFacing, world);
        HashBiMap<Character, PartialBlockState> keys = HashBiMap.create();
        AtomicInteger charIndex = new AtomicInteger(97);
        Arrays.stream(states)
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .filter(state -> !(state.getBlockState().getBlock() instanceof CustomMachineBlock) && state != PartialBlockState.ANY)
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
                    if(partial.getBlockState().getBlock() instanceof CustomMachineBlock)
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

        JsonObject both = new JsonObject();
        both.add("keys", keysJson);
        both.add("pattern", patternJson);
        String ctKubeString = ".requireStructure(" + patternJson.toString() + ", " + keysJson.toString() + ")";
        Component jsonText = Component.literal("[JSON]").withStyle(style -> style.applyFormats(ChatFormatting.YELLOW).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(both.toString()))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, both.toString())));
        Component prettyJsonText = Component.literal("[PRETTY JSON]").withStyle(style -> style.applyFormats(ChatFormatting.GOLD).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(GSON.toJson(both)))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, GSON.toJson(both))));
        Component crafttweakerText = Component.literal("[CRAFTTWEAKER]").withStyle(style -> style.applyFormats(ChatFormatting.AQUA).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(ctKubeString))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ctKubeString)));
        Component kubeJSText = Component.literal("[KUBEJS]").withStyle(style -> style.applyFormats(ChatFormatting.DARK_PURPLE).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(ctKubeString))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ctKubeString)));
        Component message = Component.translatable("custommachinery.structure_creator.message", jsonText, prettyJsonText, crafttweakerText, kubeJSText);
        player.sendSystemMessage(message);
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
                partial = new PartialBlockState(state, Lists.newArrayList(state.getProperties()), Optional.ofNullable(world.getBlockEntity(p)).map(be -> be.saveWithFullMetadata(world.registryAccess())).orElse(null));
                cache.put(state, partial);
            }
            //TODO: change "p.getY() - minY" to "maxY - p.getY()" in 1.18 to make the pattern from top to bottom instead of from bottom to top (current)
            switch (machineFacing) {
                case EAST -> states[p.getY() - minY][p.getX() - minX][maxZ - p.getZ()] = partial;
                case WEST -> states[p.getY() - minY][maxX - p.getX()][p.getZ() - minZ] = partial;
                case SOUTH -> states[p.getY() - minY][p.getZ() - minZ][p.getX() - minX] = partial;
                case NORTH -> states[p.getY() - minY][maxZ - p.getZ()][maxX - p.getX()] = partial;
            }
        });
        return states;
    }
}
