package fr.frinn.custommachinery.common.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.data.component.handler.IComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.util.Direction;

import java.util.stream.Stream;

public class RedstoneMachineComponent extends AbstractMachineComponent {

    private int powerToPause;
    private int craftingPowerOutput;
    private int idlePowerOutput;
    private int erroredPowerOutput;
    private MachineComponentType<?> comparatorInputType;
    private String comparatorInputID;

    public RedstoneMachineComponent(MachineComponentManager manager, int powerToPause, int craftingPowerOutput, int idlePowerOutput, int erroredPowerOutput, MachineComponentType<?> comparatorInputType, String comparatorInputID) {
        super(manager, Mode.BOTH);
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

    public int getPowerToPause() {
        return this.powerToPause;
    }

    public int getPowerOutput() {
        switch (this.getManager().getTile().craftingManager.getStatus()) {
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
        return this.getManager().getTile().componentManager.getOptionalComponent(this.comparatorInputType).map(component -> {
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
                        Codecs.MACHINE_COMPONENT_TYPE_CODEC.optionalFieldOf("comparatorinputtype", Registration.ENERGY_MACHINE_COMPONENT.get()).forGetter(template -> template.comparatorInputType),
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
        public RedstoneMachineComponent build(MachineComponentManager manager) {
            return new RedstoneMachineComponent(manager, this.powerToPause, this.craftingPowerOutput, this.idlePowerOutput, this.erroredPowerOutput, this.comparatorInputType, this.comparatorInputID);
        }
    }
}
