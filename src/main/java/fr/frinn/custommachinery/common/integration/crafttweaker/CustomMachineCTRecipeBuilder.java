package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.entity.MCEntityType;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker.impl.managers.RecipeManagerWrapper;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.CustomMachineRecipeBuilder;
import fr.frinn.custommachinery.common.crafting.requirements.*;
import fr.frinn.custommachinery.common.data.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ComparatorMode;
import fr.frinn.custommachinery.common.util.PositionComparator;
import fr.frinn.custommachinery.common.util.TimeComparator;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.world.biome.Biome;
import org.openzen.zencode.java.ZenCodeType.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ZenRegister
@Name("mods.custommachinery.CMRecipeBuilder")
public class CustomMachineCTRecipeBuilder {

    private CustomMachineRecipeBuilder builder;

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
    public void build(String name) {
        final ResourceLocation recipeID;
        try {
            recipeID = new ResourceLocation("crafttweaker", name);
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid Recipe name: " + name + "\n" + e.getMessage());
        }
        CustomMachineRecipe recipe = this.builder.build(recipeID);
        CraftTweakerAPI.apply(new ActionAddRecipe(new RecipeManagerWrapper(Registration.CUSTOM_MACHINE_RECIPE), recipe));
    }

    /** ENERGY **/

    @Method
    public CustomMachineCTRecipeBuilder requireEnergy(int amount, @OptionalDouble(1.0D) double chance) {
        withEnergyRequirement(IRequirement.MODE.INPUT, amount, chance, false);
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder produceEnergy(int amount, @OptionalDouble(1.0D) double chance) {
        withEnergyRequirement(IRequirement.MODE.OUTPUT, amount, chance, false);
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireEnergyPerTick(int amount, @OptionalDouble(1.0D) double chance) {
        withEnergyRequirement(IRequirement.MODE.INPUT, amount, chance, true);
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder produceEnergyPerTick(int amount, @OptionalDouble(1.0D) double chance) {
        withEnergyRequirement(IRequirement.MODE.OUTPUT, amount, chance, true);
        return this;
    }

    /** FLUIDS **/

    @Method
    public CustomMachineCTRecipeBuilder requireFluid(Fluid fluid, int amount, @OptionalDouble(1.0D) double chance) {
        withFluidRequirement(IRequirement.MODE.INPUT, fluid, null, amount, chance, false);
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFluid(MCTag<Fluid> tag, int amount, @OptionalDouble(1.0D) double chance) {
        withFluidRequirement(IRequirement.MODE.INPUT, null, tag, amount, chance, false);
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder produceFluid(Fluid fluid, int amount, @OptionalDouble(1.0D) double chance) {
        withFluidRequirement(IRequirement.MODE.OUTPUT, fluid, null, amount, chance, false);
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFluidPerTick(Fluid fluid, int amount, @OptionalDouble(1.0D) double chance) {
        withFluidRequirement(IRequirement.MODE.INPUT, fluid, null, amount, chance, true);
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireFluidPerTick(MCTag<Fluid> tag, int amount, @OptionalDouble(1.0D) double chance) {
        withFluidRequirement(IRequirement.MODE.INPUT, null, tag, amount, chance, true);
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder produceFluidPerTick(Fluid fluid, int amount, @OptionalDouble(1.0D) double chance) {
        withFluidRequirement(IRequirement.MODE.OUTPUT, fluid, null, amount, chance, true);
        return this;
    }

    /** ITEM **/

    @Method
    public CustomMachineCTRecipeBuilder requireItem(MCItemStack stack, int amount, @OptionalDouble(1.0D) double chance, @OptionalBoolean boolean useDurability) {
        withItemRequirement(IRequirement.MODE.INPUT, stack, null, amount, chance, useDurability);
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireItem(MCTag<Item> tag, int amount, @OptionalDouble(1.0D) double chance, @OptionalBoolean boolean useDurability) {
        withItemRequirement(IRequirement.MODE.INPUT, null, tag, amount, chance, useDurability);
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder produceItem(MCItemStack stack, int amount, @OptionalDouble(1.0D) double chance, @OptionalBoolean boolean useDurability) {
        withItemRequirement(IRequirement.MODE.OUTPUT, stack, null, amount, chance, useDurability);
        return this;
    }

    /** TIME **/

    @Method
    public CustomMachineCTRecipeBuilder requireTimes(String[] times) {
        List<TimeComparator> timeComparators = Stream.of(times).map(s -> Codecs.TIME_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(CraftTweakerAPI::logError).orElseThrow(() -> new IllegalArgumentException("Invalid time comparator: " + s)).getFirst()).collect(Collectors.toList());
        if(!timeComparators.isEmpty())
            this.builder.withRequirement(new TimeRequirement(timeComparators));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireTime(String time) {
        TimeComparator timeComparator = Codecs.TIME_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(time)).resultOrPartial(CraftTweakerAPI::logError).orElseThrow(() -> new IllegalArgumentException("Invalid time comparator: " + time)).getFirst();
        this.builder.withRequirement(new TimeRequirement(Collections.singletonList(timeComparator)));
        return this;
    }

    /** POSITION **/

    @Method
    public CustomMachineCTRecipeBuilder requirePositions(String[] positions) {
        List<PositionComparator> positionComparators = Stream.of(positions).map(s -> Codecs.POSITION_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).resultOrPartial(CraftTweakerAPI::logError).orElseThrow(() -> new IllegalArgumentException("Invalid position comparator: " + s)).getFirst()).collect(Collectors.toList());
        if(!positionComparators.isEmpty())
            this.builder.withRequirement(new PositionRequirement(positionComparators, Collections.emptyList(), false, Collections.emptyList(), false));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requirePosition(String position) {
        PositionComparator positionComparator = Codecs.POSITION_COMPARATOR_CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(position)).resultOrPartial(CraftTweakerAPI::logError).orElseThrow(() -> new IllegalArgumentException("Invalid position comparator: " + position)).getFirst();
        this.builder.withRequirement(new PositionRequirement(Collections.singletonList(positionComparator), Collections.emptyList(), false, Collections.emptyList(), false));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireBiomes(Biome[] biomes, @OptionalBoolean() boolean blacklist) {
        List<ResourceLocation> biomesID = Arrays.stream(biomes).map(Biome::getRegistryName).collect(Collectors.toList());
        this.builder.withRequirement(new PositionRequirement(Collections.emptyList(), biomesID, blacklist, Collections.emptyList(), false));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireBiome(Biome biome, @OptionalBoolean() boolean blacklist) {
        this.builder.withRequirement(new PositionRequirement(Collections.emptyList(), Collections.singletonList(biome.getRegistryName()), blacklist, Collections.emptyList(), false));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireDimensions(String[] dimensions, @OptionalBoolean boolean blacklist) {
        try {
            List<ResourceLocation> dimensionsID = Arrays.stream(dimensions).map(ResourceLocation::new).collect(Collectors.toList());
            this.builder.withRequirement(new PositionRequirement(Collections.emptyList(), Collections.emptyList(), false, dimensionsID, blacklist));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid dimension ID: " + e.getMessage());
        }
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder requireDimension(String dimension, @OptionalBoolean boolean blacklist) {
        try {
            this.builder.withRequirement(new PositionRequirement(Collections.emptyList(), Collections.emptyList(), false, Collections.singletonList(new ResourceLocation(dimension)), blacklist));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid dimensions ID: " + e.getMessage());
        }
        return this;
    }

    /** FUEL **/

    @Method
    public CustomMachineCTRecipeBuilder requireFuel() {
        this.builder.withRequirement(new FuelRequirement());
        return this;
    }

    /** COMMAND **/

    @Method
    public CustomMachineCTRecipeBuilder runCommandOnStart(String command, @OptionalInt(2) int permissionLevel, @OptionalBoolean boolean log, @OptionalDouble(1.0D) double chance) {
        this.builder.withRequirement(new CommandRequirement(command, CraftingManager.PHASE.STARTING, permissionLevel, log, chance));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder runCommandEachTick(String command, @OptionalInt(2) int permissionLevel, @OptionalBoolean boolean log, @OptionalDouble(1.0D) double chance) {
        this.builder.withRequirement(new CommandRequirement(command, CraftingManager.PHASE.CRAFTING, permissionLevel, log, chance));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder runCommandOnEnd(String command, @OptionalInt(2) int permissionLevel, @OptionalBoolean boolean log, @OptionalDouble(1.0D) double chance) {
        this.builder.withRequirement(new CommandRequirement(command, CraftingManager.PHASE.ENDING, permissionLevel, log, chance));
        return this;
    }

    /** EFFECT **/

    @Method
    public CustomMachineCTRecipeBuilder giveEffectAtEnd(Effect effect, int time, int radius, @OptionalInt(1) int level, @Optional MCEntityType[] filter) {
        this.builder.withRequirement(new EffectRequirement(effect, time, level, radius, Arrays.stream(filter).map(MCEntityType::getInternal).collect(Collectors.toList()), true));
        return this;
    }

    @Method
    public CustomMachineCTRecipeBuilder giveEffectEachTick(Effect effect, int time, int radius, @OptionalInt(1) int level, @Optional MCEntityType[] filter) {
        this.builder.withRequirement(new EffectRequirement(effect, time, level, radius, Arrays.stream(filter).map(MCEntityType::getInternal).collect(Collectors.toList()), false));
        return this;
    }

    /** WEATHER **/

    @Method
    public CustomMachineCTRecipeBuilder requireWeather(String weatherType, @OptionalBoolean(true) boolean onMachine) {
        this.builder.withRequirement(new WeatherRequirement(WeatherMachineComponent.WeatherType.value(weatherType), onMachine));
        return this;
    }

    /** REDSTONE **/

    @Method
    public CustomMachineCTRecipeBuilder requireRedstone(int power, @OptionalString(">=") String comparator) {
        this.builder.withRequirement(new RedstoneRequirement(power, ComparatorMode.value(comparator)));
        return this;
    }

    /** INTERNAL **/

    private void withEnergyRequirement(IRequirement.MODE mode, int amount, double chance, boolean isPerTick) {
        if(isPerTick)
            this.builder.withRequirement(new EnergyPerTickRequirement(mode, amount, chance));
        else
            this.builder.withRequirement(new EnergyRequirement(mode, amount, chance));
    }

    private void withFluidRequirement(IRequirement.MODE mode, Fluid fluid, MCTag<Fluid> tag, int amount, double chance, boolean isPerTick) {
        if(isPerTick) {
            if(fluid != null)
                this.builder.withRequirement(new FluidPerTickRequirement(mode, fluid, null, amount, chance));
            else
                this.builder.withRequirement(new FluidPerTickRequirement(mode, null, tag.getId(), amount, chance));
        } else {
            if(fluid != null)
                this.builder.withRequirement(new FluidRequirement(mode, fluid, null, amount, chance));
            else
                this.builder.withRequirement(new FluidRequirement(mode, null, tag.getId(), amount, chance));
        }
    }

    private void withItemRequirement(IRequirement.MODE mode, MCItemStack stack, MCTag<Item> tag, int amount, double chance, boolean useDurability) {
        if(stack != null)
            this.builder.withRequirement(new ItemRequirement(mode, stack.getInternal().getItem(), null, amount, stack.getInternal().getTag(), chance, useDurability));
        else
            this.builder.withRequirement(new ItemRequirement(mode, null, tag.getId(), amount, null, chance, useDurability));
    }
}
