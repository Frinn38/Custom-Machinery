package fr.frinn.custommachinery.client.screen.creation.upgrade;

import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.screen.creation.upgrade.RecipeModifierListWidget.RecipeModifierEntry;
import fr.frinn.custommachinery.client.screen.widget.ListWidget;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.upgrade.MachineUpgradeBuilder;
import fr.frinn.custommachinery.common.upgrade.MachineUpgradeBuilder.RecipeModifierBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

import java.util.List;

public class RecipeModifierListWidget extends ListWidget<RecipeModifierEntry> {

    public RecipeModifierListWidget(int x, int y, int width, int height, int itemHeight, MachineUpgradeBuilder builder) {
        super(x, y, width, height, itemHeight, Component.empty());
        builder.getRecipeModifiers().forEach(recipeModifierBuilder -> this.addEntry(new RecipeModifierEntry(recipeModifierBuilder)));
    }

    public static class RecipeModifierEntry extends Entry {

        private final CycleButton<RequirementType<?>> requirement;
        private final EditBox target;

        private RecipeModifierEntry(RecipeModifierBuilder builder) {
            //Requirement
            this.requirement = CycleButton.<RequirementType<?>>builder(RequirementType::getName)
                    .displayOnlyValue()
                    .withValues(Registration.REQUIREMENT_TYPE_REGISTRY.stream().toList())
                    .withInitialValue(builder.getRequirementType())
                    .create(0, 0, 100, 16, Component.empty(), (button, value) -> builder.setRequirementType(value));
            this.requirement.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.upgrade.recipe.requirement.tooltip")));

            //Target
            this.target = new EditBox(Minecraft.getInstance().font, 100, 16, Component.empty());
            this.target.setMaxLength(Integer.MAX_VALUE);
            this.target.setValue(builder.getTarget());
            this.target.setResponder(builder::setTarget);
            this.target.setTooltip(Tooltip.create(Component.translatable("custommachinery.gui.creation.upgrade.recipe.target.tooltip")));
        }

        @Override
        protected void render(GuiGraphics graphics, int index, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
            graphics.fill(x + 1, y + 1, x + width - 2, y + height - 1, FastColor.ARGB32.color(255, 255, 0, 0));
            graphics.fill(x + 2, y + 2, x + width - 3, y + height - 2, FastColor.ARGB32.color(255, 192, 192, 192));
            this.requirement.setPosition(x + 2, y + 2);
            this.target.setPosition(x + 2, y + 18);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.requirement, this.target);
        }
    }
}
