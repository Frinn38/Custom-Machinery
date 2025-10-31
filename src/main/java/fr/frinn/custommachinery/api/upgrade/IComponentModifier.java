package fr.frinn.custommachinery.api.upgrade;

import fr.frinn.custommachinery.api.component.MachineComponentType;
import net.minecraft.network.chat.Component;

public interface IComponentModifier {

    double apply(double original, int upgradeAmount);

    MachineComponentType<?> component();

    String id();

    String target();

    Component tooltip();

}
