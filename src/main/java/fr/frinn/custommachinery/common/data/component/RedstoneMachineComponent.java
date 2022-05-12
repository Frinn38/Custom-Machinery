package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.ComponentIOMode;
import fr.frinn.custommachinery.api.component.IComparatorInputComponent;
import fr.frinn.custommachinery.api.component.IMachineComponentManager;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.component.ITickableComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.component.handler.IComponentHandler;
import fr.frinn.custommachinery.apiimpl.component.AbstractMachineComponent;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.core.Direction;

import java.util.stream.Stream;

public class RedstoneMachineComponent extends AbstractMachineComponent implements ITickableComponent {

    private final int powerToPause;
    private final int craftingPowerOutput;
    private final int idlePowerOutput;
    private final int erroredPowerOutput;
    private final MachineComponentType<?> comparatorInputType;
    private final String comparatorInputID;
    private int checkRedstoneCooldown = Utils.RAND.nextInt(20);

    public RedstoneMachineComponent(IMachineComponentManager manager, int powerToPause, int craftingPowerOutput, int idlePowerOutput, int erroredPowerOutput, MachineComponentType<?> comparatorInputType, String comparatorInputID) {
        super(manager, ComponentIOMode.BOTH);
        this.powerToPause = powerToPause;
        this.craftingPowerOutput = craftingPowerOutput;
        this.idlePowerOutput = idlePowerOutput;
        this.erroredPowerOutput = erroredPowerOutput;
        this.comparatorInputType = comparatorInputType;
        this.comparatorInputID = comparatorInputID;
    }

    public RedstoneMachineComponent(IMachineComponentManager manager) {
        this(manager, 999, 0, 0, 0, Registration.ENERGY_MACHINE_COMPONENT.get(), "");
    }

    @Override
    public MachineComponentType<RedstoneMachineComponent> getType() {
        return Registration.REDSTONE_MACHINE_COMPONENT.get();
    }

    @Override
    public void serverTick() {
        if(this.checkRedstoneCooldown-- > 0)
            return;
        this.checkRedstoneCooldown = 20;
        if(!getManager().getTile().isPaused() && this.shouldPauseMachine())
            getManager().getTile().setPaused(true);
        if(getManager().getTile().isPaused() && !this.shouldPauseMachine())
            getManager().getTile().setPaused(false);
    }

    private boolean shouldPauseMachine() {
        return Stream.of(Direction.values()).mapToInt(direction -> getManager().getWorld().getSignal(getManager().getTile().getBlockPos(), direction)).max().orElse(0) >=
                getManager().getComponent(Registration.REDSTONE_MACHINE_COMPONENT.get()).map(RedstoneMachineComponent::getPowerToPause).orElse(1);
    }

    public int getPowerToPause() {
        return this.powerToPause;
    }

    public int getPowerOutput() {
        switch (((CustomMachineTile)this.getManager().getTile()).craftingManager.getStatus()) {
            case PAUSED:
            case IDLE:
                return this.idlePowerOutput;
            case ERRORED:
                return this.erroredPowerOutput;
            case RUNNING:
                return this.craftingPowerOutput;
        }
        return 0;
    }

    public int getComparatorInput() {
        return ((CustomMachineTile)this.getManager().getTile()).componentManager.getComponent(this.comparatorInputType).map(component -> {
            if(component instanceof IComparatorInputComponent)
                return (IComparatorInputComponent)component;
            else if(component instanceof IComponentHandler)
                return (IComparatorInputComponent) ((IComponentHandler<?>) component).getComponentForID(this.comparatorInputID).orElse(null);
            else return null;
        }).map(IComparatorInputComponent::getComparatorInput).orElse(0);
    }

    public int getMachinePower() {
        return Stream.of(Direction.values()).mapToInt(direction -> this.getManager().getWorld().getSignal(this.getManager().getTile().getBlockPos(), direction)).max().orElse(0);
    }

    public static class Template implements IMachineComponentTemplate<RedstoneMachineComponent> {

        public static final Codec<Template> CODEC = RecordCodecBuilder.create(templateInstance ->
                templateInstance.group(
                        CodecLogger.loggedOptional(Codec.INT,"powertopause", 1).forGetter(template -> template.powerToPause),
                        CodecLogger.loggedOptional(Codec.INT,"craftingpoweroutput", 0).forGetter(template -> template.craftingPowerOutput),
                        CodecLogger.loggedOptional(Codec.INT,"idlepoweroutput", 0).forGetter(template -> template.idlePowerOutput),
                        CodecLogger.loggedOptional(Codec.INT,"erroredpoweroutput", 0).forGetter(template -> template.erroredPowerOutput),
                        CodecLogger.loggedOptional(Registration.MACHINE_COMPONENT_TYPE_REGISTRY.get().getCodec(), "comparatorinputtype", Registration.ENERGY_MACHINE_COMPONENT.get()).forGetter(template -> template.comparatorInputType),
                        CodecLogger.loggedOptional(Codec.STRING,"comparatorinputid", "").forGetter(template -> template.comparatorInputID)
                ).apply(templateInstance, Template::new)
        );

        private final int powerToPause;
        private final int craftingPowerOutput;
        private final int idlePowerOutput;
        private final int erroredPowerOutput;
        private final MachineComponentType<?> comparatorInputType;
        private final String comparatorInputID;

        public Template(int powerToPause, int craftingPowerOutput, int idlePowerOutput, int erroredPowerOutput, MachineComponentType<?> comparatorInputType, String comparatorInputID) {
            this.powerToPause = powerToPause;
            this.craftingPowerOutput = craftingPowerOutput;
            this.idlePowerOutput = idlePowerOutput;
            this.erroredPowerOutput = erroredPowerOutput;
            this.comparatorInputType = comparatorInputType;
            this.comparatorInputID = comparatorInputID;
        }

        @Override
        public MachineComponentType<RedstoneMachineComponent> getType() {
            return Registration.REDSTONE_MACHINE_COMPONENT.get();
        }

        @Override
        public String getId() {
            return "";
        }

        @Override
        public boolean canAccept(Object ingredient, boolean isInput, IMachineComponentManager manager) {
            return false;
        }

        @Override
        public RedstoneMachineComponent build(IMachineComponentManager manager) {
            return new RedstoneMachineComponent(manager, this.powerToPause, this.craftingPowerOutput, this.idlePowerOutput, this.erroredPowerOutput, this.comparatorInputType, this.comparatorInputID);
        }
    }
}
