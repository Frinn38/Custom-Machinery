package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipeBuilder;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.BiomeRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.BlockRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.ButtonRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.ChunkloadRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.CommandRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.DimensionRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.DropRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.DurabilityRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.EffectRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.EnergyPerTickRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.EnergyRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.EntityRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.ExperiencePerTickRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.ExperienceRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.FluidPerTickRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.FluidRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.FuelRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.FunctionRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.ItemFilterRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.ItemRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.ItemTransformRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.LightRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.LootTableRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.PositionRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.RedstoneRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.SkyRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.StructureRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.TimeRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.WeatherRequirementCT;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@ZenRegister
@Name(CTConstants.RECIPE_BUILDER_MACHINE)
public class CustomMachineRecipeCTBuilder implements EnergyRequirementCT<CustomMachineRecipeCTBuilder>, EnergyPerTickRequirementCT<CustomMachineRecipeCTBuilder>,
        FluidRequirementCT<CustomMachineRecipeCTBuilder>, FluidPerTickRequirementCT<CustomMachineRecipeCTBuilder>, ItemRequirementCT<CustomMachineRecipeCTBuilder>,
        ItemTransformRequirementCT<CustomMachineRecipeCTBuilder>, DurabilityRequirementCT<CustomMachineRecipeCTBuilder>, TimeRequirementCT<CustomMachineRecipeCTBuilder>,
        PositionRequirementCT<CustomMachineRecipeCTBuilder>, BiomeRequirementCT<CustomMachineRecipeCTBuilder>, DimensionRequirementCT<CustomMachineRecipeCTBuilder>,
        FuelRequirementCT<CustomMachineRecipeCTBuilder>, CommandRequirementCT<CustomMachineRecipeCTBuilder>, EffectRequirementCT<CustomMachineRecipeCTBuilder>,
        WeatherRequirementCT<CustomMachineRecipeCTBuilder>, RedstoneRequirementCT<CustomMachineRecipeCTBuilder>, EntityRequirementCT<CustomMachineRecipeCTBuilder>,
        LightRequirementCT<CustomMachineRecipeCTBuilder>, BlockRequirementCT<CustomMachineRecipeCTBuilder>, StructureRequirementCT<CustomMachineRecipeCTBuilder>,
        LootTableRequirementCT<CustomMachineRecipeCTBuilder>, DropRequirementCT<CustomMachineRecipeCTBuilder>, FunctionRequirementCT<CustomMachineRecipeCTBuilder>,
        ButtonRequirementCT<CustomMachineRecipeCTBuilder>, SkyRequirementCT<CustomMachineRecipeCTBuilder>, ItemFilterRequirementCT<CustomMachineRecipeCTBuilder>,
        ExperienceRequirementCT<CustomMachineRecipeCTBuilder>, ExperiencePerTickRequirementCT<CustomMachineRecipeCTBuilder>, ChunkloadRequirementCT<CustomMachineRecipeCTBuilder> {

    public static final Map<ResourceLocation, Integer> IDS = new HashMap<>();
    private final CustomMachineRecipeBuilder builder;
    private RecipeRequirement<?, ?> lastRequirement;
    private boolean jei = false;

    public CustomMachineRecipeCTBuilder(CustomMachineRecipeBuilder builder) {
        this.builder = builder;
    }

    @Method
    public static CustomMachineRecipeCTBuilder create(String machine, int time) {
        try {
            return new CustomMachineRecipeCTBuilder(new CustomMachineRecipeBuilder(ResourceLocation.parse(machine), time));
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid Machine name: " + machine + "\n" + e.getMessage());
        }
    }

    @Method
    public void build(@OptionalString String name) {
        final ResourceLocation recipeID;
        try {
            if(!name.isEmpty()) {
                if(name.contains(":"))
                    recipeID = ResourceLocation.parse(name);
                else
                    recipeID = ResourceLocation.fromNamespaceAndPath(CraftTweakerConstants.MOD_ID, name);
            }
            else {
                int uniqueID = IDS.computeIfAbsent(this.builder.getMachine(), m -> 0);
                IDS.put(this.builder.getMachine(), uniqueID + 1);
                recipeID = ResourceLocation.fromNamespaceAndPath(CraftTweakerConstants.MOD_ID, "custom_craft/" + this.builder.getMachine().getNamespace() + "/" + this.builder.getMachine().getPath() + "/" + uniqueID);
            }
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid Recipe name: " + name + "\n" + e.getMessage());
        }
        CustomMachineRecipe recipe = this.builder.build();
        ActionAddRecipe<CustomMachineRecipe> action =  new ActionAddRecipe<>(CustomMachineRecipeCTManager.INSTANCE, new RecipeHolder<>(recipeID, recipe));
        CraftTweakerAPI.apply(action);
    }

    @Override
    public CustomMachineRecipeCTBuilder addRequirement(IRequirement<?> requirement) {
        this.lastRequirement = new RecipeRequirement<>(requirement);
        if(!this.jei)
            this.builder.withRequirement(this.lastRequirement);
        else
            this.builder.withJeiRequirement(this.lastRequirement);
        return this;
    }

    @Override
    public CustomMachineRecipeCTBuilder error(String error, Object... args) {
        CraftTweakerAPI.getLogger(CustomMachinery.MODID).error(error, args);
        return this;
    }

    /** CHANCE **/

    @Method
    public CustomMachineRecipeCTBuilder chance(double chance) {
        if(this.lastRequirement != null)
            this.lastRequirement.setChance(chance);
        else
            CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Can't set chance before adding requirements");
        return this;
    }

    /** HIDE **/

    @Method
    public CustomMachineRecipeCTBuilder hide() {
        this.builder.hide();
        return this;
    }

    /** DELAY **/

    @Method
    public CustomMachineRecipeCTBuilder delay(double delay) {
        if(this.lastRequirement != null)
            this.lastRequirement.setDelay(delay);
        else
            CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Can't set delay before adding requirements");
        return this;
    }

    /** JEI **/

    @Method
    public CustomMachineRecipeCTBuilder jei() {
        this.jei = true;
        return this;
    }

    /** PRIORITY **/

    @Method
    public CustomMachineRecipeCTBuilder priority(int priority) {
        if(!this.jei)
            this.builder.withPriority(priority);
        else
            this.builder.withJeiPriority(priority);
        return this;
    }

    /** ERROR **/

    @Method
    public CustomMachineRecipeCTBuilder resetOnError() {
        this.builder.setResetOnError();
        return this;
    }

    /** INFO **/

    @Method
    public CustomMachineRecipeCTBuilder info(Consumer<DisplayInfoTemplateCT> consumer) {
        if(this.lastRequirement != null) {
            DisplayInfoTemplateCT template = new DisplayInfoTemplateCT();
            consumer.accept(template);
            this.lastRequirement.info = template;
        }
        else
            CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Can't put info for null requirement");
        return this;
    }

    /** APPEARANCE **/

    @Method
    public CustomMachineRecipeCTBuilder appearance(Consumer<MachineAppearanceBuilderCT> consumer) {
        MachineAppearanceBuilderCT builder = new MachineAppearanceBuilderCT();
        consumer.accept(builder);
        this.builder.withAppearance(builder.build());
        return this;
    }

    /** GUI **/

    @Method
    public CustomMachineRecipeCTBuilder gui(MapData... elements) {
        for(MapData data : elements) {
            IGuiElement.CODEC.read(NbtOps.INSTANCE, data.getInternal()).resultOrPartial(s -> {
                CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Error when parsing recipe custom gui element\n" + data + "\n" + s);
            }).ifPresent(this.builder::withGuiElement);
        }
        return this;
    }
}