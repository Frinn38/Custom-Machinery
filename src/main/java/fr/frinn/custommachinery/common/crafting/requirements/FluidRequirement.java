package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.FluidIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fluids.FluidStack;

import java.util.Random;

public class FluidRequirement extends AbstractRequirement<FluidComponentHandler> implements IChanceableRequirement<FluidComponentHandler>, IJEIIngredientRequirement {

    private static final Fluid DEFAULT_FLUID = Fluids.EMPTY;
    private static final ResourceLocation DEFAULT_TAG = new ResourceLocation(CustomMachinery.MODID, "dummy");

    @SuppressWarnings("deprecation")
    public static final Codec<FluidRequirement> CODEC = RecordCodecBuilder.create(fluidRequirementInstance ->
            fluidRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    Registry.FLUID.optionalFieldOf("fluid", DEFAULT_FLUID).forGetter(requirement -> requirement.fluid),
                    ResourceLocation.CODEC.optionalFieldOf("tag", DEFAULT_TAG).forGetter(requirement -> requirement.tag != null ? Utils.getFluidTagID(requirement.tag) : DEFAULT_TAG),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    Codec.DOUBLE.optionalFieldOf("chance", 1.0D).forGetter(requirement -> requirement.chance)
            ).apply(fluidRequirementInstance, FluidRequirement::new)
    );

    private Fluid fluid;
    private ITag<Fluid> tag;
    private int amount;
    private double chance;

    public FluidRequirement(MODE mode, Fluid fluid, ResourceLocation tagLocation, int amount, double chance) {
        super(mode);
        this.amount = amount;
        if(mode == MODE.OUTPUT) {
            if(fluid != DEFAULT_FLUID && fluid != null)
                this.fluid = fluid;
            else throw new IllegalArgumentException("You must specify a fluid for an Output Fluid Requirement");
        } else {
            if(fluid == DEFAULT_FLUID || fluid == null) {
                if(tagLocation == DEFAULT_TAG || tagLocation == null)
                    throw  new IllegalArgumentException("You must specify either a fluid or a fluid tag for an Input Fluid Requirement");
                ITag<Fluid> tag = TagCollectionManager.getManager().getFluidTags().get(tagLocation);
                if(tag == null)
                    throw new IllegalArgumentException("The fluid tag: " + tagLocation + " doesn't exist");
                if(!tag.getAllElements().isEmpty())
                    this.tag = tag;
                else throw new IllegalArgumentException("The fluid tag: " + tagLocation + " doesn't contains any fluid");
            } else {
                this.fluid = fluid;
            }
        }
        this.chance = MathHelper.clamp(chance, 0.0D, 1.0D);;
    }

    @Override
    public RequirementType<FluidRequirement> getType() {
        return Registration.FLUID_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(FluidComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            if(this.fluid != null && this.fluid != DEFAULT_FLUID)
                return component.getFluidAmount(this.fluid) >= amount;
            else if(this.tag != null)
                return this.tag.getAllElements().stream().mapToInt(component::getFluidAmount).sum() >= amount;
            else throw new IllegalStateException("Using Input Fluid Requirement with null fluid and fluid tag");
        }
        else {
            if(this.fluid != null && this.fluid != DEFAULT_FLUID)
                return component.getSpaceForFluid(this.fluid) >= amount;
            else throw new IllegalStateException("Using Output Fluid Requirement with null fluid");
        }

    }

    @Override
    public CraftingResult processStart(FluidComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            if(this.fluid != null && this.fluid != DEFAULT_FLUID) {
                FluidStack stack = new FluidStack(this.fluid, amount);
                int canExtract = component.getFluidAmount(this.fluid);
                if(canExtract >= amount) {
                    component.removeFromInputs(stack);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluid.error.input", new TranslationTextComponent(this.fluid.getAttributes().getTranslationKey()), amount, canExtract));
            } else if(this.tag != null) {
                int maxExtract = this.tag.getAllElements().stream().mapToInt(component::getFluidAmount).sum();
                if(maxExtract >= amount) {
                    int toExtract = amount;
                    for (Fluid fluid : this.tag.getAllElements()) {
                        int canExtract = component.getFluidAmount(fluid);
                        if(canExtract > 0) {
                            canExtract = Math.min(canExtract, toExtract);
                            component.removeFromInputs(new FluidStack(fluid, canExtract));
                            toExtract -= canExtract;
                            if(toExtract == 0)
                                return CraftingResult.success();
                        }
                    }
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluid.error.input", Utils.getFluidTagID(this.tag), amount, maxExtract));
            } else throw new IllegalStateException("Using Input Fluid Requirement with null fluid and fluid tag");
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(FluidComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.OUTPUT) {
            if(this.fluid != null && this.fluid != DEFAULT_FLUID) {
                FluidStack stack = new FluidStack(this.fluid, amount);
                int canAdd =  component.getSpaceForFluid(this.fluid);
                if(canAdd >= amount) {
                    component.addToOutputs(stack);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluid.error.output", amount, new TranslationTextComponent(this.fluid.getAttributes().getTranslationKey())));
            } else throw new IllegalStateException("Using Output Fluid Requirement with null fluid");
        }
        return CraftingResult.pass();
    }

    @Override
    public boolean testChance(FluidComponentHandler component, Random rand, CraftingContext context) {
        double chance = context.getModifiedvalue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    private Lazy<FluidIngredientWrapper> fluidIngredientWrapper = Lazy.of(() -> new FluidIngredientWrapper(this.getMode(), this.fluid, this.amount, this.tag, this.chance, false));
    @Override
    public FluidIngredientWrapper getJEIIngredientWrapper() {
        return this.fluidIngredientWrapper.get();
    }
}
