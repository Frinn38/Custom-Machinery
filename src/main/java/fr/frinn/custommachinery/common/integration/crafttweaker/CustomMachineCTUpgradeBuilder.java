package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.actions.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.data.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.data.upgrade.RecipeModifier;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.openzen.zencode.java.ZenCodeType.*;

import java.util.ArrayList;
import java.util.List;

@ZenRegister
@Name("mods.custommachinery.CMUpgradeBuilder")
public class CustomMachineCTUpgradeBuilder {

    private final Item item;
    private String tooltip;
    private final List<ResourceLocation> machines;
    private final List<RecipeModifier> modifiers;
    private final int maxAmount;

    public CustomMachineCTUpgradeBuilder(Item item, int maxAmount) {
        this.item = item;
        this.tooltip = "custommachinery.upgrade.tooltip";
        this.maxAmount = maxAmount;
        this.machines = new ArrayList<>();
        this.modifiers = new ArrayList<>();
    }

    @Method
    public static CustomMachineCTUpgradeBuilder create(Item item, @OptionalInt(64) int maxAmount) {
        return new CustomMachineCTUpgradeBuilder(item, maxAmount);
    }

    @Method
    public void build() {
        if(this.machines.isEmpty())
            throw new IllegalArgumentException("You must specify at least 1 machine for machine upgrade item: " + this.item.getRegistryName());
        if(this.modifiers.isEmpty())
            throw new IllegalArgumentException("You must specify at least 1 recipe modifier for machine upgrade item: " + this.item.getRegistryName());
        ITextComponent tooltip;
        try {
            tooltip = ITextComponent.Serializer.getComponentFromJson(this.tooltip);
        } catch (Exception e) {
            tooltip = new TranslationTextComponent(this.tooltip);
        }
        MachineUpgrade upgrade = new MachineUpgrade(this.item, this.machines, this.modifiers, tooltip, this.maxAmount);
        CraftTweakerAPI.apply(new AddMachineUpgradeAction(upgrade));
    }

    @Method
    public CustomMachineCTUpgradeBuilder machine(String string) {
        final ResourceLocation machine;
        try {
            machine = new ResourceLocation(string);
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid Machine ID: " + string + "\n" + e.getMessage());
        }
        this.machines.add(machine);
        return this;
    }

    @Method
    public CustomMachineCTUpgradeBuilder tooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Method
    public CustomMachineCTUpgradeBuilder addInput(RequirementTypeCTBrackets.CTRequirementType type, double value, @OptionalString String target, @OptionalDouble(1.0D) double chance) {
        RecipeModifier modifier = new RecipeModifier(type.getType(), RequirementIOMode.INPUT, RecipeModifier.OPERATION.ADDITION, value, target, chance);
        this.modifiers.add(modifier);
        return this;
    }

    @Method
    public CustomMachineCTUpgradeBuilder mulInput(RequirementTypeCTBrackets.CTRequirementType type, double value, @OptionalString String target, @OptionalDouble(1.0D) double chance) {
        RecipeModifier modifier = new RecipeModifier(type.getType(), RequirementIOMode.INPUT, RecipeModifier.OPERATION.MULTIPLICATION, value, target, chance);
        this.modifiers.add(modifier);
        return this;
    }

    @Method
    public CustomMachineCTUpgradeBuilder addOutput(RequirementTypeCTBrackets.CTRequirementType type, double value, @OptionalString String target, @OptionalDouble(1.0D) double chance) {
        RecipeModifier modifier = new RecipeModifier(type.getType(), RequirementIOMode.OUTPUT, RecipeModifier.OPERATION.ADDITION, value, target, chance);
        this.modifiers.add(modifier);
        return this;
    }

    @Method
    public CustomMachineCTUpgradeBuilder mulOutput(RequirementTypeCTBrackets.CTRequirementType type, double value, @OptionalString String target, @OptionalDouble(1.0D) double chance) {
        RecipeModifier modifier = new RecipeModifier(type.getType(), RequirementIOMode.OUTPUT, RecipeModifier.OPERATION.MULTIPLICATION, value, target, chance);
        this.modifiers.add(modifier);
        return this;
    }

    public static class AddMachineUpgradeAction implements IRuntimeAction {

        private final MachineUpgrade upgrade;

        public AddMachineUpgradeAction(MachineUpgrade upgrade) {
            this.upgrade = upgrade;
        }

        @Override
        public void apply() {
            CustomMachinery.UPGRADES.add(this.upgrade);
        }

        @Override
        public String describe() {
            return "Add a custom machine upgrade for item: " + this.upgrade.getItem().getRegistryName();
        }
    }
}
