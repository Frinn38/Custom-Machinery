package fr.frinn.custommachinery.common.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.CommandMachineComponent;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedChanceableRequirement;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

import java.util.Locale;

public class CommandRequirement extends AbstractDelayedChanceableRequirement<CommandMachineComponent> implements ITickableRequirement<CommandMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<CommandRequirement> CODEC = RecordCodecBuilder.create(commandRequirementInstance ->
            commandRequirementInstance.group(
                    Codec.STRING.fieldOf("command").forGetter(requirement -> requirement.command),
                    Codecs.PHASE_CODEC.fieldOf("phase").forGetter(requirement -> requirement.phase),
                    CodecLogger.loggedOptional(Codec.INT,"permissionlevel", 2).forGetter(requirement -> requirement.permissionLevel),
                    CodecLogger.loggedOptional(Codec.BOOL,"log", false).forGetter(requirement -> requirement.log),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0),"chance", 1.0D).forGetter(AbstractDelayedChanceableRequirement::getChance),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0), "delay", 0.0).forGetter(AbstractDelayedRequirement::getDelay)
            ).apply(commandRequirementInstance, (command, phase, permissionLevel, log, chance, delay) -> {
                CommandRequirement requirement = new CommandRequirement(command, phase, permissionLevel, log);
                requirement.setChance(chance);
                requirement.setDelay(delay);
                return requirement;
            })
    );

    private final String command;
    private final CraftingManager.PHASE phase;
    private final int permissionLevel;
    private final boolean log;

    public CommandRequirement(String command, CraftingManager.PHASE phase, int permissionLevel, boolean log) {
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
        if(this.phase == CraftingManager.PHASE.STARTING && !isDelayed())
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(CommandMachineComponent component, ICraftingContext context) {
        if(this.phase == CraftingManager.PHASE.CRAFTING_TICKABLE && !isDelayed())
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(CommandMachineComponent component, ICraftingContext context) {
        if(this.phase == CraftingManager.PHASE.ENDING && !isDelayed())
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
            info.addTooltip(new TranslatableComponent("custommachinery.requirements.command.info", new TextComponent(this.command).withStyle(ChatFormatting.AQUA), this.phase.toString().toLowerCase(Locale.ENGLISH)));
        else
            info.addTooltip(new TranslatableComponent("custommachinery.requirements.command.delay", new TextComponent(this.command).withStyle(ChatFormatting.AQUA), (int)(getDelay() * 100) + "%"));
    }
}
