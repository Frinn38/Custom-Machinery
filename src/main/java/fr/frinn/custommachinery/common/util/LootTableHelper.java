package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.TableLootEntry;
import net.minecraft.loot.TagLootEntry;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class LootTableHelper {

    private static final List<ResourceLocation> tables = new ArrayList<>();
    private static Map<ResourceLocation, List<Pair<ItemStack, Double>>> lootsMap = new HashMap<>();

    public static void addTable(ResourceLocation table) {
        if(!tables.contains(table))
            tables.add(table);
    }

    public static void generate(MinecraftServer server) {
        lootsMap.clear();
        LootContext context = new LootContext.Builder(server.getWorld(World.OVERWORLD)).build(Registration.CUSTOM_MACHINE_LOOT_PARAMETER_SET);
        for (ResourceLocation table : tables) {
            List<Pair<ItemStack, Double>> loots = getLoots(table, server, context);
            lootsMap.put(table, loots);
        }
    }

    private static List<Pair<ItemStack, Double>> getLoots(ResourceLocation table, MinecraftServer server, LootContext context) {
        List<Pair<ItemStack, Double>> loots = new ArrayList<>();
        LootTable lootTable = server.getLootTableManager().getLootTableFromLocation(table);
        BiFunction<ItemStack, LootContext, ItemStack> globalFunction = ObfuscationReflectionHelper.getPrivateValue(LootTable.class, lootTable, "field_216129_g");
        List<LootPool> pools = ObfuscationReflectionHelper.getPrivateValue(LootTable.class, lootTable, "field_186466_c");

        for(LootPool pool : pools) {
            List<LootEntry> entries = ObfuscationReflectionHelper.getPrivateValue(LootPool.class, pool, "field_186453_a");
            float total = entries.stream().filter(entry -> entry instanceof StandaloneLootEntry).mapToInt(entry -> ((StandaloneLootEntry)entry).weight).sum();
            entries.stream().filter(entry -> entry instanceof ItemLootEntry)
                    .map(entry -> (ItemLootEntry)entry)
                    .forEach(entry -> {
                        Consumer<ItemStack> consumer = stack -> loots.add(Pair.of(stack, (double) (entry.weight / total)));
                        consumer = applyFunctions(consumer, entry.functions, globalFunction, context);
                        entry.func_216154_a(consumer, context);
                    });

            entries.stream().filter(entry -> entry instanceof TagLootEntry)
                    .map(entry -> (TagLootEntry)entry)
                    .forEach(entry -> {
                        Consumer<ItemStack> consumer = stack -> loots.add(Pair.of(stack, (double) (entry.weight / total / (entry.expand ? entry.tag.getAllElements().size() : 1))));
                        consumer = applyFunctions(consumer, entry.functions, globalFunction, context);
                        entry.func_216154_a(consumer, context);
                    });

            entries.stream().filter(entry -> entry instanceof TableLootEntry)
                    .map(entry -> (TableLootEntry)entry)
                    .map(entry -> getLoots(entry.table, server, context))
                    .forEach(loots::addAll);
        }
        return loots;
    }

    private static Consumer<ItemStack> applyFunctions(Consumer<ItemStack> consumer, ILootFunction[] functions, BiFunction<ItemStack, LootContext, ItemStack> globalFunction, LootContext context) {
        for(ILootFunction function : functions)
            consumer = ILootFunction.func_215858_a(function, consumer, context);
        return ILootFunction.func_215858_a(globalFunction, consumer, context);
    }

    public static Map<ResourceLocation, List<Pair<ItemStack, Double>>> getLoots() {
        return lootsMap;
    }

    public static void receiveLoots(Map<ResourceLocation, List<Pair<ItemStack, Double>>> newLoots) {
        lootsMap = newLoots;
    }

    public static List<Pair<ItemStack, Double>> getLootsForTable(ResourceLocation table) {
        return lootsMap.get(table);
    }
}
