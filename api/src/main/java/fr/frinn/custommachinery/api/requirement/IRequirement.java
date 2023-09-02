package fr.frinn.custommachinery.api.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.IMachineComponent;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.ComponentNotFoundException;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.DisplayInfoTemplate;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * The base interface to declare an IRequirement.
 * Requirements are defined by the user in a recipe json, they represent an input, an output or a condition to process the recipe.
 * Each IRequirement must be registered to the ForgeRegistry using a {@link RequirementType}.
 * Each IRequirement must have a {@link RequirementIOMode} which is either INPUT or OUTPUT, you can make it fixed or left to the user.
 * Each IRequirement must have a Codec registered with its type, the Codec will be used to deserialize the requirement from json, and send it to the client.
 * Each IRequirement must be associated to an {@link IMachineComponent} the associated component will be passed for each action of the requirement.
 * If any execution method return {@link CraftingResult#error(net.minecraft.network.chat.Component)} the crafting process will be paused and the error shown to the player.
 * The crafting process will be resumed only after the errored requirement return success or pass.
 * @param <T> The component this requirement will use.
 */
public interface IRequirement<T extends IMachineComponent> {

    /**
     * A dispatch codec, used by the {@link IMachineRecipe} main codec to parse all requirements from json using the "type" property of the requirement.
     */
    NamedCodec<IRequirement<?>> CODEC = NamedCodec.record(iRequirementInstance ->
            iRequirementInstance.group(
                    RegistrarCodec.REQUIREMENT.<IRequirement<?>>dispatch(IRequirement::getType, RequirementType::getCodec, "Requirement").forGetter(requirement -> requirement),
                    DisplayInfoTemplate.CODEC.optionalFieldOf("info").forGetter(requirement -> Optional.ofNullable(requirement.getDisplayInfoTemplate()))
            ).apply(iRequirementInstance, (requirement, info) -> {
                    info.ifPresent(requirement::setDisplayInfoTemplate);
                    return requirement;
            }), "Requirement"
    );

    /**
     * Used by the requirement dispatch codec to serialize an IRequirement.
     * This MUST return the same instance of the {@link RequirementType} as the one registered in the forge registry.
     * @return The type of this requirement.
     */
    RequirementType<? extends IRequirement<?>> getType();

    /**
     * Used by the crafting process to find which component the requirement use.
     * If the machine doesn't have the component, an {@link ComponentNotFoundException} will be thrown and the recipe will not be processed.
     * This MUST return the same instance of the {@link MachineComponentType} as the one registered in the forge registry.
     * @return The type of component used by this requirement.
     */
    MachineComponentType<T> getComponentType();

    /**
     * The first step of the crafting process, the machine is idle and searching for recipes to process.
     * For each available recipes it will test all requirements using this method.
     * NOTE : You must only do checks in this methods, and not consume or produce things as the recipe may not be processed if another requirement return false.
     * @param component The {@link IMachineComponent} used by this requirement.
     * @param context A few useful info about the crafting process, and some utilities methods.
     * @return True if the requirement can be processed by this machine, false otherwise.
     */
    boolean test(T component, ICraftingContext context);

    /**
     * This method is called on the first tick of the recipe crafting process for each requirement of the recipe.
     * Usually that's where you consume inputs.
     * @param component The {@link IMachineComponent} used by this requirement.
     * @param context A few useful info about the crafting process, and some utilities methods.
     * @return {@link CraftingResult#success()} if the requirement successfully did its things.
     *         {@link CraftingResult#error(net.minecraft.network.chat.Component)} if there was an error during the process (example : missing inputs).
     *         {@link CraftingResult#pass()} if the requirement didn't care about this phase.
     */
    CraftingResult processStart(T component, ICraftingContext context);

    /**
     * This method is called on the last tick of the recipe crafting process for each requirement of the recipe.
     * Usually that's where you produce outputs.
     * @param component The {@link IMachineComponent} used by this requirement.
     * @param context A few useful info about the crafting process, and some utilities methods.
     * @return {@link CraftingResult#success()} if the requirement successfully did its things.
     *         {@link CraftingResult#error(net.minecraft.network.chat.Component)} if there was an error during the process (example : not enough space to store outputs).
     *         {@link CraftingResult#pass()} if the requirement didn't care about this phase.
     */
    CraftingResult processEnd(T component, ICraftingContext context);

    /**
     * Currently only used by machine upgrades to find whether they apply to this requirement.
     * @return The INPUT or OUTPUT mode of this requirement.
     */
    RequirementIOMode getMode();

    /**
     * Used to set a set of custom display infos, to be rendered in jei.
     * This can be called when the requirement is parsed from json, o from the Crafttweaker or KubeJS integrations.
     * @param template A template of requirement display info.
     */
    void setDisplayInfoTemplate(DisplayInfoTemplate template);

    /**
     * Get a template of requirement display info, if provided by the user, or null otherwise.
     * @return A {@link DisplayInfoTemplate} provided by the user through json or Crafttweaker or KubeJS integrations.
     */
    @Nullable
    DisplayInfoTemplate getDisplayInfoTemplate();

    /**
     * Called by the jei integration to collect all display info from the requirement.
     * Use the passed {@link IDisplayInfo} to add some tooltips, a custom icon or a click callback to the requirement in the jei recipe.
     * Note: The {@link DisplayInfoTemplate} returned by {@link IRequirement#getDisplayInfoTemplate()} (if present) will take priority
     * over this method. Use it to define the "default" display info, that might be overridden by the user.
     * @param info The display info of the requirement.
     */
    default void getDisplayInfo(IDisplayInfo info) {

    }
}
