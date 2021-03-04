package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;

import java.util.Locale;

public interface IRequirement<T extends IMachineComponent> {

    Codec<IRequirement<?>> CODEC = RequirementType.CODEC.dispatch("type", IRequirement::getType, RequirementType::getCodec);

    RequirementType<IRequirement<T>> getType();

    boolean test(T component);

    CraftingResult processStart(T component);

    CraftingResult processEnd(T component);

    MODE getMode();

    MachineComponentType<T> getComponentType();

    enum MODE {
        INPUT,
        OUTPUT;

        static MODE value(String mode) {
            return valueOf(mode.toUpperCase(Locale.ENGLISH));
        }
    }
}
