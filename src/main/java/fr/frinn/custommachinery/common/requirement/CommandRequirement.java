package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.CommandMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public record CommandRequirement(String command, int permissionLevel, boolean log, boolean tick) implements IRequirement<CommandMachineComponent> {

    public static final NamedCodec<CommandRequirement> CODEC = NamedCodec.record(commandRequirementInstance ->
            commandRequirementInstance.group(
                    NamedCodec.STRING.fieldOf("command").forGetter(requirement -> requirement.command),
                    NamedCodec.INT.optionalFieldOf("permissionlevel", 2).forGetter(requirement -> requirement.permissionLevel),
                    NamedCodec.BOOL.optionalFieldOf("log", false).forGetter(requirement -> requirement.log),
                    NamedCodec.BOOL.optionalFieldOf("tick", false).forGetter(requirement -> requirement.tick)
            ).apply(commandRequirementInstance, CommandRequirement::new), "Command requirement"
    );

    @Override
    public RequirementType<CommandRequirement> getType() {
        return Registration.COMMAND_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<CommandMachineComponent> getComponentType() {
        return Registration.COMMAND_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }

    @Override
    public boolean test(CommandMachineComponent component, ICraftingContext context) {
        return true;
    }

    @Override
    public void gatherRequirements(IRequirementList<CommandMachineComponent> list) {
        if(this.tick)
            list.processEachTick(this::process);
        else
            list.processDelayed(1.0D, this::process);
    }

    private CraftingResult process(CommandMachineComponent component, ICraftingContext context) {
        component.sendCommand(this.command, this.permissionLevel, this.log);
        return CraftingResult.pass();
    }

    @Override
    public void getDefaultDisplayInfo(IDisplayInfo info, RecipeRequirement<?, ?> requirement) {
        info.setItemIcon(Items.COMMAND_BLOCK);
        if(requirement.delay() == 0.0D)
            info.addTooltip(Component.translatable("custommachinery.requirements.command.info", Component.literal(this.command).withStyle(ChatFormatting.AQUA), "start"));
        else if(requirement.delay() == 1.0D)
            info.addTooltip(Component.translatable("custommachinery.requirements.command.info", Component.literal(this.command).withStyle(ChatFormatting.AQUA), "end"));
        else
            info.addTooltip(Component.translatable("custommachinery.requirements.command.delay", Component.literal(this.command).withStyle(ChatFormatting.AQUA), (int)(requirement.delay() * 100) + "%"));
    }
}
