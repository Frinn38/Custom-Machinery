package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.CommandRequirement;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalBoolean;
import org.openzen.zencode.java.ZenCodeType.OptionalInt;

@ZenRegister
@Name(CTConstants.REQUIREMENT_COMMAND)
public interface CommandRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T runCommandOnStart(String command, @OptionalInt(2) int permissionLevel, @OptionalBoolean boolean log) {
        return addRequirement(new CommandRequirement(command, MachineProcessor.PHASE.STARTING, permissionLevel, log));
    }

    @Method
    default T runCommandEachTick(String command, @OptionalInt(2) int permissionLevel, @OptionalBoolean boolean log) {
        return addRequirement(new CommandRequirement(command, MachineProcessor.PHASE.CRAFTING_TICKABLE, permissionLevel, log));
    }

    @Method
    default T runCommandOnEnd(String command, @OptionalInt(2) int permissionLevel, @OptionalBoolean boolean log) {
        return addRequirement(new CommandRequirement(command, MachineProcessor.PHASE.ENDING, permissionLevel, log));
    }
}
