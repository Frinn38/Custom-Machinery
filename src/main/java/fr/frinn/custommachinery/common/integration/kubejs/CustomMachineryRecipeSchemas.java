package fr.frinn.custommachinery.common.integration.kubejs;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.TickDuration;
import dev.latvian.mods.rhino.type.TypeInfo;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public interface CustomMachineryRecipeSchemas {

    RecipeComponent<ResourceLocation> RESOURCE_LOCATION = new RecipeComponent<>() {
        @Override
        public Codec<ResourceLocation> codec() {
            return ResourceLocation.CODEC;
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(ResourceLocation.class);
        }
    };

    RecipeComponent<RecipeRequirement<?, ?>> REQUIREMENT_COMPONENT = new RecipeComponent<>() {
        @Override
        public Codec<RecipeRequirement<?, ?>> codec() {
            return RecipeRequirement.CODEC.codec();
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(RecipeRequirement.class);
        }
    };

    RecipeComponent<List<RecipeRequirement<?, ?>>> REQUIREMENT_LIST = REQUIREMENT_COMPONENT.asList();

    RecipeComponent<MachineAppearance> CUSTOM_APPEARANCE = new RecipeComponent<>() {
        @Override
        public Codec<MachineAppearance> codec() {
            return MachineAppearance.CODEC.xmap(MachineAppearance::new, MachineAppearance::getProperties, "Machine appearance").codec();
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(MachineAppearance.class);
        }
    };

    RecipeComponent<IGuiElement> CUSTOM_GUI_ELEMENTS = new RecipeComponent<>() {
        @Override
        public Codec<IGuiElement> codec() {
            return IGuiElement.CODEC.codec();
        }

        @Override
        public TypeInfo typeInfo() {
            return TypeInfo.of(IGuiElement.class);
        }
    };

    RecipeKey<ResourceLocation> MACHINE_ID = RESOURCE_LOCATION.key("machine", ComponentRole.OTHER);
    RecipeKey<TickDuration> TIME = TimeComponent.TICKS.key("time", ComponentRole.OTHER);
    RecipeKey<ItemStack> OUTPUT = ItemStackComponent.ITEM_STACK.key("output", ComponentRole.OUTPUT);

    RecipeKey<List<RecipeRequirement<?, ?>>> REQUIREMENTS = REQUIREMENT_LIST.key("requirements", ComponentRole.OTHER).optional(Collections.emptyList()).alwaysWrite().exclude();
    RecipeKey<List<RecipeRequirement<?, ?>>> JEI_REQUIREMENTS = REQUIREMENT_LIST.key("jei", ComponentRole.OTHER).optional(Collections.emptyList()).alwaysWrite().exclude();

    RecipeKey<Integer> PRIORITY = NumberComponent.INT.key("priority", ComponentRole.OTHER).optional(0).alwaysWrite().exclude();
    RecipeKey<Integer> JEI_PRIORITY = NumberComponent.INT.key("jeiPriority", ComponentRole.OTHER).optional(0).alwaysWrite().exclude();

    RecipeKey<Boolean> ERROR = BooleanComponent.BOOLEAN.key("error", ComponentRole.OTHER).optional(false).alwaysWrite().exclude();
    RecipeKey<Boolean> HIDDEN = BooleanComponent.BOOLEAN.key("hidden", ComponentRole.OTHER).optional(false).alwaysWrite().exclude();
    RecipeKey<MachineAppearance> APPEARANCE = CUSTOM_APPEARANCE.key("appearance", ComponentRole.OTHER).optional((MachineAppearance) null).alwaysWrite().exclude();
    RecipeKey<List<IGuiElement>> GUI = CUSTOM_GUI_ELEMENTS.asList().key("gui", ComponentRole.OTHER).optional(Collections.emptyList()).alwaysWrite().exclude();
    RecipeKey<List<Integer>> ALLOWED_CORES = NumberComponent.intRange(1, Integer.MAX_VALUE).asList().key("cores", ComponentRole.OTHER).optional(Collections.emptyList()).alwaysWrite().exclude();
    RecipeKey<Boolean> SINGLE_CORE = BooleanComponent.BOOLEAN.key("single_core", ComponentRole.OTHER).optional(false).alwaysWrite().exclude();

    RecipeSchema CUSTOM_MACHINE = new RecipeSchema(MACHINE_ID, TIME, REQUIREMENTS, JEI_REQUIREMENTS, PRIORITY, JEI_PRIORITY, ERROR, HIDDEN, APPEARANCE, GUI, ALLOWED_CORES, SINGLE_CORE).factory(new KubeRecipeFactory(CustomMachinery.rl("custom_machine"), TypeInfo.of(CustomMachineRecipeBuilderJS.class), CustomMachineRecipeBuilderJS::new));
    RecipeSchema CUSTOM_CRAFT = new RecipeSchema(MACHINE_ID, OUTPUT, REQUIREMENTS, JEI_REQUIREMENTS, PRIORITY, JEI_PRIORITY, HIDDEN).factory(new KubeRecipeFactory(CustomMachinery.rl("custom_craft"), TypeInfo.of(CustomCraftRecipeJSBuilder.class), CustomCraftRecipeJSBuilder::new));
}
