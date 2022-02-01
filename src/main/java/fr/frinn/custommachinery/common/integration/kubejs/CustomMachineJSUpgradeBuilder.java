package fr.frinn.custommachinery.common.integration.kubejs;

import dev.latvian.kubejs.event.EventJS;
import dev.latvian.kubejs.script.ScriptType;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.data.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.data.upgrade.RecipeModifier;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.crafttweaker.RequirementTypeCTBrackets;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class CustomMachineJSUpgradeBuilder {

    private final Item item;
    private String tooltip;
    private final List<ResourceLocation> machines;
    private final List<RecipeModifier> modifiers;
    private final int maxAmount;

    public CustomMachineJSUpgradeBuilder(Item item, int maxAmount) {
        this.item = item;
        this.tooltip = "custommachinery.upgrade.tooltip";
        this.maxAmount = maxAmount;
        this.machines = new ArrayList<>();
        this.modifiers = new ArrayList<>();
    }

    public MachineUpgrade build() {
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
        return new MachineUpgrade(this.item, this.machines, this.modifiers, tooltip, this.maxAmount);
    }

    //TODO: add method that accept a list of machines
    public CustomMachineJSUpgradeBuilder machine(String string) {
        final ResourceLocation machine;
        try {
            machine = new ResourceLocation(string);
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid Machine ID: " + string + "\n" + e.getMessage());
        }
        this.machines.add(machine);
        return this;
    }

    public CustomMachineJSUpgradeBuilder tooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public CustomMachineJSUpgradeBuilder addInput(String requirement, double value) {
        return addInput(requirement, value, "", 1.0D);
    }

    public CustomMachineJSUpgradeBuilder addInput(String requirement, double value, Object object) {
        if(object instanceof CharSequence)
            return addInput(requirement, value, ((CharSequence)object).toString(), 1.0D);
        else if(object instanceof Number)
            return addInput(requirement, value, "", ((Number)object).doubleValue());

        ScriptType.SERVER.console.warnf("Invalid argument for \"MachineUpgrade#addInput\", should be either String or Number but %s found, ignoring.", object.getClass());
        return this;
    }

    public CustomMachineJSUpgradeBuilder addInput(String requirement, double value, String target, double chance) {
        return modifier(RequirementIOMode.INPUT, RecipeModifier.OPERATION.ADDITION, requirement, value, target, chance);
    }

    public CustomMachineJSUpgradeBuilder mulInput(String requirement, double value) {
        return mulInput(requirement, value, "", 1.0D);
    }

    public CustomMachineJSUpgradeBuilder mulInput(String requirement, double value, Object object) {
        if(object instanceof CharSequence)
            return mulInput(requirement, value, ((CharSequence)object).toString(), 1.0D);
        else if(object instanceof Number)
            return mulInput(requirement, value, "", ((Number)object).doubleValue());

        ScriptType.SERVER.console.warnf("Invalid argument for \"MachineUpgrade#addInput\", should be either String or Number but %s found, ignoring.", object.getClass());
        return this;
    }

    public CustomMachineJSUpgradeBuilder mulInput(String requirement, double value, String target, double chance) {
        return modifier(RequirementIOMode.INPUT, RecipeModifier.OPERATION.MULTIPLICATION, requirement, value, target, chance);
    }

    public CustomMachineJSUpgradeBuilder addOutput(String requirement, double value) {
        return addOutput(requirement, value, "", 1.0D);
    }

    public CustomMachineJSUpgradeBuilder addOutput(String requirement, double value, Object object) {
        if(object instanceof CharSequence)
            return addOutput(requirement, value, ((CharSequence)object).toString(), 1.0D);
        else if(object instanceof Number)
            return addOutput(requirement, value, "", ((Number)object).doubleValue());

        ScriptType.SERVER.console.warnf("Invalid argument for \"MachineUpgrade#addInput\", should be either String or Number but %s found, ignoring.", object.getClass());
        return this;
    }

    public CustomMachineJSUpgradeBuilder addOutput(String requirement, double value, String target, double chance) {
        return modifier(RequirementIOMode.OUTPUT, RecipeModifier.OPERATION.ADDITION, requirement, value, target, chance);
    }

    public CustomMachineJSUpgradeBuilder mulOutput(String requirement, double value) {
        return mulOutput(requirement, value, "", 1.0D);
    }

    public CustomMachineJSUpgradeBuilder mulOutput(String requirement, double value, Object object) {
        if(object instanceof CharSequence)
            return mulOutput(requirement, value, ((CharSequence)object).toString(), 1.0D);
        else if(object instanceof Number)
            return mulOutput(requirement, value, "", ((Number)object).doubleValue());

        ScriptType.SERVER.console.warnf("Invalid argument for \"MachineUpgrade#addInput\", should be either String or Number but %s found, ignoring.", object.getClass());
        return this;
    }

    public CustomMachineJSUpgradeBuilder mulOutput(String requirement, double value, String target, double chance) {
        return modifier(RequirementIOMode.OUTPUT, RecipeModifier.OPERATION.MULTIPLICATION, requirement, value, target, chance);
    }

    public CustomMachineJSUpgradeBuilder modifier(RequirementIOMode mode, RecipeModifier.OPERATION operation, String requirement, double value, String target, double chance) {
        RequirementType<?> type = Registration.REQUIREMENT_TYPE_REGISTRY.get().getValue(ResourceLocation.tryCreate(requirement));
        if(type == null) {
            ScriptType.SERVER.console.warnf("Invalid requirement type : %s, skipping modifier.", requirement);
            return this;
        }
        RecipeModifier modifier = new RecipeModifier(type, mode, operation, value, target, chance);
        this.modifiers.add(modifier);
        return this;
    }

    public static class UpgradeEvent extends EventJS {

        private final List<CustomMachineJSUpgradeBuilder> builders = new ArrayList<>();

        //TODO: add default method with maxAmount = 64
        public CustomMachineJSUpgradeBuilder create(Item item, int maxAmount) {
            CustomMachineJSUpgradeBuilder builder = new CustomMachineJSUpgradeBuilder(item, maxAmount);
            this.builders.add(builder);
            return builder;
        }

        public List<CustomMachineJSUpgradeBuilder> getBuilders() {
            return this.builders;
        }
    }
}
