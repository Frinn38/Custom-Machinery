package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.CommandMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Locale;
import java.util.Random;

public class CommandRequirement extends AbstractTickableRequirement<CommandMachineComponent> implements IChanceableRequirement<CommandMachineComponent>, IDisplayInfoRequirement<CommandMachineComponent> {

    public static final Codec<CommandRequirement> CODEC = RecordCodecBuilder.create(commandRequirementInstance ->
            commandRequirementInstance.group(
                    Codec.STRING.fieldOf("command").forGetter(requirement -> requirement.command),
                    Codecs.PHASE_CODEC.fieldOf("phase").forGetter(requirement -> requirement.phase),
                    CodecLogger.loggedOptional(Codec.INT,"permissionlevel", 2).forGetter(requirement -> requirement.permissionLevel),
                    CodecLogger.loggedOptional(Codec.BOOL,"log", false).forGetter(requirement -> requirement.log),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0),"chance", 1.0D).forGetter(requirement -> requirement.chance),
                    CodecLogger.loggedOptional(Codec.BOOL,"jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(commandRequirementInstance, (command, phase, permissionLevel, log, chance, jeiVisible) -> {
                CommandRequirement requirement = new CommandRequirement(command, phase, permissionLevel, log);
                requirement.setChance(chance);
                requirement.setJeiVisible(jeiVisible);
                return requirement;
            })
    );

    private String command;
    private CraftingManager.PHASE phase;
    private int permissionLevel;
    private boolean log;
    private double chance = 1.0D;
    private boolean jeiVisible = true;

    public CommandRequirement(String command, CraftingManager.PHASE phase, int permissionLevel, boolean log) {
        super(MODE.INPUT);
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
    public boolean test(CommandMachineComponent component, CraftingContext context) {
        return true;
    }

    @Override
    public CraftingResult processStart(CommandMachineComponent component, CraftingContext context) {
        if(this.phase == CraftingManager.PHASE.STARTING)
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(CommandMachineComponent component, CraftingContext context) {
        if(this.phase == CraftingManager.PHASE.CRAFTING_TICKABLE)
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(CommandMachineComponent component, CraftingContext context) {
        if(this.phase == CraftingManager.PHASE.ENDING)
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public void setChance(double chance) {
        this.chance = MathHelper.clamp(chance, 0.0, 1.0);
    }

    @Override
    public boolean testChance(CommandMachineComponent component, Random rand, CraftingContext context) {
        double chance = context.getModifiedvalue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    @Override
    public MachineComponentType<CommandMachineComponent> getComponentType() {
        return Registration.COMMAND_MACHINE_COMPONENT.get();
    }

    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        return new RequirementDisplayInfo()
                .setVisible(this.jeiVisible)
                .addTooltip(new TranslationTextComponent("custommachinery.requirements.command.info", new StringTextComponent(this.command).mergeStyle(TextFormatting.AQUA), this.phase.toString().toLowerCase(Locale.ENGLISH)));
    }
}
