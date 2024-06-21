package fr.frinn.custommachinery.common.util;

import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        LootParams params = new LootParams.Builder(server.overworld()).create(Registration.CUSTOM_MACHINE_LOOT_PARAMETER_SET);
        LootContext context = new LootContext.Builder(params).create(Optional.empty());
        for (ResourceLocation table : tables) {
            List<Pair<ItemStack, Double>> loots = getLoots(table, server, context);
            lootsMap.put(table, loots);
        }
    }

    private static List<Pair<ItemStack, Double>> getLoots(ResourceLocation table, MinecraftServer server, LootContext context) {
        List<Pair<ItemStack, Double>> loots = new ArrayList<>();
        LootTable lootTable = server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, table));
        BiFunction<ItemStack, LootContext, ItemStack> globalFunction = lootTable.compositeFunction;
        List<LootPool> pools = getPoolsFromLootTable(lootTable);

        if(pools == null)
            return Collections.emptyList();

        for(LootPool pool : pools) {
            List<LootPoolEntryContainer> entries = pool.entries;
            float total = entries.stream().filter(entry -> entry instanceof LootPoolSingletonContainer).mapToInt(entry -> ((LootPoolSingletonContainer)entry).weight).sum();
            entries.stream().filter(entry -> entry instanceof LootItem)
                    .map(entry -> (LootItem)entry)
                    .forEach(entry -> {
                        Consumer<ItemStack> consumer = stack -> loots.add(Pair.of(stack, (double) (entry.weight / total)));
                        consumer = applyFunctions(consumer, entry.functions, globalFunction, context);
                        entry.createItemStack(consumer, context);
                    });

            entries.stream().filter(entry -> entry instanceof TagEntry)
                    .map(entry -> (TagEntry)entry)
                    .forEach(entry -> {
                        Consumer<ItemStack> consumer = stack -> loots.add(Pair.of(stack, (double) (entry.weight / total / (entry.expand ? TagUtil.getItems(entry.tag).count() : 1))));
                        consumer = applyFunctions(consumer, entry.functions, globalFunction, context);
                        entry.createItemStack(consumer, context);
                    });

            entries.stream().filter(entry -> entry instanceof NestedLootTable)
                    .map(entry -> (NestedLootTable)entry)
                    .map(entry -> getLoots(entry.contents.map(ResourceKey::location, LootTable::getLootTableId), server, context))
                    .forEach(loots::addAll);
        }
        return loots;
    }

    private static Consumer<ItemStack> applyFunctions(Consumer<ItemStack> consumer, List<LootItemFunction> functions, BiFunction<ItemStack, LootContext, ItemStack> globalFunction, LootContext context) {
        for(LootItemFunction function : functions)
            consumer = LootItemFunction.decorate(function, consumer, context);
        return LootItemFunction.decorate(globalFunction, consumer, context);
    }

    public static Map<ResourceLocation, List<Pair<ItemStack, Double>>> getLoots() {
        return lootsMap;
    }

    public static void receiveLoots(Map<ResourceLocation, List<Pair<ItemStack, Double>>> newLoots) {
        lootsMap = newLoots;
    }

    public static List<Pair<ItemStack, Double>> getLootsForTable(ResourceLocation table) {
        return lootsMap.getOrDefault(table, Collections.emptyList());
    }

    public static List<LootPool> getPoolsFromLootTable(LootTable table) {
        for(Field field : LootTable.class.getDeclaredFields()) {
            if(field.getName().equals("e") || field.getName().equals("f_79109_") || field.getName().equals("pools")) {
                field.setAccessible(true);
                try {
                    return (List<LootPool>) field.get(table);
                } catch (IllegalAccessException ignored) {

                }
            }
        }
        throw new RuntimeException("NOPE");
    }
}
