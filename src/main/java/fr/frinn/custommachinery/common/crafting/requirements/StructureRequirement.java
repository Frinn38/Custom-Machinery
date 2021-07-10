package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.StructureMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Map;

public class StructureRequirement extends AbstractTickableRequirement<StructureMachineComponent> {

    public static final Codec<StructureRequirement> CODEC = RecordCodecBuilder.create(structureRequirementInstance ->
            structureRequirementInstance.group(
                    Codec.STRING.listOf().listOf().fieldOf("pattern").forGetter(requirement -> requirement.pattern),
                    Codec.unboundedMap(Codecs.CHARACTER_CODEC, Codecs.PARTIAL_BLOCK_STATE_CODEC).fieldOf("keys").forGetter(requirement -> requirement.keys)
            ).apply(structureRequirementInstance, StructureRequirement::new)
    );

    private List<List<String>> pattern;
    private Map<Character, PartialBlockState> keys;
    private BlockPattern structure;

    public StructureRequirement(List<List<String>> pattern, Map<Character, PartialBlockState> keys) {
        super(MODE.INPUT);
        this.pattern = pattern;
        this.keys = keys;
        BlockPatternBuilder builder = BlockPatternBuilder.start();
        for(List<String> levels : pattern)
            builder.aisle(levels.toArray(new String[0]));
        for(Map.Entry<Character, PartialBlockState> key : keys.entrySet())
            builder.where(key.getKey(), key.getValue());
        this.structure = builder.build();
    }

    @Override
    public RequirementType<StructureRequirement> getType() {
        return Registration.STRUCTURE_REQUIREMENT.get();
    }

    @Override
    public boolean test(StructureMachineComponent component, CraftingContext context) {
        return component.checkStructure(this.structure) != null;
    }

    @Override
    public CraftingResult processStart(StructureMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(StructureMachineComponent component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<StructureMachineComponent> getComponentType() {
        return Registration.STRUCTURE_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(StructureMachineComponent component, CraftingContext context) {
        if(component.checkStructure(this.structure) != null)
            return CraftingResult.success();
        else return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.structure.error"));
    }
}
