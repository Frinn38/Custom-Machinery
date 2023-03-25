package fr.frinn.custommachinery.api.component;

import java.util.List;

public interface IDumpComponent extends IMachineComponent {

    void dump(List<String> ids);
}
