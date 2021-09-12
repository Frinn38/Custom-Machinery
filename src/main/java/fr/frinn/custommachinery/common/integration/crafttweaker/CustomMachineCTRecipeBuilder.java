package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.CraftTweaker;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.entity.MCEntityType;
import com.blamejared.crafttweaker.impl.helper.CraftTweakerHelper;
import com.blamejared.crafttweaker.impl.managers.RecipeManagerWrapper;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipeBuilder;
import fr.frinn.custommachinery.common.crafting.requirements.*;
import fr.frinn.custommachinery.common.data.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.util.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.biome.Biome;
import org.openzen.zencode.java.ZenCodeType.Optional;
import org.openzen.zencode.java.ZenCodeType.OptionalInt;
import org.openzen.zencode.java.ZenCodeType.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
@ZenRegister
@Name("mods.custommachinery.CMRecipeBuilder")
public class CustomMachineCTRecipeBuilder {

    private static int index = 0;
    private CustomMachineRecipeBuilder builder;
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
                recipeID = new ResourceLocation(CraftTweaker.MODID, name);
            else
                recipeID = new ResourceLocation(CraftTweaker.MODID, "custom_machine_recipe_" + index++);
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid Recipe name: " + name + "\n" + e.getMessage());
        }
        CustomMachineRecipe recipe = this.builder.build(recipeID);
        CraftTweakerAPI.apply(new ActionAddRecipe(new RecipeManagerWrapper(Registration.CUSTOM_MACHINE_RECIPE), recipe));
    }

    /** ENERGY **/

    @Method
    public CustomMachineCTRecipeBuilder requireEnergy(int amount) {
        return withEnergyRequirement(IRequirement.MODE.INPUT, amount, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder requireEnergyPerTick(int amount) {
        return withEnergyRequirement(IRequirement.MODE.INPUT, amount, true);
    }

    @Method
    public CustomMachineCTRecipeBuilder produceEnergy(int amount) {
        return withEnergyRequirement(IRequirement.MODE.OUTPUT, amount, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder produceEnergyPerTick(int amount) {
        return withEnergyRequirement(IRequirement.MODE.OUTPUT, amount, true);
    }

    /** FLUIDS **/

    @Method
    public CustomMachineCTRecipeBuilder requireFluid(IFluidStack stack, @OptionalString String tank) {
        return withFluidRequirement(IRequirement.MODE.INPUT, new Ingredient.FluidIngredient(stack.getFluid()), stack.getAmount(), false, tank);
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFluidTag(MCTag<Fluid> tag, int amount, @OptionalString String tank) {
        return withFluidRequirement(IRequirement.MODE.INPUT, new Ingredient.FluidIngredient(CraftTweakerHelper.getITag(tag)), amount, false, tank);
    }

    @Method
    public CustomMachineCTRecipeBuilder produceFluid(IFluidStack stack, @OptionalString String tank) {
        return withFluidRequirement(IRequirement.MODE.OUTPUT, new Ingredient.FluidIngredient(stack.getFluid()), stack.getAmount(), false, tank);
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFluidPerTick(IFluidStack stack, @OptionalString String tank) {
        return withFluidRequirement(IRequirement.MODE.INPUT, new Ingredient.FluidIngredient(stack.getFluid()), stack.getAmount(), true, tank);
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFluidTagPerTick(MCTag<Fluid> tag, int amount, @OptionalString String tank) {
        return withFluidRequirement(IRequirement.MODE.INPUT, new Ingredient.FluidIngredient(tag.getInternalRaw()), amount, true, tank);
    }

    @Method
    public CustomMachineCTRecipeBuilder produceFluidPerTick(IFluidStack stack, @OptionalString String tank) {
        return withFluidRequirement(IRequirement.MODE.OUTPUT, new Ingredient.FluidIngredient(stack.getFluid()), stack.getAmount(), true, tank);
    }

    /** ITEM **/

    @Method
    public CustomMachineCTRecipeBuilder requireItem(IItemStack stack, @OptionalString String slot) {
        return addRequirement(new ItemRequirement(IRequirement.MODE.INPUT, new Ingredient.ItemIngredient(stack.getDefinition()), stack.getAmount(), (CompoundNBT) stack.getOrCreateTag().getInternal(), slot));
    }

    @Method
    public CustomMachineCTRecipeBuilder requireItemTag(MCTag<Item> tag, int amount, @Optional IData nbt, @OptionalString String slot) {
        return addRequirement(new ItemRequirement(IRequirement.MODE.INPUT, new Ingredient.ItemIngredient(CraftTweakerHelper.getITag(tag)), amount, nbt == null ? new CompoundNBT() : (CompoundNBT) nbt.getInternal(), slot));
    }

    @Method
    public CustomMachineCTRecipeBuilder produceItem(IItemStack stack, @OptionalString String slot) {
        return addRequirement(new ItemRequirement(IRequirement.MODE.OUTPUT, new Ingredient.ItemIngredient(stack.getDefinition()), stack.getAmount(), (CompoundNBT) stack.getOrCreateTag().getInternal(), slot));
    }

    /** DURABILITY **/

    @Method
    public CustomMachineCTRecipeBuilder damageItem(IItemStack stack, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(IRequirement.MODE.INPUT, new Ingredient.ItemIngredient(stack.getDefinition()), amount, (CompoundNBT) stack.getOrCreateTag().getInternal(), slot));
    }

    @Method
    public CustomMachineCTRecipeBuilder damageItemTag(MCTag<Item> tag, int amount, @Optional IData nbt, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(IRequirement.MODE.INPUT, new Ingredient.ItemIngredient(CraftTweakerHelper.getITag(tag)), amount, nbt == null ? null : (CompoundNBT) nbt.getInternal(), slot));
    }

    @Method
    public CustomMachineCTRecipeBuilder repairItem(IItemStack stack, int amount, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(IRequirement.MODE.OUTPUT, new Ingredient.ItemIngredient(stack.getDefinition()), amount, (CompoundNBT) stack.getOrCreateTag().getInternal(), slot));
    }

    @Method
    public CustomMachineCTRecipeBuilder repairItemTag(MCTag<Item> tag, int amount, @Optional IData nbt, @OptionalString String slot) {
        return addRequirement(new DurabilityRequirement(IRequirement.MODE.OUTPUT, new Ingredient.ItemIngredient(CraftTweakerHelper.getITag(tag)), amount, nbt == null ? null : (CompoundNBT) nbt.getInternal(), slot));
    }

    /** TIME **/

    @Method
    public CustomMachineCTRecipeBuilder requireTime(String[] times) {
        List<TimeComparator> timeComparators = Stream.of(times).map(s -> Codecs.TIME_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(CraftTweakerAPI::logError).orElseThrow(() -> new IllegalArgumentException("Invalid time comparator: " + s)).getFirst()).collect(Collectors.toList());
        if(!timeComparators.isEmpty())
            return addRequirement(new TimeRequirement(timeComparators));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireTime(String time) {
        TimeComparator timeComparator = Codecs.TIME_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(time)).resultOrPartial(CraftTweakerAPI::logError).orElseThrow(() -> new IllegalArgumentException("Invalid time comparator: " + time)).getFirst();
        return addRequirement(new TimeRequirement(Collections.singletonList(timeComparator)));
    }

    /** POSITION **/

    @Method
    public CustomMachineCTRecipeBuilder requirePosition(String[] positions) {
        List<PositionComparator> positionComparators = Stream.of(positions).map(s -> Codecs.POSITION_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(CraftTweakerAPI::logError).orElseThrow(() -> new IllegalArgumentException("Invalid position comparator: " + s)).getFirst()).collect(Collectors.toList());
        if(!positionComparators.isEmpty())
            return addRequirement(new PositionRequirement(positionComparators, Collections.emptyList(), false, Collections.emptyList(), false));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requirePosition(String position) {
        PositionComparator positionComparator = Codecs.POSITION_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(position)).resultOrPartial(CraftTweakerAPI::logError).orElseThrow(() -> new IllegalArgumentException("Invalid position comparator: " + position)).getFirst();
        return addRequirement(new PositionRequirement(Collections.singletonList(positionComparator), Collections.emptyList(), false, Collections.emptyList(), false));
    }

    @Method
    public CustomMachineCTRecipeBuilder biomeWhitelist(Biome[] biomes) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).map(Biome::getRegistryName).collect(Collectors.toList());
        return addRequirement(new PositionRequirement(Collections.emptyList(), biomesID, false, Collections.emptyList(), false));
    }

    @Method
    public CustomMachineCTRecipeBuilder biomeWhitelist(Biome biome) {
        return addRequirement(new PositionRequirement(Collections.emptyList(), Collections.singletonList(biome.getRegistryName()), false, Collections.emptyList(), false));
    }

    @Method
    public CustomMachineCTRecipeBuilder biomeBlacklist(Biome[] biomes) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).map(Biome::getRegistryName).collect(Collectors.toList());
        return addRequirement(new PositionRequirement(Collections.emptyList(), biomesID, true, Collections.emptyList(), false));
    }

    @Method
    public CustomMachineCTRecipeBuilder biomeBlacklist(Biome biome) {
        return addRequirement(new PositionRequirement(Collections.emptyList(), Collections.singletonList(biome.getRegistryName()), true, Collections.emptyList(), false));
    }

    @Method
    public CustomMachineCTRecipeBuilder dimensionWhitelist(String[] dimensions) {
        try {
            List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).map(ResourceLocation::new).collect(Collectors.toList());
            return addRequirement(new PositionRequirement(Collections.emptyList(), Collections.emptyList(), false, dimensionsID, false));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid dimension ID: " + e.getMessage());
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder dimensionWhitelist(String dimension) {
        try {
            return addRequirement(new PositionRequirement(Collections.emptyList(), Collections.emptyList(), false, Collections.singletonList(new ResourceLocation(dimension)), false));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid dimensions ID: " + e.getMessage());
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder dimensionBlacklist(String[] dimensions) {
        try {
            List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).map(ResourceLocation::new).collect(Collectors.toList());
            return addRequirement(new PositionRequirement(Collections.emptyList(), Collections.emptyList(), false, dimensionsID, false));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid dimension ID: " + e.getMessage());
        }
    }

    @Method
    public CustomMachineCTRecipeBuilder dimensionBlacklist(String dimension) {
        try {
            return addRequirement(new PositionRequirement(Collections.emptyList(), Collections.emptyList(), false, Collections.singletonList(new ResourceLocation(dimension)), false));
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
    public CustomMachineCTRecipeBuilder giveEffectOnEnd(Effect effect, int time, int radius, @OptionalInt(1) int level, @Optional MCEntityType[] filter) {
        return addRequirement(new EffectRequirement(effect, time, level, radius, CraftTweakerHelper.getEntityTypes(Arrays.asList(filter)), true));
    }

    @Method
    public CustomMachineCTRecipeBuilder giveEffectEachTick(Effect effect, int time, int radius, @OptionalInt(1) int level, @Optional MCEntityType[] filter) {
        return addRequirement(new EffectRequirement(effect, time, level, radius, CraftTweakerHelper.getEntityTypes(Arrays.asList(filter)), false));
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
    public CustomMachineCTRecipeBuilder requireEntities(int amount, int radius, @Optional MCEntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(IRequirement.MODE.INPUT, EntityRequirement.ACTION.CHECK_AMOUNT, amount, radius, CraftTweakerHelper.getEntityTypes(Arrays.asList(filter)), whitelist));
    }

    @Method
    public CustomMachineCTRecipeBuilder requireEntitiesHealth(int amount, int radius, @Optional MCEntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(IRequirement.MODE.INPUT, EntityRequirement.ACTION.CHECK_HEALTH, amount, radius, CraftTweakerHelper.getEntityTypes(Arrays.asList(filter)), whitelist));
    }

    @Method
    public CustomMachineCTRecipeBuilder consumeEntityHealthOnStart(int amount, int radius, @Optional MCEntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(IRequirement.MODE.INPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, CraftTweakerHelper.getEntityTypes(Arrays.asList(filter)), whitelist));
    }

    @Method
    public CustomMachineCTRecipeBuilder consumeEntityHealthOnEnd(int amount, int radius, @Optional MCEntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(IRequirement.MODE.OUTPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, CraftTweakerHelper.getEntityTypes(Arrays.asList(filter)), whitelist));
    }

    @Method
    public CustomMachineCTRecipeBuilder killEntityOnStart(int amount, int radius, @Optional MCEntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(IRequirement.MODE.INPUT, EntityRequirement.ACTION.KILL, amount, radius, CraftTweakerHelper.getEntityTypes(Arrays.asList(filter)), whitelist));
    }

    @Method
    public CustomMachineCTRecipeBuilder killEntityOnEnd(int amount, int radius, @Optional MCEntityType[] filter, @OptionalBoolean(true) boolean whitelist) {
        return addRequirement(new EntityRequirement(IRequirement.MODE.OUTPUT, EntityRequirement.ACTION.KILL, amount, radius, CraftTweakerHelper.getEntityTypes(Arrays.asList(filter)), whitelist));
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
        return withBlockRequirement(IRequirement.MODE.INPUT, BlockRequirement.ACTION.CHECK, "", startX, startY, startZ, endX, endY, endZ, amount, comparator, filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder placeBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(IRequirement.MODE.INPUT, BlockRequirement.ACTION.PLACE, block, startX, startY, startZ, endX, endY, endZ, amount, "==", new String[]{}, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder placeBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(IRequirement.MODE.OUTPUT, BlockRequirement.ACTION.PLACE, block, startX, startY, startZ, endX, endY, endZ, amount, "==", new String[]{}, false);
    }

    @Method
    public CustomMachineCTRecipeBuilder breakAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(IRequirement.MODE.INPUT, BlockRequirement.ACTION.REPLACE_BREAK, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder breakAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(IRequirement.MODE.OUTPUT, BlockRequirement.ACTION.REPLACE_BREAK, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder destroyAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(IRequirement.MODE.INPUT, BlockRequirement.ACTION.REPLACE_DESTROY, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder destroyAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount, @Optional String[] filter, @OptionalBoolean boolean whitelist) {
        return withBlockRequirement(IRequirement.MODE.OUTPUT, BlockRequirement.ACTION.REPLACE_DESTROY, block, startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder destroyBlockOnStart(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(IRequirement.MODE.INPUT, BlockRequirement.ACTION.DESTROY, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder destroyBlockOnEnd(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(IRequirement.MODE.OUTPUT, BlockRequirement.ACTION.DESTROY, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder breakBlockOnStart(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(IRequirement.MODE.INPUT, BlockRequirement.ACTION.BREAK, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    @Method
    public CustomMachineCTRecipeBuilder breakBlockOnEnd(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, @OptionalInt(1) int amount) {
        return withBlockRequirement(IRequirement.MODE.OUTPUT, BlockRequirement.ACTION.BREAK, "", startX, startY, startZ, endX, endY, endZ, amount, "==", filter, whitelist);
    }

    /** STRUCTURE **/

    @Method
    public CustomMachineCTRecipeBuilder requireStructure(String[][] pattern, Map<String, String> key) {
        List<List<String>> patternList = Arrays.stream(pattern).map(floors -> Arrays.stream(floors).collect(Collectors.toList())).collect(Collectors.toList());
        Map<Character, PartialBlockState> keysMap = new HashMap<>();
        for(Map.Entry<String, String> entry : key.entrySet()) {
            if(entry.getKey().length() != 1) {
                CraftTweakerAPI.logError("Invalid structure key: " + entry.getKey() + " Must be a single character which is not 'm'");
                return this;
            }
            char keyChar = entry.getKey().charAt(0);
            DataResult<Pair<PartialBlockState, JsonElement>> result = Codecs.PARTIAL_BLOCK_STATE_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(entry.getValue()));
            if(result.error().isPresent() || !result.result().isPresent()) {
                CraftTweakerAPI.logError("Invalid structure block: " + entry.getValue());
                CraftTweakerAPI.logError(result.error().get().message());
                return this;
            }
            PartialBlockState state = result.result().get().getFirst();
            keysMap.put(keyChar, state);
        }
        return addRequirement(new StructureRequirement(patternList, keysMap));
    }

    /** LOOT TABLE **/

    @Method
    public CustomMachineCTRecipeBuilder lootTableOutput(String lootTable, @OptionalFloat float luck) {
        if(!Utils.isResourceNameValid(lootTable)) {
            CraftTweakerAPI.logError("Invalid loot table id: " + lootTable);
            return this;
        }
        ResourceLocation tableLoc = new ResourceLocation(lootTable);
        return addRequirement(new LootTableRequirement(tableLoc, luck));
    }

    /** CHANCE **/

    @Method
    public CustomMachineCTRecipeBuilder chance(double chance) {
        if(this.lastRequirement != null && this.lastRequirement instanceof IChanceableRequirement)
            ((IChanceableRequirement<?>)this.lastRequirement).setChance(chance);
        else
            CraftTweakerAPI.logError("Can't set chance for requirement: " + this.lastRequirement);
        return this;
    }

    /** HIDE **/

    @Method
    public CustomMachineCTRecipeBuilder hide() {
        if(this.lastRequirement != null && this.lastRequirement instanceof IDisplayInfoRequirement)
            ((IDisplayInfoRequirement<?>)this.lastRequirement).setJeiVisible(false);
        else
            CraftTweakerAPI.logError("Can't hide requirement: " + this.lastRequirement);
        return this;
    }

    /** DELAY **/

    @Method
    public CustomMachineCTRecipeBuilder delay(double delay) {
        if(this.lastRequirement != null && this.lastRequirement instanceof IDelayedRequirement<?>)
            ((IDelayedRequirement<?>)this.lastRequirement).setDelay(delay);
        else
            CraftTweakerAPI.logError("Can't put delay for requirement: " + this.lastRequirement);
        return this;
    }

    /** JEI **/

    @Method
    public CustomMachineCTRecipeBuilder jei() {
        this.jei = true;
        return this;
    }

    /** PRIORITY **/

    @Method CustomMachineCTRecipeBuilder priority(int priority) {
        this.builder.withPriority(priority);
        return this;
    }

    /** INTERNAL **/

    private CustomMachineCTRecipeBuilder addRequirement(IRequirement<?> requirement) {
        this.lastRequirement = requirement;
        if(!this.jei)
            this.builder.withRequirement(requirement);
        else
            this.builder.withJeiRequirement(requirement);
        return this;
    }

    private CustomMachineCTRecipeBuilder withEnergyRequirement(IRequirement.MODE mode, int amount, boolean isPerTick) {
        if(isPerTick)
            return addRequirement(new EnergyPerTickRequirement(mode, amount));
        else
            return addRequirement(new EnergyRequirement(mode, amount));
    }

    private CustomMachineCTRecipeBuilder withFluidRequirement(IRequirement.MODE mode, Ingredient.FluidIngredient fluid, int amount, boolean isPerTick, String tank) {
        if(isPerTick) {
            return addRequirement(new FluidPerTickRequirement(mode, fluid, amount, tank));
        } else {
            return addRequirement(new FluidRequirement(mode, fluid, amount, tank));
        }
    }

    private CustomMachineCTRecipeBuilder withBlockRequirement(IRequirement.MODE mode, BlockRequirement.ACTION action, String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String comparator, String[] stringFilter, boolean whitelist) {
        PartialBlockState state = Codecs.PARTIAL_BLOCK_STATE_CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(block)).resultOrPartial(CraftTweakerAPI::logError).orElse(null);
        if(state == null) {
            CraftTweakerAPI.logError("Invalid block: " + block);
            return this;
        }
        AxisAlignedBB bb = new AxisAlignedBB(startX, startY, startZ, endX, endY, endZ);
        List<PartialBlockState> filter = Arrays.stream(stringFilter).map(s -> Codecs.PARTIAL_BLOCK_STATE_CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(CraftTweakerAPI::logError).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList());
        try {
            return this.addRequirement(new BlockRequirement(mode, action, bb, amount, ComparatorMode.value(comparator), state, filter, whitelist));
        } catch (IllegalArgumentException e) {
            CraftTweakerAPI.logError("Invalid comparator: " + comparator);
        }
        return this;
    }
}
