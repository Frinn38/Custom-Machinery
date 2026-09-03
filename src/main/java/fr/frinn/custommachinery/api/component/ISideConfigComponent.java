package fr.frinn.custommachinery.api.component;

import fr.frinn.custommachinery.impl.component.config.SideConfig;

/**
 * Should be implemented by any component that use a {@link SideConfig} to let users customize the I/O config of this component.
 * Components can use {@link fr.frinn.custommachinery.impl.component.config.IOSideConfig} for an input/output/both/none type of config or
 * {@link fr.frinn.custommachinery.impl.component.config.ToggleSideConfig} for an enabled/disabled type of config.
 */
public interface ISideConfigComponent extends IMachineComponent {

    /**
     * @return The {@link SideConfig} used by this component.
     */
    SideConfig<?> getConfig();

    /**
     * @return The id of the component to configure.
     */
    String getId();
}
