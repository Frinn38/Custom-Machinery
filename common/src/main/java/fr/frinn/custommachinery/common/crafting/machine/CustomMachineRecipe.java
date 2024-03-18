package fr.frinn.custommachinery.common.crafting.machine;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.machine.IMachineAppearance;
import fr.frinn.custommachinery.api.machine.MachineAppearanceProperty;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.common.crafting.RecipeChecker;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.MachineAppearance;
import fr.frinn.custommachinery.common.util.Comparators;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CustomMachineRecipe implements Recipe<Container>, IMachineRecipe {

    private final ResourceLocation id;
    private final ResourceLocation machine;
    private final int time;
    private final List<IRequirement<?>> requirements;
    private final List<IRequirement<?>> jeiRequirements;
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
    private final Supplier<RecipeChecker<CustomMachineRecipe>> checker = Suppliers.memoize(() -> new RecipeChecker<>(this));

    public CustomMachineRecipe(ResourceLocation id, ResourceLocation machine, int time, List<IRequirement<?>> requirements, List<IRequirement<?>> jeiRequirements, int priority, int jeiPriority, boolean resetOnError, boolean hidden, @Nullable MachineAppearance appearance, List<IGuiElement> guiElements) {
        this.id = id;
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
    public ResourceLocation getId() {
        return this.getRecipeId();
    }

    @Override
    public ResourceLocation getRecipeId() {
        return this.id;
    }

    @Override
    public int getRecipeTime() {
        return this.time;
    }

    @Override
    public List<IRequirement<?>> getRequirements() {
        return this.requirements;
    }

    @Override
    public List<IRequirement<?>> getJeiRequirements() {
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

    public RecipeChecker<CustomMachineRecipe> checker() {
        return checker.get();
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
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }
}
