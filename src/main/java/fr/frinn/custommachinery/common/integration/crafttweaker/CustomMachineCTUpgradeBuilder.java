package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.integration.crafttweaker.RequirementTypeCTBrackets.CTRequirementType;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.RecipeModifier;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalDouble;
import org.openzen.zencode.java.ZenCodeType.OptionalInt;
import org.openzen.zencode.java.ZenCodeType.OptionalString;

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
        Component tooltip;
        try {
            tooltip = Component.Serializer.fromJson(this.tooltip);
        } catch (Exception e) {
            tooltip = new TranslatableComponent(this.tooltip);
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
    public CustomMachineCTUpgradeBuilder addInput(CTRequirementType type, double value, @OptionalString String target, @OptionalDouble double chance) {
        RecipeModifier modifier = new RecipeModifier(type.getType(), RequirementIOMode.INPUT, RecipeModifier.OPERATION.ADDITION, value, target, chance);
        this.modifiers.add(modifier);
        return this;
    }

    @Method
    public CustomMachineCTUpgradeBuilder mulInput(CTRequirementType type, double value, @OptionalString String target, @OptionalDouble double chance) {
        RecipeModifier modifier = new RecipeModifier(type.getType(), RequirementIOMode.INPUT, RecipeModifier.OPERATION.MULTIPLICATION, value, target, chance);
        this.modifiers.add(modifier);
        return this;
    }

    @Method
    public CustomMachineCTUpgradeBuilder addOutput(CTRequirementType type, double value, @OptionalString String target, @OptionalDouble double chance) {
        RecipeModifier modifier = new RecipeModifier(type.getType(), RequirementIOMode.OUTPUT, RecipeModifier.OPERATION.ADDITION, value, target, chance);
        this.modifiers.add(modifier);
        return this;
    }

    @Method
    public CustomMachineCTUpgradeBuilder mulOutput(CTRequirementType type, double value, @OptionalString String target, @OptionalDouble double chance) {
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
            CustomMachinery.UPGRADES.addUpgrade(this.upgrade);
        }

        @Override
        public String describe() {
            return "Add a custom machine upgrade for item: " + this.upgrade.getItem().getRegistryName();
        }
    }
}
