package fr.frinn.custommachinery.common.crafting.machine;

import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.IMachineAppearance;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.util.Comparators;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomMachineRecipe implements Recipe<RecipeInput>, IMachineRecipe {

    private final ResourceLocation machine;
    private final int time;
    private final List<RecipeRequirement<?, ?>> requirements;
    private final List<RecipeRequirement<?, ?>> jeiRequirements;
    private final int priority;
    private final int jeiPriority;
    private final boolean resetOnError;
    private final boolean hidden;
    @Nullable
    private final MachineAppearance appearance;
    private final List<IGuiElement> guiElements;
    @Nullable
    private MachineAppearance customAppearance;
    @Nullable
    private List<IGuiElement> customGuiElements;

    public CustomMachineRecipe(ResourceLocation machine, int time, List<RecipeRequirement<?, ?>> requirements, List<RecipeRequirement<?, ?>> jeiRequirements, int priority, int jeiPriority, boolean resetOnError, boolean hidden, @Nullable MachineAppearance appearance, List<IGuiElement> guiElements) {
        this.machine = machine;
        this.time = time;
        this.requirements = requirements.stream().sorted(Comparators.REQUIREMENT_COMPARATOR).toList();
        this.jeiRequirements = jeiRequirements;
        this.priority = priority;
        this.jeiPriority = jeiPriority;
        this.resetOnError = resetOnError;
        this.hidden = hidden;
        this.appearance = appearance;
        this.guiElements = guiElements;
    }

    @Override
    public ResourceLocation getMachineId() {
        return this.machine;
    }

    @Override
    public int getRecipeTime() {
        return this.time;
    }

    @Override
    public List<RecipeRequirement<?, ?>> getRequirements() {
        return this.requirements;
    }

    @Override
    public List<RecipeRequirement<?, ?>> getJeiRequirements() {
        return this.jeiRequirements;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public int getJeiPriority() {
        return this.jeiPriority;
    }

    @Override
    public boolean shouldResetOnError() {
        return this.resetOnError;
    }

    @Override
    public boolean showInJei() {
        return !this.hidden;
    }

    @Nullable
    public MachineAppearance getAppearance() {
        return this.appearance;
    }

    @Nullable
    public MachineAppearance getCustomAppearance(IMachineAppearance baseAppearance) {
        if(this.customAppearance != null)
            return this.customAppearance;

        if(this.appearance == null)
            return null;

        ImmutableMap.Builder<MachineAppearanceProperty<?>, Object> properties = ImmutableMap.builder();

        for(MachineAppearanceProperty<?> property : Registration.APPEARANCE_PROPERTY_REGISTRY) {
            Object value = this.appearance.getProperty(property);
            if(value == null || property.getDefaultValue().equals(value))
                properties.put(property, baseAppearance.getProperty(property));
            else
                properties.put(property, value);
        }

        this.customAppearance = new MachineAppearance(properties.build());

        return this.customAppearance;
    }

    public List<IGuiElement> getGuiElements() {
        return this.guiElements;
    }

    public List<IGuiElement> getCustomGuiElements(List<IGuiElement> baseGuiElements) {
        if(this.customGuiElements != null)
            return this.customGuiElements;

        if(this.guiElements.isEmpty())
            return null;

        List<IGuiElement> elements = new ArrayList<>(baseGuiElements);
        this.guiElements.forEach(element -> {
            if(element.getId().isEmpty() || baseGuiElements.stream().noneMatch(e -> e.getId().equals(element.getId())))
                elements.add(element);
            else
                elements.replaceAll(toReplace -> {
                    if(toReplace.getId().equals(element.getId()))
                        return element;
                    return toReplace;
                });
        });
        this.customGuiElements = List.copyOf(elements);
        return elements;
    }

    /** Vanilla Recipe Implementation **/

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.CUSTOM_MACHINE_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Registration.CUSTOM_MACHINE_RECIPE.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public boolean matches(RecipeInput p_346065_, Level p_345375_) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput p_345149_, Provider p_346030_) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(Provider pRegistries) {
        return ItemStack.EMPTY;
    }
}
