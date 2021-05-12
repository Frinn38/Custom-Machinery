package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
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

public class FluidPerTickRequirement extends AbstractTickableRequirement<FluidComponentHandler> implements IChanceableRequirement, IJEIIngredientRequirement {

    private static final Fluid DEFAULT_FLUID = Fluids.EMPTY;
    private static final ResourceLocation DEFAULT_TAG = new ResourceLocation(CustomMachinery.MODID, "dummy");

    @SuppressWarnings("deprecation")
    public static final Codec<FluidPerTickRequirement> CODEC = RecordCodecBuilder.create(fluidPerTickRequirementInstance ->
            fluidPerTickRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractTickableRequirement::getMode),
                    Registry.FLUID.optionalFieldOf("fluid", DEFAULT_FLUID).forGetter(requirement -> requirement.fluid),
                    ResourceLocation.CODEC.optionalFieldOf("tag", DEFAULT_TAG).forGetter(requirement -> requirement.tag != null ? Utils.getFluidTagID(requirement.tag) : DEFAULT_TAG),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    Codec.DOUBLE.optionalFieldOf("chance", 1.0D).forGetter(requirement -> requirement.chance)
            ).apply(fluidPerTickRequirementInstance, FluidPerTickRequirement::new)
    );

    private Fluid fluid;
    private ITag<Fluid> tag;
    private int amount;
    private double chance;

    public FluidPerTickRequirement(MODE mode, Fluid fluid, ResourceLocation tagLocation, int amount, double chance) {
        super(mode);
        this.amount = amount;
        if(mode == MODE.OUTPUT) {
            if(fluid != DEFAULT_FLUID)
                this.fluid = fluid;
            else throw new IllegalArgumentException("You must specify a fluid for an Output Fluid Per Tick Requirement");
        } else {
            if(fluid == DEFAULT_FLUID) {
                if(tagLocation == DEFAULT_TAG)
                    throw  new IllegalArgumentException("You must specify either a fluid or a fluid tag for an Input Fluid Per Tick Requirement");
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
    public RequirementType getType() {
        return Registration.FLUID_PER_TICK_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(FluidComponentHandler component) {
        return true;
    }

    @Override
    public CraftingResult processStart(FluidComponentHandler component) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(FluidComponentHandler component) {
        if(getMode() == MODE.INPUT) {
            if(this.fluid != null && this.fluid != DEFAULT_FLUID) {
                FluidStack stack = new FluidStack(this.fluid, this.amount);
                int canExtract = component.getFluidAmount(this.fluid);
                if(canExtract >= this.amount) {
                    component.removeFromInputs(stack);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluid.error.input", new TranslationTextComponent(this.fluid.getAttributes().getTranslationKey()), this.amount, canExtract));
            } else if(this.tag != null) {
                int maxExtract = this.tag.getAllElements().stream().mapToInt(component::getFluidAmount).sum();
                if(maxExtract >= this.amount) {
                    int toExtract = this.amount;
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
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluid.error.input", Utils.getFluidTagID(this.tag), this.amount, maxExtract));
            } else throw new IllegalStateException("Using Input Fluid Per Tick Requirement with null fluid and fluid tag");
        }
        else {
            if(this.fluid != null && this.fluid != DEFAULT_FLUID) {
                FluidStack stack = new FluidStack(this.fluid, this.amount);
                int canInsert = component.getSpaceForFluid(this.fluid);
                if(canInsert >= this.amount) {
                    component.addToOutputs(stack);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.fluidpertick.error.output", this.amount, this.fluid.getRegistryName()));
            } else throw new IllegalStateException("Using Output Fluid Per Tick Requirement with null fluid");
        }
    }

    @Override
    public CraftingResult processEnd(FluidComponentHandler component) {
        return CraftingResult.pass();
    }

    @Override
    public boolean testChance(Random rand) {
        return rand.nextDouble() > this.chance;
    }

    private Lazy<FluidIngredientWrapper> fluidIngredientWrapper = Lazy.of(() -> new FluidIngredientWrapper(this.getMode(), this.fluid, this.amount, this.tag, this.chance, true));
    @Override
    public FluidIngredientWrapper getJEIIngredientWrapper() {
        return this.fluidIngredientWrapper.get();
    }
}
