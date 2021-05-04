package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.CommandMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;

public class CommandRequirement extends AbstractTickableRequirement<CommandMachineComponent> {

    public static final Codec<CommandRequirement> CODEC = RecordCodecBuilder.create(commandRequirementInstance ->
            commandRequirementInstance.group(
                Codec.STRING.fieldOf("command").forGetter(requirement -> requirement.command),
                Codecs.PHASE_CODEC.fieldOf("phase").forGetter(requirement -> requirement.phase),
                Codec.INT.optionalFieldOf("permissionlevel", 2).forGetter(requirement -> requirement.permissionLevel),
                Codec.BOOL.optionalFieldOf("log", false).forGetter(requirement -> requirement.log)
            ).apply(commandRequirementInstance, CommandRequirement::new)
    );

    private String command;
    private CraftingManager.PHASE phase;
    private int permissionLevel;
    private boolean log;

    public CommandRequirement(String command, CraftingManager.PHASE phase, int permissionLevel, boolean log) {
        super(MODE.INPUT);
        this.command = command;
        this.phase = phase;
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
        if(this.phase == CraftingManager.PHASE.STARTING)
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(CommandMachineComponent component) {
        if(this.phase == CraftingManager.PHASE.CRAFTING)
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(CommandMachineComponent component) {
        if(this.phase == CraftingManager.PHASE.ENDING)
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<CommandMachineComponent> getComponentType() {
        return Registration.COMMAND_MACHINE_COMPONENT.get();
    }
}
