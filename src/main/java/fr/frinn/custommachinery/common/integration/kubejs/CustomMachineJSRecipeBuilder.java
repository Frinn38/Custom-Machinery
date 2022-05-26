package fr.frinn.custommachinery.common.integration.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.kubejs.util.MapJS;
import fr.frinn.custommachinery.api.requirement.IChanceableRequirement;
import fr.frinn.custommachinery.api.requirement.IDelayedRequirement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipeBuilder;
import fr.frinn.custommachinery.common.crafting.requirement.BiomeRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.BlockRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.CommandRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.DimensionRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.DropRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.DurabilityRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.EffectRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.EnergyPerTickRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.EnergyRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.EntityRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.FluidPerTickRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.FluidRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.FuelRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.FunctionRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.ItemRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.LightRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.LootTableRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.PositionRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.RedstoneRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.StructureRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.TimeRequirement;
import fr.frinn.custommachinery.common.crafting.requirement.WeatherRequirement;
import fr.frinn.custommachinery.common.data.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.integration.kubejs.function.KJSFunction;
import fr.frinn.custommachinery.common.integration.kubejs.function.RecipeFunction;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomMachineJSRecipeBuilder extends RecipeJS {

    private static final Map<ResourceLocation, Integer> IDS = new HashMap<>();
    private CustomMachineRecipeBuilder builder;
    private IRequirement<?> lastRequirement;
    private boolean jei = false;

    @Override
    public void create(ListJS listJS) {
        if(listJS.size() < 2 || !(listJS.get(0) instanceof String) || !Utils.isResourceNameValid((String)listJS.get(0)) || !(listJS.get(1) instanceof Number))
            throw new RecipeExceptionJS("Custom Machine recipe must have a machine and a time specified");
        ResourceLocation machine = new ResourceLocation((String) listJS.get(0));
        int time = ((Number)listJS.get(1)).intValue();
        this.builder = new CustomMachineRecipeBuilder(machine, time);
        int uniqueID = IDS.computeIfAbsent(machine, m -> 0);
        IDS.put(machine, uniqueID + 1);
        this.id(new ResourceLocation("kubejs", "custom_machine/" + machine.getNamespace() + "/" + machine.getPath() + "/" + uniqueID));
    }

    @Override
    public void deserialize() {
        DataResult<Pair<CustomMachineRecipeBuilder, JsonElement>> result = CustomMachineRecipeBuilder.CODEC.decode(JsonOps.INSTANCE, this.json);
        this.builder = result.resultOrPartial(ScriptType.SERVER.console::error).orElseThrow(() -> new RecipeExceptionJS("Invalid Custom Machine Recipe")).getFirst();
    }

    @Override
    public void serialize() {
        DataResult<JsonElement> result = CustomMachineRecipeBuilder.CODEC.encodeStart(JsonOps.INSTANCE, this.builder);
        this.json = (JsonObject) result.resultOrPartial(ScriptType.SERVER.console::error).orElseThrow(() -> new RecipeExceptionJS("Invalid Custom Machine Recipe"));
    }

    @Override
    public Recipe<?> createRecipe() {
        return this.builder.build(getOrCreateId());
    }

    @Override
    public String getFromToString() {
        return this.builder.toString();
    }

    @Override
    public RecipeJS merge(Object data) {
        ScriptType.SERVER.console.warn("Don't use 'merge' method on custom machine recipe");
        return this;
    }

    private CustomMachineJSRecipeBuilder addRequirement(IRequirement<?> requirement) {
        this.lastRequirement = requirement;
        if(!this.jei)
            this.builder.withRequirement(requirement);
        else
            this.builder.withJeiRequirement(requirement);
        return this;
    }

    /** CHANCE **/

    public CustomMachineJSRecipeBuilder chance(double chance) {
        if(this.lastRequirement != null && this.lastRequirement instanceof IChanceableRequirement)
            ((IChanceableRequirement<?>)this.lastRequirement).setChance(chance);
        else
            ScriptType.SERVER.console.warn("Can't set chance for requirement: " + this.lastRequirement);
        return this;
    }

    /** JEI **/

    public CustomMachineJSRecipeBuilder hide() {
        //TODO: Remake
        return this;
    }

    /** DELAY **/

    public CustomMachineJSRecipeBuilder delay(double delay) {
        if(this.lastRequirement != null && this.lastRequirement instanceof IDelayedRequirement<?>)
            ((IDelayedRequirement<?>)this.lastRequirement).setDelay(delay);
        else
            ScriptType.SERVER.console.warn("Can't set delay for requirement: " + this.lastRequirement);
        return this;
    }

    /** PRIORITY **/

    public CustomMachineJSRecipeBuilder priority(int priority) {
        if(!this.jei)
            this.builder.withPriority(priority);
        else
            this.builder.withJeiPriority(priority);
        return this;
    }

    /** JEI **/

    public CustomMachineJSRecipeBuilder jei() {
        this.jei = true;
        return this;
    }

    /** ITEM **/

    public CustomMachineJSRecipeBuilder requireItem(ItemStackJS stack) {
        return this.requireItem(stack, "");
    }

    public CustomMachineJSRecipeBuilder requireItem(ItemStackJS stack, String slot) {
        return this.addRequirement(new ItemRequirement(RequirementIOMode.INPUT, new ItemIngredient(stack.getItem()), stack.getCount(), nbtFromStack(stack), slot));
    }

    public CustomMachineJSRecipeBuilder requireItemTag(String tag, int amount) {
        return this.requireItemTag(tag, amount, null, "");
    }

    public CustomMachineJSRecipeBuilder requireItemTag(String tag, int amount, Object thing) {
        if(thing instanceof String)
            return this.requireItemTag(tag, amount, null, (String)thing);
        else
            return this.requireItemTag(tag, amount, MapJS.of(thing), "");
    }

    public CustomMachineJSRecipeBuilder requireItemTag(String tag, int amount, MapJS nbt, String slot) {
        try {
            return this.addRequirement(new ItemRequirement(RequirementIOMode.INPUT, ItemTagIngredient.create(tag), amount, MapJS.nbt(nbt), slot));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn(e.getMessage());
            return this;
        }
    }

    public CustomMachineJSRecipeBuilder produceItem(ItemStackJS stack) {
        return this.produceItem(stack, "");
    }

    public CustomMachineJSRecipeBuilder produceItem(ItemStackJS stack, String slot) {
        return this.addRequirement(new ItemRequirement(RequirementIOMode.OUTPUT, new ItemIngredient(stack.getItem()), stack.getCount(), nbtFromStack(stack), slot));
    }

    /** DURABILITY **/

    public CustomMachineJSRecipeBuilder damageItem(ItemStackJS stack, int amount) {
        return this.damageItem(stack, amount,"");
    }

    public CustomMachineJSRecipeBuilder damageItem(ItemStackJS stack, int amount, String slot) {
        return this.addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, new ItemIngredient(stack.getItem()), amount, nbtFromStack(stack), slot));
    }

    public CustomMachineJSRecipeBuilder damageItemTag(String tag, int amount) {
        return this.damageItemTag(tag, amount, null, "");
    }

    public CustomMachineJSRecipeBuilder damageItemTag(String tag, int amount, Object thing) {
        if(thing instanceof String)
            return this.damageItemTag(tag, amount, null, (String)thing);
        else
            return this.damageItemTag(tag, amount, MapJS.of(thing), "");
    }

    public CustomMachineJSRecipeBuilder damageItemTag(String tag, int amount, MapJS nbt, String slot) {
        try {
            return this.addRequirement(new DurabilityRequirement(RequirementIOMode.INPUT, ItemTagIngredient.create(tag), amount, MapJS.nbt(nbt), slot));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn(e.getMessage());
            return this;
        }
    }

    public CustomMachineJSRecipeBuilder repairItem(ItemStackJS stack, int amount) {
        return this.repairItem(stack, amount, "");
    }

    public CustomMachineJSRecipeBuilder repairItem(ItemStackJS stack, int amount, String slot) {
        return this.addRequirement(new DurabilityRequirement(RequirementIOMode.OUTPUT, new ItemIngredient(stack.getItem()), amount, nbtFromStack(stack), slot));
    }

    public CustomMachineJSRecipeBuilder repairItemTag(String tag, int amount) {
        return this.repairItemTag(tag, amount, null, "");
    }

    public CustomMachineJSRecipeBuilder repairItemTag(String tag, int amount, Object thing) {
        if(thing instanceof String)
            return this.repairItemTag(tag, amount, null, (String)thing);
        else
            return this.repairItemTag(tag, amount, MapJS.of(thing), "");
    }

    public CustomMachineJSRecipeBuilder repairItemTag(String tag, int amount, MapJS nbt, String slot) {
        try {
            return this.addRequirement(new DurabilityRequirement(RequirementIOMode.OUTPUT, ItemTagIngredient.create(tag), amount, MapJS.nbt(nbt), slot));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn(e.getMessage());
            return this;
        }
    }

    /** FLUID **/

    public CustomMachineJSRecipeBuilder requireFluid(FluidStackJS stack) {
        return this.requireFluid(stack, "");
    }

    public CustomMachineJSRecipeBuilder requireFluid(FluidStackJS stack, String tank) {
        return this.addRequirement(new FluidRequirement(RequirementIOMode.INPUT, new FluidIngredient(stack.getFluid()), (int)stack.getAmount(), stack.getFluidStack().getTag(), tank));
    }

    public CustomMachineJSRecipeBuilder requireFluidTag(String tag, int amount) {
        return this.requireFluidTag(tag, amount, null, "");
    }

    public CustomMachineJSRecipeBuilder requireFluidTag(String tag, int amount, Object thing) {
        if(thing instanceof String)
            return this.requireFluidTag(tag, amount, null, (String)thing);
        else
            return this.requireFluidTag(tag, amount, MapJS.of(thing), "");
    }

    public CustomMachineJSRecipeBuilder requireFluidTag(String tag, int amount, MapJS nbt, String tank) {
        try {
            return this.addRequirement(new FluidRequirement(RequirementIOMode.INPUT, FluidTagIngredient.create(tag), amount, MapJS.nbt(nbt), tank));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn(e.getMessage());
            return this;
        }
    }

    public CustomMachineJSRecipeBuilder produceFluid(FluidStackJS stack) {
        return this.produceFluid(stack, "");
    }

    public CustomMachineJSRecipeBuilder produceFluid(FluidStackJS stack, String tank) {
        return this.addRequirement(new FluidRequirement(RequirementIOMode.OUTPUT, new FluidIngredient(stack.getFluid()), (int)stack.getAmount(), stack.getFluidStack().getTag(), tank));
    }

    public CustomMachineJSRecipeBuilder requireFluidPerTick(FluidStackJS stack) {
        return this.requireFluidPerTick(stack, "");
    }

    public CustomMachineJSRecipeBuilder requireFluidPerTick(FluidStackJS stack, String tank) {
        return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.INPUT, new FluidIngredient(stack.getFluid()), (int)stack.getAmount(), stack.getFluidStack().getTag(), tank));
    }

    public CustomMachineJSRecipeBuilder requireFluidTagPerTick(String tag, int amount) {
        return this.requireFluidTagPerTick(tag, amount, null, "");
    }

    public CustomMachineJSRecipeBuilder requireFluidTagPerTick(String tag, int amount, Object thing) {
        if(thing instanceof String)
            return this.requireFluidTagPerTick(tag, amount, null, (String)thing);
        else
            return this.requireFluidTagPerTick(tag, amount, MapJS.of(thing), "");
    }

    public CustomMachineJSRecipeBuilder requireFluidTagPerTick(String tag, int amount, MapJS nbt, String tank) {
        try {
            return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.INPUT, FluidTagIngredient.create(tag), amount, MapJS.nbt(nbt), tank));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn(e.getMessage());
            return this;
        }
    }

    public CustomMachineJSRecipeBuilder produceFluidPerTick(FluidStackJS stack) {
        return this.produceFluidPerTick(stack, "");
    }

    public CustomMachineJSRecipeBuilder produceFluidPerTick(FluidStackJS stack, String tank) {
        return this.addRequirement(new FluidPerTickRequirement(RequirementIOMode.OUTPUT, new FluidIngredient(stack.getFluid()), (int)stack.getAmount(), stack.getFluidStack().getTag(), tank));
    }

    /** ENERGY **/

    public CustomMachineJSRecipeBuilder requireEnergy(int amount) {
        return this.addRequirement(new EnergyRequirement(RequirementIOMode.INPUT, amount));
    }

    public CustomMachineJSRecipeBuilder requireEnergyPerTick(int amount) {
        return this.addRequirement(new EnergyPerTickRequirement(RequirementIOMode.INPUT, amount));
    }

    public CustomMachineJSRecipeBuilder produceEnergy(int amount) {
        return this.addRequirement(new EnergyRequirement(RequirementIOMode.OUTPUT, amount));
    }

    public CustomMachineJSRecipeBuilder produceEnergyPerTick(int amount) {
        return this.addRequirement(new EnergyPerTickRequirement(RequirementIOMode.OUTPUT, amount));
    }

    /** TIME **/

    public CustomMachineJSRecipeBuilder requireTime(String[] times) {
        List<TimeComparator> timeComparators = Stream.of(times).map(s -> Codecs.TIME_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(ScriptType.SERVER.console::error).orElseThrow(() -> new IllegalArgumentException("Invalid time comparator: " + s)).getFirst()).toList();
        if(!timeComparators.isEmpty())
            return this.addRequirement(new TimeRequirement(timeComparators));
        return this;
    }

    /** POSITION **/

    public CustomMachineJSRecipeBuilder requirePosition(String[] position) {
        List<PositionComparator> positionComparators = Stream.of(position).map(s -> Codecs.POSITION_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(ScriptType.SERVER.console::error).orElseThrow(() -> new IllegalArgumentException("Invalid position comparator: " + s)).getFirst()).toList();
        if(!positionComparators.isEmpty())
            return this.addRequirement(new PositionRequirement(positionComparators));
        return this;
    }

    /** BIOME **/

    public CustomMachineJSRecipeBuilder biomeWhitelist(String[] biomes) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).filter(biome -> {
            if(Utils.isResourceNameValid(biome))
                return true;
            ScriptType.SERVER.console.warn("Invalid biome ID: " + biome);
            return false;
        }).map(ResourceLocation::new).toList();
        return this.addRequirement(new BiomeRequirement(biomesID, false));
    }

    public CustomMachineJSRecipeBuilder biomeBlacklist(String[] biomes) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).filter(biome -> {
            if(Utils.isResourceNameValid(biome))
                return true;
            ScriptType.SERVER.console.warn("Invalid biome ID: " + biome);
            return false;
        }).map(ResourceLocation::new).toList();
        return this.addRequirement(new BiomeRequirement(biomesID, true));
    }

    /** DIMENSION **/

    public CustomMachineJSRecipeBuilder dimensionWhitelist(String[] dimensions) {
        List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).filter(dimension -> {
            if(Utils.isResourceNameValid(dimension))
                return true;
            ScriptType.SERVER.console.warn("Invalid dimension ID: " + dimension);
            return false;
        }).map(ResourceLocation::new).toList();
        return this.addRequirement(new DimensionRequirement(dimensionsID, false));
    }

    public CustomMachineJSRecipeBuilder dimensionBlacklist(String[] dimensions) {
        List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).filter(dimension -> {
            if(Utils.isResourceNameValid(dimension))
                return true;
            ScriptType.SERVER.console.warn("Invalid dimension ID: " + dimension);
            return false;
        }).map(ResourceLocation::new).toList();
        return this.addRequirement(new DimensionRequirement(dimensionsID, true));
    }

    /** FUEL **/

    public CustomMachineJSRecipeBuilder requireFuel() {
        return requireFuel(1);
    }

    public CustomMachineJSRecipeBuilder requireFuel(int amount) {
        return this.addRequirement(new FuelRequirement(amount));
    }

    /** COMMAND **/

    public CustomMachineJSRecipeBuilder runCommandOnStart(String command) {
        return this.runCommandOnStart(command, 2, false);
    }

    public CustomMachineJSRecipeBuilder runCommandOnStart(String command, int permissionLevel) {
        return this.runCommandOnStart(command, permissionLevel, false);
    }

    public CustomMachineJSRecipeBuilder runCommandOnStart(String command, boolean log) {
        return this.runCommandOnStart(command, 2, log);
    }

    public CustomMachineJSRecipeBuilder runCommandOnStart(String command, int permissionLevel, boolean log) {
        return this.addRequirement(new CommandRequirement(command, CraftingManager.PHASE.STARTING, permissionLevel, log));
    }

    public CustomMachineJSRecipeBuilder runCommandEachTick(String command) {
        return this.runCommandEachTick(command, 2, true);
    }

    public CustomMachineJSRecipeBuilder runCommandEachTick(String command, int permissionLevel) {
        return this.runCommandEachTick(command, permissionLevel, true);
    }

    public CustomMachineJSRecipeBuilder runCommandEachTick(String command, boolean log) {
        return this.runCommandEachTick(command, 2, log);
    }

    public CustomMachineJSRecipeBuilder runCommandEachTick(String command, int permissionLevel, boolean log) {
        return this.addRequirement(new CommandRequirement(command, CraftingManager.PHASE.CRAFTING_TICKABLE, permissionLevel, log));
    }

    public CustomMachineJSRecipeBuilder runCommandOnEnd(String command) {
        return this.runCommandOnEnd(command, 2, true);
    }

    public CustomMachineJSRecipeBuilder runCommandOnEnd(String command, int permissionLevel) {
        return this.runCommandOnEnd(command, permissionLevel, true);
    }

    public CustomMachineJSRecipeBuilder runCommandOnEnd(String command, boolean log) {
        return this.runCommandOnEnd(command, 2, log);
    }

    public CustomMachineJSRecipeBuilder runCommandOnEnd(String command, int permissionLevel, boolean log) {
        return this.addRequirement(new CommandRequirement(command, CraftingManager.PHASE.ENDING, permissionLevel, log));
    }

    /** EFFECT **/

    public CustomMachineJSRecipeBuilder giveEffectOnEnd(String effect, int time, int radius) {
        return this.giveEffectOnEnd(effect, time, radius, 1, new String[]{});
    }

    public CustomMachineJSRecipeBuilder giveEffectOnEnd(String effect, int time, int radius, int level) {
        return this.giveEffectOnEnd(effect, time, radius, level, new String[]{});
    }

    public CustomMachineJSRecipeBuilder giveEffectOnEnd(String effect, int time, int radius, String[] filter) {
        return this.giveEffectOnEnd(effect, time, radius, 1, filter);
    }

    public CustomMachineJSRecipeBuilder giveEffectOnEnd(String effect, int time, int radius, int level, String[] filter) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(ForgeRegistries.ENTITIES::getValue).collect(Collectors.toList());
        if(Utils.isResourceNameValid(effect) && ForgeRegistries.MOB_EFFECTS.containsKey(new ResourceLocation(effect)))
            return this.addRequirement(new EffectRequirement(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effect)), time, level, radius, entityFilter, true));
        else
            ScriptType.SERVER.console.warn("Invalid effect ID: " + effect);
        return this;
    }

    public CustomMachineJSRecipeBuilder giveEffectEachTick(String effect, int time, int radius) {
        return this.giveEffectEachTick(effect, time, radius, 1, new String[]{});
    }

    public CustomMachineJSRecipeBuilder giveEffectEachTick(String effect, int time, int radius, Object levelOrFilter) {
        if(levelOrFilter instanceof Number)
            return this.giveEffectEachTick(effect, time, radius, ((Number)levelOrFilter).intValue(), new String[]{});
        else if(levelOrFilter instanceof String)
            return this.giveEffectEachTick(effect, time, radius, 1, new String[]{(String)levelOrFilter});
        else if(levelOrFilter instanceof String[])
            return this.giveEffectEachTick(effect, time, radius, 1, (String[])levelOrFilter);
        ScriptType.SERVER.console.error("Invalid 4th param given to 'giveEffectEachTick' : " + levelOrFilter.toString());
        return this;
    }

    public CustomMachineJSRecipeBuilder giveEffectEachTick(String effect, int time, int radius, int level, String[] filter) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(ForgeRegistries.ENTITIES::getValue).collect(Collectors.toList());
        if(Utils.isResourceNameValid(effect) && ForgeRegistries.MOB_EFFECTS.containsKey(new ResourceLocation(effect)))
            return this.addRequirement(new EffectRequirement(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effect)), time, level, radius, entityFilter, false));
        else
            ScriptType.SERVER.console.warn("Invalid effect ID: " + effect);
        return this;
    }

    /** WEATHER **/

    public CustomMachineJSRecipeBuilder requireWeather(String weather) {
        try {
            return this.addRequirement(new WeatherRequirement(WeatherMachineComponent.WeatherType.value(weather), false));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn("Invalid weather type: " + weather);
        }
        return this;
    }

    public CustomMachineJSRecipeBuilder requireWeatherOnMachine(String weather) {
        try {
            return this.addRequirement(new WeatherRequirement(WeatherMachineComponent.WeatherType.value(weather), true));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn("Invalid weather type: " + weather);
        }
        return this;
    }

    /** REDSTONE **/

    public CustomMachineJSRecipeBuilder requireRedstone(int power) {
        return this.requireRedstone(power, ">=");
    }

    public CustomMachineJSRecipeBuilder requireRedstone(int power, String comparator) {
        try {
            return this.addRequirement(new RedstoneRequirement(power, ComparatorMode.value(comparator)));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn("Invalid comparator: " + comparator);
        }
        return this;
    }

    /** LIGHT **/

    public CustomMachineJSRecipeBuilder requireSkyLight(int amount) {
        return this.requireSkyLight(amount, ">=");
    }

    public CustomMachineJSRecipeBuilder requireSkyLight(int amount, String comparator) {
        try {
            return this.addRequirement(new LightRequirement(amount, ComparatorMode.value(comparator), true));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn("Invalid comparator: " + comparator);
        }
        return this;
    }

    public CustomMachineJSRecipeBuilder requireBlockLight(int amount) {
        return this.requireBlockLight(amount, ">=");
    }

    public CustomMachineJSRecipeBuilder requireBlockLight(int amount, String comparator) {
        try {
            return this.addRequirement(new LightRequirement(amount, ComparatorMode.value(comparator), false));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn("Invalid comparator: " + comparator);
        }
        return this;
    }

    /** ENTITY **/

    public CustomMachineJSRecipeBuilder requireEntities(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(ForgeRegistries.ENTITIES::getValue).collect(Collectors.toList());
        if(!entityFilter.isEmpty() || !whitelist)
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CHECK_AMOUNT, amount, radius, entityFilter, whitelist));
        return this;
    }

    public CustomMachineJSRecipeBuilder requireEntitiesHealth(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(ForgeRegistries.ENTITIES::getValue).collect(Collectors.toList());
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CHECK_HEALTH, amount, radius, entityFilter, whitelist));
        return this;
    }

    public CustomMachineJSRecipeBuilder consumeEntityHealthOnStart(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(ForgeRegistries.ENTITIES::getValue).collect(Collectors.toList());
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, entityFilter, whitelist));
        return this;
    }

    public CustomMachineJSRecipeBuilder consumeEntityHealthOnEnd(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(ForgeRegistries.ENTITIES::getValue).collect(Collectors.toList());
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.OUTPUT, EntityRequirement.ACTION.CONSUME_HEALTH, amount, radius, entityFilter, whitelist));
        return this;
    }

    public CustomMachineJSRecipeBuilder killEntitiesOnStart(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(ForgeRegistries.ENTITIES::getValue).collect(Collectors.toList());
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.INPUT, EntityRequirement.ACTION.KILL, amount, radius, entityFilter, whitelist));
        return this;
    }

    public CustomMachineJSRecipeBuilder killEntitiesOnEnd(int amount, int radius, String[] filter, boolean whitelist) {
        List<EntityType<?>> entityFilter = Arrays.stream(filter).filter(type -> {
            if(Utils.isResourceNameValid(type) && ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(type)))
                return true;
            ScriptType.SERVER.console.warn("Invalid entity ID: " + type);
            return false;
        }).map(ResourceLocation::new).map(ForgeRegistries.ENTITIES::getValue).collect(Collectors.toList());
        if(!entityFilter.isEmpty())
            return this.addRequirement(new EntityRequirement(RequirementIOMode.OUTPUT, EntityRequirement.ACTION.KILL, amount, radius, entityFilter, whitelist));
        return this;
    }

    /** BLOCK **/

    public CustomMachineJSRecipeBuilder requireBlock(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.requireBlock(filter, whitelist, startX, startY, startZ, endX, endY, endZ, 1, ">=");
    }

    public CustomMachineJSRecipeBuilder requireBlock(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.requireBlock(filter, whitelist, startX, startY, startZ, endX, endY, endZ, amount, ">=");
    }

    public CustomMachineJSRecipeBuilder requireBlock(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String comparator) {
        return this.blockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.CHECK, "", startX, startY, startZ, endX, endY, endZ, amount, comparator, filter, whitelist);
    }

    public CustomMachineJSRecipeBuilder placeBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.placeBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    public CustomMachineJSRecipeBuilder placeBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.PLACE, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS.toString(), new String[]{}, true);
    }

    public CustomMachineJSRecipeBuilder placeBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.placeBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    public CustomMachineJSRecipeBuilder placeBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.PLACE, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS.toString(), new String[]{}, true);
    }

    public CustomMachineJSRecipeBuilder breakAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.breakAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    public CustomMachineJSRecipeBuilder breakAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.breakAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, amount, new String[]{});
    }

    public CustomMachineJSRecipeBuilder breakAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String[] filter) {
        return this.breakAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, amount, filter, false);
    }

    public CustomMachineJSRecipeBuilder breakAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String[] filter, boolean whitelist) {
        return this.blockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.REPLACE_BREAK, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS.toString(), filter, whitelist);
    }

    public CustomMachineJSRecipeBuilder breakAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.breakAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    public CustomMachineJSRecipeBuilder breakAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.breakAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, amount, new String[]{});
    }

    public CustomMachineJSRecipeBuilder breakAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String[] filter) {
        return this.breakAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, amount, filter, false);
    }

    public CustomMachineJSRecipeBuilder breakAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String[] filter, boolean whitelist) {
        return this.blockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.REPLACE_BREAK, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS.toString(), filter, whitelist);
    }

    public CustomMachineJSRecipeBuilder destroyAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.destroyAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    public CustomMachineJSRecipeBuilder destroyAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.destroyAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, amount, new String[]{});
    }

    public CustomMachineJSRecipeBuilder destroyAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String[] filter) {
        return this.destroyAndPlaceBlockOnStart(block, startX, startY, startZ, endX, endY, endZ, amount, filter, false);
    }

    public CustomMachineJSRecipeBuilder destroyAndPlaceBlockOnStart(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String[] filter, boolean whitelist) {
        return this.blockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.REPLACE_DESTROY, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS.toString(), filter, whitelist);
    }

    public CustomMachineJSRecipeBuilder destroyAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.destroyAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, 1);
    }

    public CustomMachineJSRecipeBuilder destroyAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.destroyAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, amount, new String[]{});
    }

    public CustomMachineJSRecipeBuilder destroyAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String[] filter) {
        return this.destroyAndPlaceBlockOnEnd(block, startX, startY, startZ, endX, endY, endZ, amount, filter, false);
    }

    public CustomMachineJSRecipeBuilder destroyAndPlaceBlockOnEnd(String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String[] filter, boolean whitelist) {
        return this.blockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.REPLACE_DESTROY, block, startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS.toString(), filter, whitelist);
    }

    public CustomMachineJSRecipeBuilder destroyBlockOnStart(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.destroyBlockOnStart(filter, whitelist, startX, startY, startZ, endX, endY, endZ, 1);
    }

    public CustomMachineJSRecipeBuilder destroyBlockOnStart(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.DESTROY, "", startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS.toString(), filter, whitelist);
    }

    public CustomMachineJSRecipeBuilder destroyBlockOnEnd(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.destroyBlockOnEnd(filter, whitelist, startX, startY, startZ, endX, endY, endZ, 1);
    }

    public CustomMachineJSRecipeBuilder destroyBlockOnEnd(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.DESTROY, "", startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS.toString(), filter, whitelist);
    }

    public CustomMachineJSRecipeBuilder breakBlockOnStart(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.breakBlockOnStart(filter, whitelist, startX, startY, startZ, endX, endY, endZ, 1);
    }

    public CustomMachineJSRecipeBuilder breakBlockOnStart(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.INPUT, BlockRequirement.ACTION.BREAK, "", startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS.toString(), filter, whitelist);
    }

    public CustomMachineJSRecipeBuilder breakBlockOnEnd(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        return this.breakBlockOnEnd(filter, whitelist, startX, startY, startZ, endX, endY, endZ, 1);
    }

    public CustomMachineJSRecipeBuilder breakBlockOnEnd(String[] filter, boolean whitelist, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount) {
        return this.blockRequirement(RequirementIOMode.OUTPUT, BlockRequirement.ACTION.BREAK, "", startX, startY, startZ, endX, endY, endZ, amount, ComparatorMode.EQUALS.toString(), filter, whitelist);
    }

    private CustomMachineJSRecipeBuilder blockRequirement(RequirementIOMode mode, BlockRequirement.ACTION action, String block, int startX, int startY, int startZ, int endX, int endY, int endZ, int amount, String comparator, String[] stringFilter, boolean whitelist) {
        PartialBlockState state;
        if(block.isEmpty())
            state = PartialBlockState.AIR;
        else
            state = Codecs.PARTIAL_BLOCK_STATE_CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(block)).resultOrPartial(ScriptType.SERVER.console::warn).orElse(null);
        if(state == null) {
            ScriptType.SERVER.console.warn("Invalid block: " + block);
            return this;
        }
        AABB bb = new AABB(startX, startY, startZ, endX, endY, endZ);
        List<IIngredient<PartialBlockState>> filter = Arrays.stream(stringFilter).map(s -> IIngredient.BLOCK.parse(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(ScriptType.SERVER.console::warn).orElse(null)).filter(Objects::nonNull).toList();
        try {
            return this.addRequirement(new BlockRequirement(mode, action, bb, amount, ComparatorMode.value(comparator), state, filter, whitelist));
        } catch (IllegalArgumentException e) {
            ScriptType.SERVER.console.warn("Invalid comparator: " + comparator);
        }
        return this;
    }

    /** STRUCTURE **/

    public CustomMachineJSRecipeBuilder requireStructure(String[][] pattern, Map<String, String> key) {
        List<List<String>> patternList = Arrays.stream(pattern).map(floors -> Arrays.stream(floors).toList()).toList();
        Map<Character, IIngredient<PartialBlockState>> keysMap = new HashMap<>();
        for(Map.Entry<String, String> entry : key.entrySet()) {
            if(entry.getKey().length() != 1) {
                ScriptType.SERVER.console.warn("Invalid structure key: " + entry.getKey() + " Must be a single character which is not 'm'");
                return this;
            }
            char keyChar = entry.getKey().charAt(0);
            DataResult<IIngredient<PartialBlockState>> result = IIngredient.BLOCK.parse(JsonOps.INSTANCE, new JsonPrimitive(entry.getValue()));
            if(result.error().isPresent() || result.result().isEmpty()) {
                ScriptType.SERVER.console.warn("Invalid structure block: " + entry.getValue());
                ScriptType.SERVER.console.warn(result.error().get().message());
                return this;
            }
            keysMap.put(keyChar, result.result().get());
        }
        return addRequirement(new StructureRequirement(patternList, keysMap));
    }

    /** LOOT TABLE **/

    public CustomMachineJSRecipeBuilder lootTableOutput(String lootTable) {
        return this.lootTableOutput(lootTable, 0.0F);
    }

    public CustomMachineJSRecipeBuilder lootTableOutput(String lootTable, float luck) {
        if (!Utils.isResourceNameValid(lootTable)) {
            ScriptType.SERVER.console.warn("Invalid loot table id: " + lootTable);
            return this;
        }
        ResourceLocation tableLoc = new ResourceLocation(lootTable);
        return addRequirement(new LootTableRequirement(tableLoc, luck));
    }

    /** DROP **/

    public CustomMachineJSRecipeBuilder checkDrop(ItemStackJS item, int amount, int radius) {
        return checkDrops(new ItemStackJS[]{item}, amount, radius, true);
    }

    public CustomMachineJSRecipeBuilder checkAnyDrop(int amount, int radius) {
        return checkDrops(new ItemStackJS[]{}, amount, radius, false);
    }

    public CustomMachineJSRecipeBuilder checkDrops(ItemStackJS[] items, int amount, int radius) {
        return checkDrops(items, amount, radius, true);
    }

    public CustomMachineJSRecipeBuilder checkDrops(ItemStackJS[] items, int amount, int radius, boolean whitelist) {
        if(items.length == 0) {
            ScriptType.SERVER.console.warn("Invalid Drop requirement, checkDrop method must have at least 1 item defined when using whitelist mode");
            return this;
        }
        List<IIngredient<Item>> input = Arrays.stream(items).map(ItemStackJS::getItem).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CHECK, input, whitelist, Items.AIR, nbtFromStack(items[0]), amount, radius));
    }

    public CustomMachineJSRecipeBuilder consumeDropOnStart(ItemStackJS item, int amount, int radius) {
        return consumeDropsOnStart(new ItemStackJS[]{item}, amount, radius, true);
    }

    public CustomMachineJSRecipeBuilder consumeAnyDropOnStart(int amount, int radius) {
        return consumeDropsOnStart(new ItemStackJS[]{}, amount, radius, false);
    }

    public CustomMachineJSRecipeBuilder consumeDropsOnStart(ItemStackJS[] items, int amount, int radius) {
        return consumeDropsOnStart(items, amount, radius, true);
    }

    public CustomMachineJSRecipeBuilder consumeDropsOnStart(ItemStackJS[] items, int amount, int radius, boolean whitelist) {
        if(items.length == 0) {
            ScriptType.SERVER.console.warn("Invalid Drop requirement, consumeDropOnStart method must have at least 1 item defined when using whitelist mode");
            return this;
        }
        List<IIngredient<Item>> input = Arrays.stream(items).map(ItemStackJS::getItem).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.CONSUME, input, whitelist, Items.AIR, nbtFromStack(items[0]), amount, radius));
    }

    public CustomMachineJSRecipeBuilder consumeDropOnEnd(ItemStackJS item, int amount, int radius) {
        return consumeDropsOnEnd(new ItemStackJS[]{item}, amount, radius, true);
    }

    public CustomMachineJSRecipeBuilder consumeAnyDropOnEnd(int amount, int radius) {
        return consumeDropsOnEnd(new ItemStackJS[]{}, amount, radius, false);
    }

    public CustomMachineJSRecipeBuilder consumeDropsOnEnd(ItemStackJS[] items, int amount, int radius) {
        return consumeDropsOnEnd(items, amount, radius, true);
    }

    public CustomMachineJSRecipeBuilder consumeDropsOnEnd(ItemStackJS[] items, int amount, int radius, boolean whitelist) {
        if(items.length == 0) {
            ScriptType.SERVER.console.warn("Invalid Drop requirement, consumeDropOnEnd method must have at least 1 item defined when using whitelist mode");
            return this;
        }
        List<IIngredient<Item>> input = Arrays.stream(items).map(ItemStackJS::getItem).map(ItemIngredient::new).collect(Collectors.toList());
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.CONSUME, input, whitelist, Items.AIR, nbtFromStack(items[0]), amount, radius));
    }

    public CustomMachineJSRecipeBuilder dropItemOnStart(ItemStackJS stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.INPUT, DropRequirement.Action.PRODUCE, Collections.emptyList(), true, stack.getItem(), nbtFromStack(stack), stack.getCount(), 1));
    }

    public CustomMachineJSRecipeBuilder dropItemOnEnd(ItemStackJS stack) {
        return addRequirement(new DropRequirement(RequirementIOMode.OUTPUT, DropRequirement.Action.PRODUCE, Collections.emptyList(), true, stack.getItem(), nbtFromStack(stack), stack.getCount(), 1));
    }

    /** FUNCTION **/

    public CustomMachineJSRecipeBuilder requireFunctionToStart(RecipeFunction function) {
        return addRequirement(new FunctionRequirement(FunctionRequirement.Phase.CHECK, new KJSFunction(function)));
    }

    public CustomMachineJSRecipeBuilder requireFunctionOnStart(RecipeFunction function) {
        return addRequirement(new FunctionRequirement(FunctionRequirement.Phase.START, new KJSFunction(function)));
    }

    public CustomMachineJSRecipeBuilder requireFunctionEachTick(RecipeFunction function) {
        return addRequirement(new FunctionRequirement(FunctionRequirement.Phase.TICK, new KJSFunction(function)));
    }

    public CustomMachineJSRecipeBuilder requireFunctionOnEnd(RecipeFunction function) {
        return addRequirement(new FunctionRequirement(FunctionRequirement.Phase.END, new KJSFunction(function)));
    }

    private @Nullable CompoundTag nbtFromStack(ItemStackJS stack) {
        CompoundTag nbt = stack.getNbt();
        if(nbt == null || nbt.isEmpty())
            return null;
        if(nbt.contains("Damage", Tag.TAG_INT) && nbt.getInt("Damage") == 0)
            nbt.remove("Damage");
        if(nbt.isEmpty())
            return null;
        return nbt;
    }
}