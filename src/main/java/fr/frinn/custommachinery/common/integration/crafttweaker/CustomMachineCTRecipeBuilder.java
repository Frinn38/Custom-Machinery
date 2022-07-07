package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.base.IData;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.tag.MCTag;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.requirement.IChanceableRequirement;
import fr.frinn.custommachinery.api.requirement.IDelayedRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipeBuilder;
import fr.frinn.custommachinery.common.integration.crafttweaker.function.CTFunction;
import fr.frinn.custommachinery.common.integration.crafttweaker.function.Context;
import fr.frinn.custommachinery.common.requirement.BiomeRequirement;
import fr.frinn.custommachinery.common.requirement.BlockRequirement;
import fr.frinn.custommachinery.common.requirement.CommandRequirement;
import fr.frinn.custommachinery.common.requirement.DimensionRequirement;
import fr.frinn.custommachinery.common.requirement.DropRequirement;
import fr.frinn.custommachinery.common.requirement.DurabilityRequirement;
import fr.frinn.custommachinery.common.requirement.EffectRequirement;
import fr.frinn.custommachinery.common.requirement.EnergyPerTickRequirement;
import fr.frinn.custommachinery.common.requirement.EnergyRequirement;
import fr.frinn.custommachinery.common.requirement.EntityRequirement;
import fr.frinn.custommachinery.common.requirement.FluidPerTickRequirement;
import fr.frinn.custommachinery.common.requirement.FluidRequirement;
import fr.frinn.custommachinery.common.requirement.FuelRequirement;
import fr.frinn.custommachinery.common.requirement.FunctionRequirement;
import fr.frinn.custommachinery.common.requirement.ItemRequirement;
import fr.frinn.custommachinery.common.requirement.LightRequirement;
import fr.frinn.custommachinery.common.requirement.LootTableRequirement;
import fr.frinn.custommachinery.common.requirement.PositionRequirement;
import fr.frinn.custommachinery.common.requirement.RedstoneRequirement;
import fr.frinn.custommachinery.common.requirement.StructureRequirement;
import fr.frinn.custommachinery.common.requirement.TimeRequirement;
import fr.frinn.custommachinery.common.requirement.WeatherRequirement;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.PositionComparator;
import fr.frinn.custommachinery.common.util.TimeComparator;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.FluidIngredient;
import fr.frinn.custommachinery.common.util.ingredient.FluidTagIngredient;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemTagIngredient;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Optional;
import org.openzen.zencode.java.ZenCodeType.OptionalBoolean;
import org.openzen.zencode.java.ZenCodeType.OptionalFloat;
import org.openzen.zencode.java.ZenCodeType.OptionalInt;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ZenRegister
@Name("mods.custommachinery.CMRecipeBuilder")
public class CustomMachineCTRecipeBuilder {

    private static int index = 0;
    private final CustomMachineRecipeBuilder builder;
    private IRequirement<?> lastRequirement;
    private boolean jei = false;

    public CustomMachineCTRecipeBuilder(CustomMachineRecipeBuilder builder) {
        this.builder = builder;
    }

    @Method
    public static CustomMachineCTRecipeBuilder create(String machine, int time) {
        try {
            return new CustomMachineCTRecipeBuilder(new CustomMachineRecipeBuilder(new ResourceLocation(machine), time));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid Machine name: " + machine + "\n" + e.getMessage());
        }
    }

    @Method
    public void build(@OptionalString String name) {
        final ResourceLocation recipeID;
        try {
            if(!name.isEmpty())
                recipeID = new ResourceLocation(CraftTweakerConstants.MOD_ID, name);
            else
                recipeID = new ResourceLocation(CraftTweakerConstants.MOD_ID, "custom_machine_recipe_" + index++);
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid Recipe name: " + name + "\n" + e.getMessage());
        }
        CustomMachineRecipe recipe = this.builder.build(recipeID);
        ActionAddRecipe<CustomMachineRecipe> action =  new ActionAddRecipe<>(CustomMachineryCTRecipeManager.INSTANCE, recipe);
        CraftTweakerAPI.apply(action);
    }

