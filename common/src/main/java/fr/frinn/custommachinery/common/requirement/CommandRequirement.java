package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.CommandMachineComponent;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor.PHASE;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedChanceableRequirement;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.Locale;

public class CommandRequirement extends AbstractDelayedChanceableRequirement<CommandMachineComponent> implements ITickableRequirement<CommandMachineComponent>, IDisplayInfoRequirement {

    public static final NamedCodec<CommandRequirement> CODEC = NamedCodec.record(commandRequirementInstance ->
            commandRequirementInstance.group(
                    NamedCodec.STRING.fieldOf("command").forGetter(requirement -> requirement.command),
                    PHASE.CODEC.fieldOf("phase").forGetter(requirement -> requirement.phase),
                    NamedCodec.INT.optionalFieldOf("permissionlevel", 2).forGetter(requirement -> requirement.permissionLevel),
                    NamedCodec.BOOL.optionalFieldOf("log", false).forGetter(requirement -> requirement.log),
                    NamedCodec.doubleRange(0.0, 1.0).optionalFieldOf("chance", 1.0D).forGetter(AbstractDelayedChanceableRequirement::getChance),
                    NamedCodec.doubleRange(0.0, 1.0).optionalFieldOf("delay", 0.0).forGetter(AbstractDelayedRequirement::getDelay)
            ).apply(commandRequirementInstance, (command, phase, permissionLevel, log, chance, delay) -> {
                CommandRequirement requirement = new CommandRequirement(command, phase, permissionLevel, log);
                requirement.setChance(chance);
                requirement.setDelay(delay);
                return requirement;
            }), "Command requirement"
    );

    private final String command;
    private final MachineProcessor.PHASE phase;
    private final int permissionLevel;
    private final boolean log;

    public CommandRequirement(String command, MachineProcessor.PHASE phase, int permissionLevel, boolean log) {
        super(RequirementIOMode.INPUT);
        this.command = command;
        this.phase = phase;
        this.permissionLevel = permissionLevel;
        this.log = log;
    }

    @Override
    public RequirementType<CommandRequirement> getType() {
        return Registration.COMMAND_REQUIREMENT.get();
    }

    @Override
    public boolean test(CommandMachineComponent component, ICraftingContext context) {
        return true;
    }

    @Override
    public CraftingResult processStart(CommandMachineComponent component, ICraftingContext context) {
        if(this.phase == MachineProcessor.PHASE.STARTING && !isDelayed())
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(CommandMachineComponent component, ICraftingContext context) {
        if(this.phase == MachineProcessor.PHASE.CRAFTING_TICKABLE && !isDelayed())
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(CommandMachineComponent component, ICraftingContext context) {
        if(this.phase == MachineProcessor.PHASE.ENDING && !isDelayed())
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult execute(CommandMachineComponent component, ICraftingContext context) {
        component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<CommandMachineComponent> getComponentType() {
        return Registration.COMMAND_MACHINE_COMPONENT.get();
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.setItemIcon(Items.COMMAND_BLOCK);
        if(!isDelayed())
            info.addTooltip(Component.translatable("custommachinery.requirements.command.info", Component.literal(this.command).withStyle(ChatFormatting.AQUA), this.phase.toString().toLowerCase(Locale.ENGLISH)));
        else
            info.addTooltip(Component.translatable("custommachinery.requirements.command.delay", Component.literal(this.command).withStyle(ChatFormatting.AQUA), (int)(getDelay() * 100) + "%"));
    }
}
