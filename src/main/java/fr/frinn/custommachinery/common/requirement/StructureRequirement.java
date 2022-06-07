package fr.frinn.custommachinery.common.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.common.component.StructureMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.BlockStructure;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StructureRequirement extends AbstractRequirement<StructureMachineComponent> implements ITickableRequirement<StructureMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<StructureRequirement> CODEC = RecordCodecBuilder.create(structureRequirementInstance ->
            structureRequirementInstance.group(
                    Codecs.list(Codecs.list(Codec.STRING)).fieldOf("pattern").forGetter(requirement -> requirement.pattern),
                    Codec.unboundedMap(Codecs.CHARACTER_CODEC, IIngredient.BLOCK).fieldOf("keys").forGetter(requirement -> requirement.keys)
            ).apply(structureRequirementInstance, StructureRequirement::new)
    );

    private final List<List<String>> pattern;
    private final Map<Character, IIngredient<PartialBlockState>> keys;
    private final BlockStructure structure;

    public StructureRequirement(List<List<String>> pattern, Map<Character, IIngredient<PartialBlockState>> keys) {
        super(RequirementIOMode.INPUT);
        this.pattern = pattern;
        this.keys = keys;
        BlockStructure.Builder builder = BlockStructure.Builder.start();
        //TODO: iterate list in inverse order in 1.18 to make the pattern from top to bottom instead of from bottom to top (current)
        for(List<String> levels : pattern)
            builder.aisle(levels.toArray(new String[0]));
        for(Map.Entry<Character, IIngredient<PartialBlockState>> key : keys.entrySet())
            builder.where(key.getKey(), key.getValue());
        this.structure = builder.build();
    }

    @Override
    public RequirementType<StructureRequirement> getType() {
        return Registration.STRUCTURE_REQUIREMENT.get();
    }

    @Override
    public boolean test(StructureMachineComponent component, ICraftingContext context) {
        return component.checkStructure(this.structure);
    }

    @Override
    public CraftingResult processStart(StructureMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(StructureMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<StructureMachineComponent> getComponentType() {
        return Registration.STRUCTURE_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(StructureMachineComponent component, ICraftingContext context) {
        if(component.checkStructure(this.structure))
            return CraftingResult.success();
        else return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.structure.error"));
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.addTooltip(new TranslatableComponent("custommachinery.requirements.structure.info"));
        info.addTooltip(new TranslatableComponent("custommachinery.requirements.structure.click"));
        this.pattern.stream().flatMap(List::stream).flatMap(s -> s.chars().mapToObj(c -> (char)c)).collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).forEach((key, amount) -> {
            IIngredient<PartialBlockState> ingredient = this.keys.get(key);
            if(ingredient != null && amount > 0)
                info.addTooltip(new TranslatableComponent("custommachinery.requirements.structure.list", amount, new TextComponent(ingredient.toString()).withStyle(ChatFormatting.GOLD)));
        });
        info.setClickAction((machine, mouseButton) -> CustomMachineRenderer.addRenderBlock(machine.getId(), this.structure::getBlocks));
        info.setItemIcon(Items.STRUCTURE_BLOCK);
    }
}