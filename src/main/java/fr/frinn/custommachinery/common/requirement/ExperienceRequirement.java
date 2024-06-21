package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.ExperienceIngredientWrapper;
import fr.frinn.custommachinery.common.component.ExperienceMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.integration.jei.Experience;
import fr.frinn.custommachinery.impl.integration.jei.Experience.Form;
import fr.frinn.custommachinery.impl.requirement.AbstractChanceableRequirement;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class ExperienceRequirement extends AbstractChanceableRequirement<ExperienceMachineComponent> implements IJEIIngredientRequirement<Experience> {
  public static final NamedCodec<ExperienceRequirement> CODEC = NamedCodec.record(experienceRequirementInstance ->
    experienceRequirementInstance.group(
      RequirementIOMode.CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
      NamedCodec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
      NamedCodec.enumCodec(Form.class).optionalFieldOf("form", Form.POINT).forGetter(requirement -> requirement.type),
      NamedCodec.doubleRange(0.0, 1.0).optionalFieldOf("chance", 1.0D).forGetter(AbstractChanceableRequirement::getChance)
    ).apply(
      experienceRequirementInstance, (mode, amount, type,chance) -> {
        ExperienceRequirement requirement = new ExperienceRequirement(mode, amount, type);
        requirement.setChance(chance);
        return requirement;
      }
    ), "Experience requirement"
  );

  private final int amount;
  private final Form type;

  public ExperienceRequirement(RequirementIOMode mode, int amount, Form type) {
    super(mode);
    this.amount = amount;
    this.type = type;
  }

  @Override
  public RequirementType<ExperienceRequirement> getType() {
    return Registration.EXPERIENCE_REQUIREMENT.get();
  }

  @Override
  public MachineComponentType<ExperienceMachineComponent> getComponentType() {
    return Registration.EXPERIENCE_MACHINE_COMPONENT.get();
  }

  public Form getForm() {
    return type;
  }

  @Override
  public boolean test(ExperienceMachineComponent component, ICraftingContext context) {
    int amount = (int) context.getIntegerModifiedValue(this.amount, this, null);
    if(getForm().isPoint())
      if (getMode() == RequirementIOMode.INPUT)
        return component.extractXp(amount, true) == amount;
      else
        return component.receiveXp(amount, true) == amount;
    else {
      if (getMode() == RequirementIOMode.INPUT)
        return component.extractLevel(amount, true) == amount;
      else
        return component.receiveLevel(amount, true) == amount;
    }
  }

  @Override
  public CraftingResult processStart(ExperienceMachineComponent component, ICraftingContext context) {
    int amount = (int) context.getIntegerModifiedValue(this.amount, this, null);
    if (getMode() == RequirementIOMode.INPUT) {
      if (getForm().isPoint()) {
        int canExtract = component.extractXp(amount, true);
        if (canExtract == amount) {
          component.extractXp(amount, false);
          return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.xp.point.error.input", amount, canExtract));
      } else {
        int canExtract = component.extractLevel(amount, true);
        if (canExtract == amount) {
          component.extractLevel(amount, false);
          return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.xp.level.error.input", amount, canExtract));
      }
    }
    return CraftingResult.pass();
  }

  @Override
  public CraftingResult processEnd(ExperienceMachineComponent component, ICraftingContext context) {
    int amount = (int) context.getIntegerModifiedValue(this.amount, this, null);
    if (getMode() == RequirementIOMode.OUTPUT) {
      if (getForm().isPoint()) {
        int canReceive = component.receiveXp(amount, true);
        if(canReceive == amount) {
          component.receiveXp(amount, false);
          return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.xp.point.error.output", amount));
      } else {
        int canReceive = component.receiveLevel(amount, true);
        if(canReceive == amount) {
          component.receiveLevel(amount, false);
          return CraftingResult.success();
        }
        return CraftingResult.error(Component.translatable("custommachinery.requirements.xp.level.error.output", amount));
      }
    }
    return CraftingResult.pass();
  }

  @Override
  public List<IJEIIngredientWrapper<Experience>> getJEIIngredientWrappers(IMachineRecipe recipe) {
    return Collections.singletonList(new ExperienceIngredientWrapper(this.getMode(), this.amount, getChance(), false, recipe.getRecipeTime(), type));
  }
}
