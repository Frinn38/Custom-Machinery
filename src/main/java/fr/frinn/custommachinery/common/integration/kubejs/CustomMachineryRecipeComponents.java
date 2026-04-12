package fr.frinn.custommachinery.common.integration.kubejs;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.rhino.type.TypeInfo;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.resources.ResourceLocation;

public interface CustomMachineryRecipeComponents {

    RecipeComponentType<ResourceLocation> RESOURCE_LOCATION = RecipeComponentType.unit(CustomMachinery.rl("rl"), new RecipeComponent<>() {
        @Override
        public RecipeComponentType<ResourceLocation> type() {
            return RESOURCE_LOCATION;
        }

        @Override
        public Codec<ResourceLocation> codec() {
            return ResourceLocation.CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(ResourceLocation.class);
        }
    });

    RecipeComponentType<RecipeRequirement<?, ?>> REQUIREMENT_COMPONENT = RecipeComponentType.unit(CustomMachinery.rl("requirements"), new RecipeComponent<>() {
        @Override
        public RecipeComponentType<RecipeRequirement<?, ?>> type() {
            return REQUIREMENT_COMPONENT;
        }

        @Override
        public Codec<RecipeRequirement<?, ?>> codec() {
            return RecipeRequirement.CODEC.codec();
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(RecipeRequirement.class);
        }
    });

    RecipeComponentType<MachineAppearance> CUSTOM_APPEARANCE = RecipeComponentType.unit(CustomMachinery.rl("appearance"), new RecipeComponent<>() {
        @Override
        public RecipeComponentType<MachineAppearance> type() {
            return CUSTOM_APPEARANCE;
        }

        @Override
        public Codec<MachineAppearance> codec() {
            return MachineAppearance.CODEC.xmap(MachineAppearance::new, MachineAppearance::properties, "Machine appearance").codec();
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(MachineAppearance.class);
        }
    });

    RecipeComponentType<IGuiElement> CUSTOM_GUI_ELEMENTS = RecipeComponentType.unit(CustomMachinery.rl("gui_element"), new RecipeComponent<>() {
        @Override
        public RecipeComponentType<IGuiElement> type() {
            return CUSTOM_GUI_ELEMENTS;
        }

        @Override
        public Codec<IGuiElement> codec() {
            return IGuiElement.CODEC.codec();
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(IGuiElement.class);
        }
    });
}
