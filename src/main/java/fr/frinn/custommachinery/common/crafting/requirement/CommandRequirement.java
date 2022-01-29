package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.IDelayedRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractChanceableRequirement;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractDelayedChanceableRequirement;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractDelayedRequirement;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.data.component.CommandMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Locale;
import java.util.Random;

public class CommandRequirement extends AbstractDelayedChanceableRequirement<CommandMachineComponent> implements ITickableRequirement<CommandMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<CommandRequirement> CODEC = RecordCodecBuilder.create(commandRequirementInstance ->
            commandRequirementInstance.group(
                    Codec.STRING.fieldOf("command").forGetter(requirement -> requirement.command),
                    Codecs.PHASE_CODEC.fieldOf("phase").forGetter(requirement -> requirement.phase),
                    CodecLogger.loggedOptional(Codec.INT,"permissionlevel", 2).forGetter(requirement -> requirement.permissionLevel),
                    CodecLogger.loggedOptional(Codec.BOOL,"log", false).forGetter(requirement -> requirement.log),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0),"chance", 1.0D).forGetter(requirement -> requirement.chance),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0), "delay", 0.0).forGetter(AbstractDelayedRequirement::getDelay),
                    CodecLogger.loggedOptional(Codec.BOOL,"jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(commandRequirementInstance, (command, phase, permissionLevel, log, chance, delay, jeiVisible) -> {
                CommandRequirement requirement = new CommandRequirement(command, phase, permissionLevel, log);
                requirement.setChance(chance);
                requirement.setDelay(delay);
                requirement.setJeiVisible(jeiVisible);
                return requirement;
            })
    );

    private final String command;
    private final CraftingManager.PHASE phase;
    private final int permissionLevel;
    private final boolean log;
    private double chance = 1.0D;
    private boolean jeiVisible = true;

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
        if(this.phase == CraftingManager.PHASE.STARTING)
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(CommandMachineComponent component, ICraftingContext context) {
        if(this.phase == CraftingManager.PHASE.CRAFTING_TICKABLE)
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(CommandMachineComponent component, ICraftingContext context) {
        if(this.phase == CraftingManager.PHASE.ENDING)
            component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult execute(CommandMachineComponent component, ICraftingContext context) {
        component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public void setChance(double chance) {
        this.chance = MathHelper.clamp(chance, 0.0, 1.0);
    }

    @Override
    public boolean shouldSkip(CommandMachineComponent component, Random rand, ICraftingContext context) {
        double chance = context.getModifiedValue(this.chance, this, "chance");
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
    public void getDisplayInfo(IDisplayInfo info) {
        info.setVisible(this.jeiVisible)
                .addTooltip(new TranslationTextComponent("custommachinery.requirements.command.info", new StringTextComponent(this.command).mergeStyle(TextFormatting.AQUA), this.phase.toString().toLowerCase(Locale.ENGLISH)))
                //.setSpriteIcon(Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(new ResourceLocation("block/command_block_front")));
                .setItemIcon(Items.COMMAND_BLOCK);
    }
}
