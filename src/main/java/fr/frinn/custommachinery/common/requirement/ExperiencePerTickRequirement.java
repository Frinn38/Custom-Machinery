package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.ExperienceIngredientWrapper;
import fr.frinn.custommachinery.common.component.ExperienceMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.integration.jei.Experience;
import fr.frinn.custommachinery.impl.integration.jei.Experience.Form;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public record ExperiencePerTickRequirement(RequirementIOMode mode, int amount, Form form) implements IRequirement<ExperienceMachineComponent>, IJEIIngredientRequirement<Experience> {

    public static final NamedCodec<ExperiencePerTickRequirement> CODEC = NamedCodec.record(experienceRequirementInstance ->
            experienceRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(ExperiencePerTickRequirement::getMode),
                    NamedCodec.INT.fieldOf("amount").forGetter(ExperiencePerTickRequirement::amount),
                    NamedCodec.enumCodec(Form.class).optionalFieldOf("form", Form.POINT).forGetter(ExperiencePerTickRequirement::form)
            ).apply(experienceRequirementInstance, ExperiencePerTickRequirement::new), "Experience requirement"
    );

    @Override
    public RequirementType<ExperiencePerTickRequirement> getType() {
        return Registration.EXPERIENCE_PER_TICK_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<ExperienceMachineComponent> getComponentType() {
      return Registration.EXPERIENCE_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return this.mode;
    }

    @Override
    public boolean test(ExperienceMachineComponent component, ICraftingContext context) {
        int amount = (int) context.getIntegerModifiedValue(this.amount, this, null);
        if (this.form.isPoint()) {
            if (getMode() == RequirementIOMode.INPUT)
                return component.extractXp(amount, true) == amount;
            else
                return component.receiveXp(amount, true) == amount;
        } else {
            if (getMode() == RequirementIOMode.INPUT)
                return component.extractLevel(amount, true) == amount;
            else
                return component.receiveLevel(amount, true) == amount;
        }
    }

    @Override
    public void gatherRequirements(IRequirementList<ExperienceMachineComponent> list) {
        if(this.mode == RequirementIOMode.INPUT)
            list.processEachTick(this::processInputs);
        else
            list.processEachTick(this::processOutputs);
    }

    private CraftingResult processInputs(ExperienceMachineComponent component, ICraftingContext context) {
        int amount = (int) context.getIntegerModifiedValue(this.amount, this, null);
        if (this.form.isPoint()) {
            int canExtract = component.extractXp(amount, true);
            if (canExtract == amount) {
                component.extractXp(amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.xppertick.point.error.input", amount, canExtract));
        } else {
            int canExtract = component.extractLevel(amount, true);
            if (canExtract == amount) {
                component.extractLevel(amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.xppertick.level.error.input", amount, canExtract));
        }
    }

    private CraftingResult processOutputs(ExperienceMachineComponent component, ICraftingContext context) {
        int amount = (int) context.getIntegerModifiedValue(this.amount, this, null);
        if (this.form.isPoint()) {
            int canReceive = component.receiveXp(amount, true);
            if (canReceive == amount) {
                component.receiveXp(amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.xppertick.point.error.output", amount));
        } else {
            int canReceive = component.receiveLevel(amount, true);
            if (canReceive == amount) {
                component.receiveLevel(amount, false);
                return CraftingResult.success();
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.xppertick.level.error.output", amount));
        }
    }

    @Override
    public List<IJEIIngredientWrapper<Experience>> getJEIIngredientWrappers(IMachineRecipe recipe, RecipeRequirement<?, ?> requirement) {
        return Collections.singletonList(new ExperienceIngredientWrapper(this.getMode(), this.amount, requirement.chance(), true, recipe.getRecipeTime(), this.form));
    }
}
