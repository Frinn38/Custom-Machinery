package fr.frinn.custommachinery.common.integration.kubejs;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface CustomMachineryRecipeComponents {

    RecipeComponent<Identifier> RESOURCE_LOCATION = new RecipeComponent<>() {
        @Override
        public ResourceKey<RecipeComponentType<?>> type() {
            return RecipeComponentType.key(CustomMachinery.rl("id"));
        }

        @Override
        public Codec<Identifier> codec() {
            return Identifier.CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(Identifier.class);
        }
    };

    RecipeComponent<RecipeRequirement<?, ?>> REQUIREMENT_COMPONENT = new RecipeComponent<>() {
        @Override
        public ResourceKey<RecipeComponentType<?>> type() {
            return RecipeComponentType.key(CustomMachinery.rl("requirements"));
        }

        @Override
        public Codec<RecipeRequirement<?, ?>> codec() {
            return RecipeRequirement.CODEC.codec();
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(RecipeRequirement.class);
        }
    };

    RecipeComponent<MachineAppearance> CUSTOM_APPEARANCE = new RecipeComponent<>() {
        @Override
        public ResourceKey<RecipeComponentType<?>> type() {
            return RecipeComponentType.key(CustomMachinery.rl("appearance"));
        }

        @Override
        public Codec<MachineAppearance> codec() {
            return MachineAppearance.CODEC.xmap(MachineAppearance::new, MachineAppearance::properties, "Machine appearance").codec();
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(MachineAppearance.class);
        }
    };

    RecipeComponent<IGuiElement> CUSTOM_GUI_ELEMENTS = new RecipeComponent<>() {
        @Override
        public ResourceKey<RecipeComponentType<?>> type() {
            return RecipeComponentType.key(CustomMachinery.rl("gui_element"));
        }

        @Override
        public Codec<IGuiElement> codec() {
            return IGuiElement.CODEC.codec();
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(IGuiElement.class);
        }
    };
}
