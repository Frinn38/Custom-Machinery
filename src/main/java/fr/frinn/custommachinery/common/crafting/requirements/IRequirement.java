package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.IMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

import java.util.Locale;

public interface IRequirement<T extends IMachineComponent> {

    Codec<IRequirement<?>> CODEC = RequirementType.CODEC.dispatch("type", IRequirement::getType, RequirementType::getCodec);

    RequirementType<IRequirement<T>> getType();

    boolean test(T component);

    CraftingResult processStart(T component);

    CraftingResult processEnd(T component);

    MODE getMode();

    MachineComponentType<T> getComponentType();

    IIngredientType<?> getJEIIngredientType();

    Object asJEIIngredient();

    void addJeiIngredients(IIngredients ingredients);

    enum MODE {
        INPUT,
        OUTPUT;

        public static final Codec<MODE> CODEC = Codec.STRING.xmap(MODE::value, MODE::toString).stable();

        static MODE value(String mode) {
            return valueOf(mode.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ENGLISH);
        }
    }
}
