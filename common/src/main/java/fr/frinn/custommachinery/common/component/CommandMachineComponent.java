package fr.frinn.custommachinery.common.component;

import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.component.AbstractMachineComponent;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandMachineComponent extends AbstractMachineComponent {

    private static final CommandSource COMMAND_SOURCE_LOG = new CommandSource() {

        @Override
        public void sendSystemMessage(Component component) {
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
        public void sendSystemMessage(Component component) {
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

        CommandSourceStack source = new CommandSourceStack(log ? COMMAND_SOURCE_LOG : COMMAND_SOURCE_NO_LOG, Vec3.atCenterOf(getManager().getTile().getBlockPos()), getMachineRotation(), (ServerLevel)getManager().getLevel(), permissionLevel, "Custom Machinery", getManager().getTile().getMachine().getName(), getManager().getServer(), null);
        getManager().getLevel().getServer().getCommands().performPrefixedCommand(source, command);
    }

    @Override
    public MachineComponentType<CommandMachineComponent> getType() {
        return Registration.COMMAND_MACHINE_COMPONENT.get();
    }

    private Vec2 getMachineRotation() {
        Direction facing = getManager().getTile().getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return new Vec2(0, facing.toYRot() - 180);
    }
}
