package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipeBuilder;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.BiomeRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.BlockRequirementCT;
import fr.frinn.custommachinery.common.integration.crafttweaker.requirements.ButtonRequirementCT;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@ZenRegister
@Name(CTConstants.RECIPE_BUILDER_CRAFT)
public class CustomCraftRecipeCTBuilder implements EnergyRequirementCT<CustomCraftRecipeCTBuilder>, EnergyPerTickRequirementCT<CustomCraftRecipeCTBuilder>,
        FluidRequirementCT<CustomCraftRecipeCTBuilder>, FluidPerTickRequirementCT<CustomCraftRecipeCTBuilder>, ItemRequirementCT<CustomCraftRecipeCTBuilder>,
        ItemTransformRequirementCT<CustomCraftRecipeCTBuilder>, DurabilityRequirementCT<CustomCraftRecipeCTBuilder>, TimeRequirementCT<CustomCraftRecipeCTBuilder>,
        PositionRequirementCT<CustomCraftRecipeCTBuilder>, BiomeRequirementCT<CustomCraftRecipeCTBuilder>, DimensionRequirementCT<CustomCraftRecipeCTBuilder>,
        FuelRequirementCT<CustomCraftRecipeCTBuilder>, CommandRequirementCT<CustomCraftRecipeCTBuilder>, EffectRequirementCT<CustomCraftRecipeCTBuilder>,
        WeatherRequirementCT<CustomCraftRecipeCTBuilder>, RedstoneRequirementCT<CustomCraftRecipeCTBuilder>, EntityRequirementCT<CustomCraftRecipeCTBuilder>,
        LightRequirementCT<CustomCraftRecipeCTBuilder>, BlockRequirementCT<CustomCraftRecipeCTBuilder>, StructureRequirementCT<CustomCraftRecipeCTBuilder>,
        LootTableRequirementCT<CustomCraftRecipeCTBuilder>, DropRequirementCT<CustomCraftRecipeCTBuilder>, ButtonRequirementCT<CustomCraftRecipeCTBuilder>,
        SkyRequirementCT<CustomCraftRecipeCTBuilder>, ItemFilterRequirementCT<CustomCraftRecipeCTBuilder>, ExperienceRequirementCT<CustomCraftRecipeCTBuilder>,
        ExperiencePerTickRequirementCT<CustomCraftRecipeCTBuilder> {

    public static final Map<ResourceLocation, Integer> IDS = new HashMap<>();
    private final CustomCraftRecipeBuilder builder;
    private RecipeRequirement<?, ?> lastRequirement;
    private boolean jei = false;

    public CustomCraftRecipeCTBuilder(CustomCraftRecipeBuilder builder) {
        this.builder = builder;
    }

    @Method
    public static CustomCraftRecipeCTBuilder create(String machine, IItemStack output) {
        try {
            return new CustomCraftRecipeCTBuilder(new CustomCraftRecipeBuilder(ResourceLocation.parse(machine), output.getImmutableInternal()));
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
        CustomCraftRecipe recipe = this.builder.build();
        ActionAddRecipe<CustomCraftRecipe> action =  new ActionAddRecipe<>(CustomCraftRecipeCTManager.INSTANCE, new RecipeHolder<>(recipeID, recipe));
        CraftTweakerAPI.apply(action);
    }

    @Override
    public CustomCraftRecipeCTBuilder addRequirement(IRequirement<?> requirement) {
        this.lastRequirement = new RecipeRequirement<>(requirement);
        if(!this.jei)
            this.builder.withRequirement(this.lastRequirement);
        else
            this.builder.withJeiRequirement(this.lastRequirement);
        return this;
    }

    @Override
    public CustomCraftRecipeCTBuilder error(String error, Object... args) {
        CraftTweakerAPI.getLogger(CustomMachinery.MODID).error(error, args);
        return this;
    }

    /** CHANCE **/

    @Method
    public CustomCraftRecipeCTBuilder chance(double chance) {
        if(this.lastRequirement != null)
            this.lastRequirement.setChance(chance);
        else
            CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Can't set chance before adding requirements");
        return this;
    }

    /** HIDE **/

    @Method
    public CustomCraftRecipeCTBuilder hide() {
        this.builder.hide();
        return this;
    }

    /** DELAY **/

    @Method
    public CustomCraftRecipeCTBuilder delay(double delay) {
        if(this.lastRequirement != null)
            this.lastRequirement.setDelay(delay);
        else
            CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Can't set delay before adding requirements");
        return this;
    }

    /** JEI **/

    @Method
    public CustomCraftRecipeCTBuilder jei() {
        this.jei = true;
        return this;
    }

    /** PRIORITY **/

    @Method
    public CustomCraftRecipeCTBuilder priority(int priority) {
        if(!this.jei)
            this.builder.withPriority(priority);
        else
            this.builder.withJeiPriority(priority);
        return this;
    }

    /** INFO **/

    @Method
    public CustomCraftRecipeCTBuilder info(Consumer<DisplayInfoTemplateCT> consumer) {
        if(this.lastRequirement != null) {
            DisplayInfoTemplateCT template = new DisplayInfoTemplateCT();
            consumer.accept(template);
            this.lastRequirement.info = template;
        }
        else
            CraftTweakerAPI.getLogger(CustomMachinery.MODID).error("Can't put info for null requirement");
        return this;
    }
}
