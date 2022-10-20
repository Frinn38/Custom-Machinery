package fr.frinn.custommachinery.apiimpl.integration.jei;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ingredients {

    private final Map<IIngredientType<?>, List<List<?>>> inputs = new HashMap<>();
    private final Map<IIngredientType<?>, List<List<?>>> outputs = new HashMap<>();

    public <T> void addInput(IIngredientType<T> type, T input) {
        addInputs(type, Collections.singletonList(input));
    }

    public <T> void addInputs(IIngredientType<T> type, List<T> inputs) {
        this.inputs.computeIfAbsent(type, key -> new ArrayList<>());
        this.inputs.get(type).add(inputs);
    }

    public <T> void addOutput(IIngredientType<T> type, T output) {
        addOutputs(type, Collections.singletonList(output));
    }

    public <T> void addOutputs(IIngredientType<T> type, List<T> outputs) {
        this.outputs.computeIfAbsent(type, key -> new ArrayList<>());
        this.outputs.get(type).add(outputs);
    }

    /**
     * DO NOT CALL THIS METHOD
     * It will be used by the jei integration to give all collected ingredients to jei after all wrappers set their ingredients.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void finish(IIngredients ingredients) {
        this.inputs.forEach((type, list) -> ingredients.setInputLists(type, (List)list));
        this.outputs.forEach((type, list) -> ingredients.setOutputLists(type, (List)list));
    }
}
