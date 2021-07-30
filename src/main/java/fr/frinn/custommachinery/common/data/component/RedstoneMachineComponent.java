package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.*;
import fr.frinn.custommachinery.api.components.handler.IComponentHandler;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.util.Direction;

import java.util.Random;
import java.util.stream.Stream;

public class RedstoneMachineComponent extends AbstractMachineComponent implements ITickableComponent {

    private int powerToPause;
    private int craftingPowerOutput;
    private int idlePowerOutput;
    private int erroredPowerOutput;
    private MachineComponentType<?> comparatorInputType;
    private String comparatorInputID;
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

    @Override
    public MachineComponentType<RedstoneMachineComponent> getType() {
        return Registration.REDSTONE_MACHINE_COMPONENT.get();
    }

    @Override
    public void tick() {
        if(this.checkRedstoneCooldown-- > 0)
            return;
        this.checkRedstoneCooldown = 20;
        if(!getManager().getTile().isPaused() && this.shouldPauseMachine())
            getManager().getTile().setPaused(true);
        if(getManager().getTile().isPaused() && !this.shouldPauseMachine())
            getManager().getTile().setPaused(false);
    }

    private boolean shouldPauseMachine() {
        if(getManager().getTile().getWorld() == null)
            return false;
        return Stream.of(Direction.values()).mapToInt(direction -> getManager().getTile().getWorld().getRedstonePower(getManager().getTile().getPos(), direction)).max().orElse(0) >=
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
        return Stream.of(Direction.values()).mapToInt(direction -> this.getManager().getTile().getWorld().getRedstonePower(this.getManager().getTile().getPos(), direction)).max().orElse(0);
    }

    public static class Template implements IMachineComponentTemplate<RedstoneMachineComponent> {

        public static final Codec<Template> CODEC = RecordCodecBuilder.create(templateInstance ->
                templateInstance.group(
                        Codec.INT.optionalFieldOf("powertopause", 1).forGetter(template -> template.powerToPause),
                        Codec.INT.optionalFieldOf("craftingpoweroutput", 0).forGetter(template -> template.craftingPowerOutput),
                        Codec.INT.optionalFieldOf("idlepoweroutput", 0).forGetter(template -> template.idlePowerOutput),
                        Codec.INT.optionalFieldOf("erroredpoweroutput", 0).forGetter(template -> template.erroredPowerOutput),
                        MachineComponentType.CODEC.optionalFieldOf("comparatorinputtype", Registration.ENERGY_MACHINE_COMPONENT.get()).forGetter(template -> template.comparatorInputType),
                        Codec.STRING.optionalFieldOf("comparatorinputid", "").forGetter(template -> template.comparatorInputID)
                ).apply(templateInstance, Template::new)
        );

        private int powerToPause;
        private int craftingPowerOutput;
        private int idlePowerOutput;
        private int erroredPowerOutput;
        private MachineComponentType<?> comparatorInputType;
        private String comparatorInputID;

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
        public RedstoneMachineComponent build(IMachineComponentManager manager) {
            return new RedstoneMachineComponent(manager, this.powerToPause, this.craftingPowerOutput, this.idlePowerOutput, this.erroredPowerOutput, this.comparatorInputType, this.comparatorInputID);
        }
    }
}
