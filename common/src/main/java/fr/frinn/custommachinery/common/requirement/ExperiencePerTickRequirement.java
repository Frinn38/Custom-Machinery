package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.ExperienceIngredientWrapper;
import fr.frinn.custommachinery.common.component.ExperienceMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.integration.jei.ExperienceStorage;
import fr.frinn.custommachinery.impl.requirement.AbstractChanceableRequirement;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class ExperiencePerTickRequirement extends AbstractChanceableRequirement<ExperienceMachineComponent> implements ITickableRequirement<ExperienceMachineComponent>, IJEIIngredientRequirement<ExperienceStorage> {
  public static final NamedCodec<ExperiencePerTickRequirement> CODEC = NamedCodec.record(experienceRequirementInstance ->
    experienceRequirementInstance.group(
      RequirementIOMode.CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
      NamedCodec.FLOAT.fieldOf("amount").forGetter(requirement -> requirement.amount),
      NamedCodec.doubleRange(0.0, 1.0).optionalFieldOf("chance", 1.0D).forGetter(AbstractChanceableRequirement::getChance)
    ).apply(
      experienceRequirementInstance, (mode, amount, chance) -> {
        ExperiencePerTickRequirement requirement = new ExperiencePerTickRequirement(mode, amount);
        requirement.setChance(chance);
        return requirement;
      }
    ), "Experience requirement"
  );

  private final float amount;

  public ExperiencePerTickRequirement(RequirementIOMode mode, float amount) {
    super(mode);
    this.amount = amount;
  }

  @Override
  public RequirementType<ExperiencePerTickRequirement> getType() {
    return Registration.EXPERIENCE_PER_TICK_REQUIREMENT.get();
  }

  @Override
  public MachineComponentType<ExperienceMachineComponent> getComponentType() {
    return Registration.EXPERIENCE_MACHINE_COMPONENT.get();
  }

  @Override
  public boolean test(ExperienceMachineComponent component, ICraftingContext context) {
    float amount = (float) context.getModifiedValue(this.amount, this, null);
    if (getMode() == RequirementIOMode.INPUT)
      return component.extractRecipeXp(amount, true) == amount;
    else
      return component.receiveRecipeXp(amount, true) == amount;
  }

  @Override
  public CraftingResult processStart(ExperienceMachineComponent component, ICraftingContext context) {
    return CraftingResult.pass();
  }

  @Override
  public CraftingResult processTick(ExperienceMachineComponent component, ICraftingContext context) {
    float amount = (float) context.getModifiedValue(this.amount, this, null);
    if(getMode() == RequirementIOMode.INPUT) {
      float canExtract = component.extractRecipeXp(amount, true);
      if(canExtract == amount) {
        component.extractRecipeXp(amount, false);
        return CraftingResult.success();
      }
      return CraftingResult.error(Component.translatable("custommachinery.requirements.xppertick.error.input", amount, canExtract));
    }
    else {
      float canReceive = component.receiveRecipeXp(amount, true);
      if(canReceive == amount) {
        component.receiveRecipeXp(amount, false);
        return CraftingResult.success();
      }
      return CraftingResult.error(Component.translatable("custommachinery.requirements.xppertick.error.output", amount));
    }
  }

  @Override
  public CraftingResult processEnd(ExperienceMachineComponent component, ICraftingContext context) {
    return CraftingResult.pass();
  }

  @Override
  public List<IJEIIngredientWrapper<ExperienceStorage>> getJEIIngredientWrappers(IMachineRecipe recipe) {
    return Collections.singletonList(new ExperienceIngredientWrapper(this.getMode(), this.amount, getChance(), true, recipe.getRecipeTime()));
  }
}