    /** ENERGY **/

    @Method
    public CustomMachineCTRecipeBuilder requireEnergy(int amount) {
        return withEnergyRequirement(RequirementIOMode.INPUT, amount, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder requireEnergyPerTick(int amount) {
        return withEnergyRequirement(RequirementIOMode.INPUT, amount, true);
    }

    @Method
    public CustomMachineCTRecipeBuilder produceEnergy(int amount) {
        return withEnergyRequirement(RequirementIOMode.OUTPUT, amount, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder produceEnergyPerTick(int amount) {
        return withEnergyRequirement(RequirementIOMode.OUTPUT, amount, true);
    }

    /** FLUIDS **/

    @Method
    public CustomMachineCTRecipeBuilder requireFluid(IFluidStack stack, @OptionalString String tank) {
        return withFluidRequirement(RequirementIOMode.INPUT, new FluidIngredient(stack.getFluid()), stack.getAmount(), false, stack.getInternal().getTag(), tank);
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFluidTag(MCTag tag, int amount, @Optional IData data, @OptionalString String tank) {
        try {
            return withFluidRequirement(RequirementIOMode.INPUT, FluidTagIngredient.create(tag.getTagKey()), amount, false, getNBT(data), tank);
        } catch (IllegalArgumentException e) {
            CraftTweakerAPI.LOGGER.error(e.getMessage());
            return this;
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder produceFluid(IFluidStack stack, @OptionalString String tank) {
        return withFluidRequirement(RequirementIOMode.OUTPUT, new FluidIngredient(stack.getFluid()), stack.getAmount(), false, stack.getInternal().getTag(), tank);
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFluidPerTick(IFluidStack stack, @OptionalString String tank) {
        return withFluidRequirement(RequirementIOMode.INPUT, new FluidIngredient(stack.getFluid()), stack.getAmount(), true, stack.getInternal().getTag(), tank);
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFluidTagPerTick(MCTag tag, int amount, @Optional IData data, @OptionalString String tank) {
        try {
            return withFluidRequirement(RequirementIOMode.INPUT, FluidTagIngredient.create(tag.getTagKey()), amount, true, getNBT(data), tank);
        } catch (IllegalArgumentException e) {
            CraftTweakerAPI.LOGGER.error(e.getMessage());
            return this;
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder produceFluidPerTick(IFluidStack stack, @OptionalString String tank) {
        return withFluidRequirement(RequirementIOMode.OUTPUT, new FluidIngredient(stack.getFluid()), stack.getAmount(), true, stack.getInternal().getTag(), tank);
    }

    /** ITEM **/

    @Method
    public CustomMachineCTRecipeBuilder requireItem(IItemStack stack, @OptionalString String slot) {
        return addRequirement(new ItemRequirement(RequirementIOMode.INPUT, new ItemIngredient(stack.getDefinition()), stack.getAmount(), nbtFromStack(stack), slot));
    }

    @Method
    public CustomMachineCTRecipeBuilder requireItemTag(MCTag tag, int amount, @Optional IData data, @OptionalString String slot) {
        try {
            return addRequirement(new ItemRequirement(RequirementIOMode.INPUT, ItemTagIngredient.create(tag.getTagKey()), amount, getNBT(data), slot));
        } catch (IllegalArgumentException e) {
            CraftTweakerAPI.LOGGER.error(e.getMessage());
            return this;
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder produceItem(IItemStack stack, @OptionalString String slot) {
        return addRequirement(new ItemRequirement(RequirementIOMode.OUTPUT, new ItemIngredient(stack.getDefinition()), stack.getAmount(), nbtFromStack(stack), slot));
    }

    /** DURABILITY **/

    @Method
    public CustomMachineCTRecipeBuilder damageItem(IItemStack stack, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, new ItemIngredient(stack.getDefinition()), amount, nbtFromStack(stack), true, slot));
    }

    @Method
    public CustomMachineCTRecipeBuilder damageItemNoBreak(IItemStack stack, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, new ItemIngredient(stack.getDefinition()), amount, nbtFromStack(stack), false, slot));
    }

    @Method
    public CustomMachineCTRecipeBuilder damageItemTag(MCTag tag, int amount, @Optional IData data, @OptionalString String slot) {
        try {
            return addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ItemTagIngredient.create(tag.getTagKey()), amount, getNBT(data), true, slot));
        } catch (IllegalArgumentException e) {
            CraftTweakerAPI.LOGGER.error(e.getMessage());
            return this;
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder damageItemTagNoBreak(MCTag tag, int amount, @Optional IData data, @OptionalString String slot) {
        try {
            return addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ItemTagIngredient.create(tag.getTagKey()), amount, getNBT(data), false, slot));
        } catch (IllegalArgumentException e) {
            CraftTweakerAPI.LOGGER.error(e.getMessage());
            return this;
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder repairItem(IItemStack stack, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(RequirementIOMode.OUTPUT, new ItemIngredient(stack.getDefinition()), amount, nbtFromStack(stack), false, slot));
    }

    @Method
    public CustomMachineCTRecipeBuilder repairItemTag(MCTag tag, int amount, @Optional IData data, @OptionalString String slot) {
        try {
            return addRequirement(new DurabilityRequirement(RequirementIOMode.OUTPUT, ItemTagIngredient.create(tag.getTagKey()), amount, getNBT(data), false, slot));
        } catch (IllegalArgumentException e) {
            CraftTweakerAPI.LOGGER.error(e.getMessage());
            return this;
        }
    }

    /** TIME **/

    @Method
    public CustomMachineCTRecipeBuilder requireTime(String[] times) {
        List<TimeComparator> timeComparators = Stream.of(times).map(s -> Codecs.TIME_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(CraftTweakerAPI.LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid time comparator: " + s)).getFirst()).toList();
        if(!timeComparators.isEmpty())
            return addRequirement(new TimeRequirement(timeComparators));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireTime(String time) {
        TimeComparator timeComparator = Codecs.TIME_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(time)).resultOrPartial(CraftTweakerAPI.LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid time comparator: " + time)).getFirst();
        return addRequirement(new TimeRequirement(Collections.singletonList(timeComparator)));
    }

    /** POSITION **/

    @Method
    public CustomMachineCTRecipeBuilder requirePosition(String[] positions) {
        List<PositionComparator> positionComparators = Stream.of(positions).map(s -> Codecs.POSITION_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(CraftTweakerAPI.LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid position comparator: " + s)).getFirst()).toList();
        if(!positionComparators.isEmpty())
            return addRequirement(new PositionRequirement(positionComparators));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requirePosition(String position) {
        PositionComparator positionComparator = Codecs.POSITION_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(position)).resultOrPartial(CraftTweakerAPI.LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid position comparator: " + position)).getFirst();
        return addRequirement(new PositionRequirement(Collections.singletonList(positionComparator)));
    }

    /** BIOME **/

    @Method
    public CustomMachineCTRecipeBuilder biomeWhitelist(Biome[] biomes) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).map(Biome::getRegistryName).toList();
        return addRequirement(new BiomeRequirement(biomesID, false));
    }

    @Method
    public CustomMachineCTRecipeBuilder biomeWhitelist(Biome biome) {
        return addRequirement(new BiomeRequirement(Collections.singletonList(biome.getRegistryName()), false));
    }

    @Method
    public CustomMachineCTRecipeBuilder biomeBlacklist(Biome[] biomes) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).map(Biome::getRegistryName).toList();
        return addRequirement(new BiomeRequirement(biomesID, true));
    }

    @Method
    public CustomMachineCTRecipeBuilder biomeBlacklist(Biome biome) {
        return addRequirement(new BiomeRequirement(Collections.singletonList(biome.getRegistryName()), true));
    }

    /** DIMENSION **/

    @Method
    public CustomMachineCTRecipeBuilder dimensionWhitelist(String[] dimensions) {
        try {
            List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).map(ResourceLocation::new).toList();
            return addRequirement(new DimensionRequirement(dimensionsID, false));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid dimension ID: " + e.getMessage());
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder dimensionWhitelist(String dimension) {
        try {
            return addRequirement(new DimensionRequirement(Collections.singletonList(new ResourceLocation(dimension)), false));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid dimensions ID: " + e.getMessage());
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder dimensionBlacklist(String[] dimensions) {
        try {
            List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).map(ResourceLocation::new).toList();
            return addRequirement(new DimensionRequirement(dimensionsID, false));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid dimension ID: " + e.getMessage());
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder dimensionBlacklist(String dimension) {
        try {
            return addRequirement(new DimensionRequirement(Collections.singletonList(new ResourceLocation(dimension)), false));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid dimensions ID: " + e.getMessage());
        }
    }

    /** FUEL **/

    @Method
    public CustomMachineCTRecipeBuilder requireFuel(@OptionalInt(1) int amount) {
        return addRequirement(new FuelRequirement(amount));
    }

    /** COMMAND **/

    @Method
    public CustomMachineCTRecipeBuilder runCommandOnStart(String command, @OptionalInt(2) int permissionLevel, @OptionalBoolean boolean log) {
        return addRequirement(new CommandRequirement(command, CraftingManager.PHASE.STARTING, permissionLevel, log));
    }

    @Method
    public CustomMachineCTRecipeBuilder runCommandEachTick(String command, @OptionalInt(2) int permissionLevel, @OptionalBoolean boolean log) {
        return addRequirement(new CommandRequirement(command, CraftingManager.PHASE.CRAFTING_TICKABLE, permissionLevel, log));
    }

    @Method
    public CustomMachineCTRecipeBuilder runCommandOnEnd(String command, @OptionalInt(2) int permissionLevel, @OptionalBoolean boolean log) {
        return addRequirement(new CommandRequirement(command, CraftingManager.PHASE.ENDING, permissionLevel, log));
    }

    /** EFFECT **/

    @Method
    public CustomMachineCTRecipeBuilder giveEffectOnEnd(MobEffect effect, int time, int radius, @OptionalInt(1) int level, @Optional EntityType[] filter) {
        return addRequirement(new EffectRequirement(effect, time, level, radius, Arrays.asList(filter), true));
    }

    @Method
    public CustomMachineCTRecipeBuilder giveEffectEachTick(MobEffect effect, int time, int radius, @OptionalInt(1) int level, @Optional EntityType[] filter) {
        return addRequirement(new EffectRequirement(effect, time, level, radius, Arrays.asList(filter), false));
    }

    /** WEATHER **/

    @Method
    public CustomMachineCTRecipeBuilder requireWeather(String weatherType) {
        return addRequirement(new WeatherRequirement(WeatherMachineComponent.WeatherType.value(weatherType), false));
    }

    @Method
    public CustomMachineCTRecipeBuilder requireWeatherOnMachine(String weatherType) {
        return addRequirement(new WeatherRequirement(WeatherMachineComponent.WeatherType.value(weatherType), true));
    }

    /** REDSTONE **/

    @Method
    public CustomMachineCTRecipeBuilder requireRedstone(int power, @OptionalString(">=") String comparator) {
        return addRequirement(new RedstoneRequirement(power, ComparatorMode.value(comparator)));
    }

    /** ENTITY **/

    @Method
    public CustomMachineCTRecipeBuilder requireEntities(int amount, int radius, @Optional EntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CHECK_AMOUNT, amount, radius, Arrays.asList(filter), whitelist));
    }

    @Method
    public CustomMachineCTRecipeBuilder requireEntitiesHealth(int amount, int radius, @Optional EntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CHECK_HEALTH, amount, radius, Arrays.asList(filter), whitelist));
    }

    @Method
    public CustomMachineCTRecipeBuilder consumeEntityHealthOnStart(int amount, int radius, @Optional EntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, Arrays.asList(filter), whitelist));
    }

    @Method
    public CustomMachineCTRecipeBuilder consumeEntityHealthOnEnd(int amount, int radius, @Optional EntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(RequirementIOMode.OUTPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, Arrays.asList(filter), whitelist));
    }

    @Method
    public CustomMachineCTRecipeBuilder killEntityOnStart(int amount, int radius, @Optional EntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.KILL, amount, radius, Arrays.asList(filter), whitelist));
    }

    @Method
    public CustomMachineCTRecipeBuilder killEntityOnEnd(int amount, int radius, @Optional EntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(RequirementIOMode.OUTPUT, EntityRequirement.ACTION.KILL, amount, radius, Arrays.asList(filter), whitelist));
    }

    /** LIGHT **/

    @Method
    public CustomMachineCTRecipeBuilder requireSkyLight(int level, @OptionalString(">=") String comparator) {
        return addRequirement(new LightRequirement(level, ComparatorMode.value(comparator), true));
    }

    @Method
    public CustomMachineCTRecipeBuilder requireBlockLight(int level, @OptionalString(">=") String comparator) {
        return addRequirement(new LightRequirement(level, ComparatorMode.value(comparator), false));
    }

    /** BLOCK **/

    @Method
    public CustomMachineCTRecipeBuilder requireBlock(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @OptionalString(">=") String comparator) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.CHECK, "", startX, startY, startZ, endX, endY, endZ, amount, comparator, filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder placeBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.PLACE, block, startX, startY, startZ, endX, endY, endZ, amount, "==", new String[]{}, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder placeBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.PLACE, block, startX, startY, startZ, endX, endY, endZ, amount, "==", new String[]{}, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder breakAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.REPLACE_BREAK, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder breakAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.REPLACE_BREAK, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder destroyAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.REPLACE_DESTROY, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder destroyAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.REPLACE_DESTROY, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder destroyBlockOnStart(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.DESTROY, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder destroyBlockOnEnd(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.DESTROY, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder breakBlockOnStart(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.BREAK, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder breakBlockOnEnd(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.BREAK, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    /** STRUCTURE **/

    @Method
    public CustomMachineCTRecipeBuilder requireStructure(String[][] pattern, Map<String, String> key) {
        List<List<String>> patternList = Arrays.stream(pattern).map(floors -> Arrays.stream(floors).toList()).toList();
        Map<Character, IIngredient<PartialBlockState>> keysMap = new HashMap<>();
        for(Map.Entry<String, String> entry : key.entrySet()) {
            if(entry.getKey().length() != 1) {
                CraftTweakerAPI.LOGGER.error("Invalid structure key: " + entry.getKey() + " Must be a single character which is not 'm'");
                return this;
            }
            char keyChar = entry.getKey().charAt(0);
            DataResult<IIngredient<PartialBlockState>> result = IIngredient.BLOCK.parse(JsonOps.INSTANCE, new JsonPrimitive(entry.getValue()));
            if(result.error().isPresent() || !result.result().isPresent()) {
                CraftTweakerAPI.LOGGER.error("Invalid structure block: " + entry.getValue());
                CraftTweakerAPI.LOGGER.error(result.error().get().message());
                return this;
            }
            keysMap.put(keyChar, result.result().get());
        }
        return addRequirement(new StructureRequirement(patternList, keysMap));
    }

    /** LOOT TABLE **/

    @Method
    public CustomMachineCTRecipeBuilder lootTableOutput(String lootTable, @OptionalFloat float luck) {
        if(!Utils.isResourceNameValid(lootTable)) {
            CraftTweakerAPI.LOGGER.error("Invalid loot table id: " + lootTable);
            return this;
        }
        ResourceLocation tableLoc = new ResourceLocation(lootTable);
        return addRequirement(new LootTableRequirement(tableLoc, luck));
    }

    /** DROP **/

    @Method
    public CustomMachineCTRecipeBuilder checkDrop(IItemStack item, int radius) {
        return checkDrops(new IItemStack[]{item}, item.getAmount(), radius, true);
    }

    @Method
    public CustomMachineCTRecipeBuilder checkAnyDrop(int amount, int radius) {
        return checkDrops(new IItemStack[]{}, amount, radius, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder checkDrops(IItemStack[] items, int amount, int radius, @OptionalBoolean(true) boolean whitelist) {
        if(items.length == 0 && whitelist) {
            CraftTweakerAPI.LOGGER.error("Invalid Drop requirement, checkDrop method must have at least 1 item defined when using whitelist mode");
            return this;
        }
        List<IIngredient<Item>> input = Arrays.stream(items).map(IItemStack::getDefinition).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CHECK, input, whitelist, Items.AIR, nbtFromStack(items[0]), amount, radius));
    }

    @Method
    public CustomMachineCTRecipeBuilder consumeDropOnStart(IItemStack item, int radius) {
        return consumeDropsOnStart(new IItemStack[]{item}, item.getAmount(), radius, true);
    }

    @Method
    public CustomMachineCTRecipeBuilder consumeAnyDropOnStart(int amount, int radius) {
        return consumeDropsOnStart(new IItemStack[]{}, amount, radius, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder consumeDropsOnStart(IItemStack[] items, int amount, int radius, @OptionalBoolean(true) boolean whitelist) {
        if(items.length == 0 && whitelist) {
            CraftTweakerAPI.LOGGER.error("Invalid Drop requirement, consumeDropOnStart method must have at least 1 item defined when using whitelist mode");
            return this;
        }
        List<IIngredient<Item>> input = Arrays.stream(items).map(IItemStack::getDefinition).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CONSUME, input, whitelist, Items.AIR, nbtFromStack(items[0]), amount, radius));
    }

    @Method
    public CustomMachineCTRecipeBuilder consumeDropOnEnd(IItemStack item, int radius) {
        return consumeDropsOnEnd(new IItemStack[]{item}, item.getAmount(), radius, true);
    }

    @Method
    public CustomMachineCTRecipeBuilder consumeAnyDropOnEnd(int amount, int radius) {
        return consumeDropsOnEnd(new IItemStack[]{}, amount, radius, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder consumeDropsOnEnd(IItemStack[] items, int amount, int radius, @OptionalBoolean(true) boolean whitelist) {
        if(items.length == 0 && whitelist) {
            CraftTweakerAPI.LOGGER.error("Invalid Drop requirement, consumeDropOnEnd method must have at least 1 item defined when using whitelist mode");
            return this;
        }
        List<IIngredient<Item>> input = Arrays.stream(items).map(IItemStack::getDefinition).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.CONSUME, input, whitelist, Items.AIR, nbtFromStack(items[0]), amount, radius));
    }

    @Method
    public CustomMachineCTRecipeBuilder dropItemOnStart(IItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.PRODUCE, Collections.emptyList(), true, stack.getDefinition(), nbtFromStack(stack), stack.getAmount(), 1));
    }

    @Method
    public CustomMachineCTRecipeBuilder dropItemOnEnd(IItemStack stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.PRODUCE, Collections.emptyList(), true, stack.getDefinition(), nbtFromStack(stack), stack.getAmount(), 1));
    }

    /** FUNCTION **/

    @Method
    public CustomMachineCTRecipeBuilder requireFunctionToStart(Function<Context, CraftingResult> function) {
        return addRequirement(new FunctionRequirement(FunctionRequirement.Phase.CHECK, new CTFunction(function)));
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFunctionOnStart(Function<Context, CraftingResult> function) {
        return addRequirement(new FunctionRequirement(FunctionRequirement.Phase.START, new CTFunction(function)));
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFunctionEachTick(Function<Context, CraftingResult> function) {
        return addRequirement(new FunctionRequirement(FunctionRequirement.Phase.TICK, new CTFunction(function)));
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFunctionOnEnd(Function<Context, CraftingResult> function) {
        return addRequirement(new FunctionRequirement(FunctionRequirement.Phase.END, new CTFunction(function)));
    }

    /** CHANCE **/

    @Method
    public CustomMachineCTRecipeBuilder chance(double chance) {
        if(this.lastRequirement != null && this.lastRequirement instanceof IChanceableRequirement)
            ((IChanceableRequirement<?>)this.lastRequirement).setChance(chance);
        else
            CraftTweakerAPI.LOGGER.error("Can't set chance for requirement: " + this.lastRequirement);
        return this;
    }

    /** HIDE **/

    @Method
    public CustomMachineCTRecipeBuilder hide() {
        //TODO: Remake
        return this;
    }

    /** DELAY **/

    @Method
    public CustomMachineCTRecipeBuilder delay(double delay) {
        if(this.lastRequirement != null && this.lastRequirement instanceof IDelayedRequirement<?>)
            ((IDelayedRequirement<?>)this.lastRequirement).setDelay(delay);
        else
            CraftTweakerAPI.LOGGER.error("Can't put delay for requirement: " + this.lastRequirement);
        return this;
    }

    /** JEI **/

    @Method
    public CustomMachineCTRecipeBuilder jei() {
        this.jei = true;
        return this;
    }

    /** PRIORITY **/

    @Method
    public CustomMachineCTRecipeBuilder priority(int priority) {
        if(!this.jei)
            this.builder.withPriority(priority);
        else
            this.builder.withJeiPriority(priority);
        return this;
    }

    /** ERROR **/

    @Method
    public CustomMachineCTRecipeBuilder resetOnError() {
        this.builder.setResetOnError();
        return this;
    }

    /** INTERNAL **/

    @Nullable
    private CompoundTag getNBT(IData data) {
        if(data == null) return null;
        return data.getInternal() instanceof CompoundTag ? (CompoundTag) data.getInternal() : null;
    }

    @Nullable
    private CompoundTag nbtFromStack(IItemStack stack) {
        return nbtFromStack(stack.getInternal());
    }

    @Nullable
    private CompoundTag nbtFromStack(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if(nbt == null || nbt.isEmpty())
            return null;
        if(nbt.contains("Damage", Tag.TAG_INT) && nbt.getInt("Damage") == 0)
            nbt.remove("Damage");
        if(nbt.isEmpty())
            return null;
        return nbt;
    }

    private CustomMachineCTRecipeBuilder addRequirement(IRequirement<?> requirement) {
        this.lastRequirement = requirement;
        if(!this.jei)
            this.builder.withRequirement(requirement);
        else
            this.builder.withJeiRequirement(requirement);
        return this;
    }

    private CustomMachineCTRecipeBuilder withEnergyRequirement(RequirementIOMode mode, int amount, boolean isPerTick) {
        if(isPerTick)
            return addRequirement(new EnergyPerTickRequirement(mode, amount));
        else
            return addRequirement(new EnergyRequirement(mode, amount));
    }

    private CustomMachineCTRecipeBuilder withFluidRequirement(RequirementIOMode mode, IIngredient<Fluid> fluid, int amount, boolean isPerTick, CompoundTag nbt, String tank) {
        if(isPerTick) {
            return addRequirement(new FluidPerTickRequirement(mode, fluid, amount, nbt, tank));
        } else {
            return addRequirement(new FluidRequirement(mode, fluid, amount, nbt, tank));
        }
    }

    private CustomMachineCTRecipeBuilder withBlockRequirement(RequirementIOMode mode, BlockRequirement.ACTION action, String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String comparator, String[] stringFilter, boolean whitelist) {
        PartialBlockState state;
        if(block.isEmpty())
            state = PartialBlockState.AIR;
        else
            state = Codecs.PARTIAL_BLOCK_STATE_CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(block)).resultOrPartial(CraftTweakerAPI.LOGGER::error).orElse(null);
        if(state == null) {
            CraftTweakerAPI.LOGGER.error("Invalid block: " + block);
            return this;
        }
        AABB bb = new AABB(startX, startY, startZ, endX, endY, endZ);
        List<IIngredient<PartialBlockState>> filter;
        if(stringFilter != null)
            filter = Arrays.stream(stringFilter).map(s -> IIngredient.BLOCK.parse(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(CraftTweakerAPI.LOGGER::error).orElse(null)).filter(Objects::nonNull).toList();
        else
            filter = Collections.emptyList();
        try {
            return this.addRequirement(new BlockRequirement(mode, action, bb, amount, ComparatorMode.value(comparator), state, filter, whitelist));
        } catch (IllegalArgumentException e) {
            CraftTweakerAPI.LOGGER.error("Invalid comparator: " + comparator);
        }
        return this;
    }
}