package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class CommandMachineComponent extends AbstractMachineComponent {

    private static final CommandSource COMMAND_SOURCE_LOG = new CommandSource() {
        @Override
        public void sendMessage(Component component, UUID senderUUID) {
            CustomMachinery.LOGGER.info(component.getString());
        }

        @Override
        public boolean acceptsSuccess() {return false;}

        @Override
        public boolean acceptsFailure() {return true;}

        @Override
        public boolean shouldInformAdmins() {return true;}
    };

    private static final CommandSource COMMAND_SOURCE_NO_LOG = new CommandSource() {
        @Override
        public void sendMessage(Component component, UUID senderUUID) {
            CustomMachinery.LOGGER.info(component.getString());
        }

        @Override
        public boolean acceptsSuccess() {return false;}

        @Override
        public boolean acceptsFailure() {return true;}

        @Override
        public boolean shouldInformAdmins() {return false;}
    };

    public CommandMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    public void sendCommand(String command, int permissionLevel, boolean log) {
        if(getManager().getLevel().getServer() == null)
            return;

        CommandSourceStack source = new CommandSourceStack(log ? COMMAND_SOURCE_LOG : COMMAND_SOURCE_NO_LOG, Utils.vec3dFromBlockPos(getManager().getTile().getBlockPos()), Vec2.ZERO, (ServerLevel)getManager().getLevel(), permissionLevel, "Custom Machinery", getManager().getTile().getMachine().getName(), getManager().getServer(), null);
        getManager().getLevel().getServer().getCommands().performCommand(source, command);
    }

    @Override
    public MachineComponentType<CommandMachineComponent> getType() {
        return Registration.COMMAND_MACHINE_COMPONENT.get();
    }
}
