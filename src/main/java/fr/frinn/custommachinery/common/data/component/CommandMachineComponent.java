package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class CommandMachineComponent extends AbstractMachineComponent {

    private static final ICommandSource COMMAND_SOURCE_LOG = new ICommandSource() {
        @Override
        public void sendMessage(ITextComponent component, UUID senderUUID) {
            CustomMachinery.LOGGER.info(component.getString());
        }

        @Override
        public boolean shouldReceiveFeedback() {return false;}

        @Override
        public boolean shouldReceiveErrors() {return true;}

        @Override
        public boolean allowLogging() {return true;}
    };

    private static final ICommandSource COMMAND_SOURCE_NO_LOG = new ICommandSource() {
        @Override
        public void sendMessage(ITextComponent component, UUID senderUUID) {
            CustomMachinery.LOGGER.info(component.getString());
        }

        @Override
        public boolean shouldReceiveFeedback() {return false;}

        @Override
        public boolean shouldReceiveErrors() {return true;}

        @Override
        public boolean allowLogging() {return false;}
    };

    public CommandMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    public void sendCommand(String command, int permissionLevel, boolean log) {
        if(getManager().getWorld().getServer() == null)
            return;

        CommandSource source = new CommandSource(log ? COMMAND_SOURCE_LOG : COMMAND_SOURCE_NO_LOG, Utils.vec3dFromBlockPos(getManager().getTile().getPos()), Vector2f.ZERO, (ServerWorld)getManager().getWorld(), permissionLevel, "Custom Machinery", getManager().getTile().getMachine().getName(), getManager().getServer(), null);
        getManager().getWorld().getServer().getCommandManager().handleCommand(source, command);
    }

    @Override
    public MachineComponentType<CommandMachineComponent> getType() {
        return Registration.COMMAND_MACHINE_COMPONENT.get();
    }
}
