package fr.frinn.custommachinery.common.data.component;

public abstract class AbstractMachineComponent implements IMachineComponent {

    private MachineComponentManager manager;
    private Mode mode;

    public AbstractMachineComponent(MachineComponentManager manager, Mode mode) {
        this.manager = manager;
        this.mode = mode;
    }

    public Mode getMode() {
        return this.mode;
    }

    public MachineComponentManager getManager() {
        return this.manager;
    }
}
