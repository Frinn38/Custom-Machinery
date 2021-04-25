package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Lazy;

import java.util.UUID;

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

    private final Lazy<CommandSource> sourceLog;
    private final Lazy<CommandSource> sourceNoLog;

    public CommandMachineComponent(MachineComponentManager manager) {
        super(manager, Mode.NONE);
        sourceLog = Lazy.of(() -> new CommandSource(COMMAND_SOURCE_LOG, Utils.vec3dFromBlockPos(manager.getTile().getPos()), Vector2f.ZERO, (ServerWorld)manager.getTile().getWorld(), 2, "Custom Machinery", new StringTextComponent("Custom Machinery"), manager.getTile().getWorld().getServer(), null));
        sourceNoLog = Lazy.of(() -> new CommandSource(COMMAND_SOURCE_NO_LOG, Utils.vec3dFromBlockPos(manager.getTile().getPos()), Vector2f.ZERO, (ServerWorld)manager.getTile().getWorld(), 2, "Custom Machinery", new StringTextComponent("Custom Machinery"), manager.getTile().getWorld().getServer(), null));
    }

    public void sendCommand(String command, int permissionLevel, boolean log) {
        if(log)
            getManager().getTile().getWorld().getServer().getCommandManager().handleCommand(sourceLog.get().withPermissionLevel(permissionLevel), command);
        else
            getManager().getTile().getWorld().getServer().getCommandManager().handleCommand(sourceNoLog.get().withPermissionLevel(permissionLevel), command);
    }

    @Override
    public MachineComponentType<CommandMachineComponent> getType() {
        return Registration.COMMAND_MACHINE_COMPONENT.get();
    }

    @Override
    public void serialize(CompoundNBT nbt) {

    }

    @Override
    public void deserialize(CompoundNBT nbt) {

    }
}
