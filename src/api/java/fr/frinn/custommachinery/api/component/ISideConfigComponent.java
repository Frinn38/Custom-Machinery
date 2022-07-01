package fr.frinn.custommachinery.api.component;

import fr.frinn.custommachinery.apiimpl.component.config.SideConfig;

public interface ISideConfigComponent extends IMachineComponent {

    SideConfig getConfig();

    String getId();
}
