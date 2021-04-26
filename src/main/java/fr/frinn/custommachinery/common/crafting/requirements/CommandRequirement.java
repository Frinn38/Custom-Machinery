package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.CommandMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

public class CommandRequirement extends AbstractRequirement<CommandMachineComponent> {

    public static final Codec<CommandRequirement> CODEC = RecordCodecBuilder.create(commandRequirementInstance ->
            commandRequirementInstance.group(
                Codec.STRING.fieldOf("command").forGetter(requirement -> requirement.command),
                Codec.INT.optionalFieldOf("permissionlevel", 2).forGetter(requirement -> requirement.permissionLevel),
                Codec.BOOL.optionalFieldOf("log", false).forGetter(requirement -> requirement.log)
            ).apply(commandRequirementInstance, CommandRequirement::new)
    );

    private String command;
    private int permissionLevel;
    private boolean log;

    public CommandRequirement(String command, int permissionLevel, boolean log) {
        super(MODE.OUTPUT);
        this.command = command;
        this.permissionLevel = permissionLevel;
        this.log = log;
    }

    @Override
    public RequirementType getType() {
        return Registration.COMMAND_REQUIREMENT.get();
    }

    @Override
    public boolean test(CommandMachineComponent component) {
        return true;
    }

    @Override
    public CraftingResult processStart(CommandMachineComponent component) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(CommandMachineComponent component) {
        component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<CommandMachineComponent> getComponentType() {
        return Registration.COMMAND_MACHINE_COMPONENT.get();
    }
}
