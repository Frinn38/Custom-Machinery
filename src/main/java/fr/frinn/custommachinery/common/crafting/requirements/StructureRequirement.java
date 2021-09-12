package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.StructureMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.common.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.util.BlockStructure;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Map;

public class StructureRequirement extends AbstractTickableRequirement<StructureMachineComponent> implements IDisplayInfoRequirement<StructureMachineComponent> {

    public static final Codec<StructureRequirement> CODEC = RecordCodecBuilder.create(structureRequirementInstance ->
            structureRequirementInstance.group(
                    Codecs.list(Codecs.list(Codec.STRING)).fieldOf("pattern").forGetter(requirement -> requirement.pattern),
                    Codec.unboundedMap(Codecs.CHARACTER_CODEC, Codecs.PARTIAL_BLOCK_STATE_CODEC).fieldOf("keys").forGetter(requirement -> requirement.keys),
                    CodecLogger.loggedOptional(Codec.BOOL,"jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(structureRequirementInstance, (pattern, keys, jei) -> {
                    StructureRequirement requirement = new StructureRequirement(pattern, keys);
                    requirement.setJeiVisible(jei);
                    return requirement;
            })
    );

    private List<List<String>> pattern;
    private Map<Character, PartialBlockState> keys;
    private BlockStructure structure;
    private boolean jeiVisible = true;

    public StructureRequirement(List<List<String>> pattern, Map<Character, PartialBlockState> keys) {
        super(MODE.INPUT);
        this.pattern = pattern;
        this.keys = keys;
        BlockStructure.Builder builder = BlockStructure.Builder.start();
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
        return component.checkStructure(this.structure);
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
        if(component.checkStructure(this.structure))
            return CraftingResult.success();
        else return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.structure.error"));
    }

    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public RequirementDisplayInfo getDisplayInfo() {
        RequirementDisplayInfo info = new RequirementDisplayInfo();
        info.addTooltip(new TranslationTextComponent("custommachinery.requirements.structure.info"));
        info.addTooltip(new TranslationTextComponent("custommachinery.requirements.structure.click"));
        info.setClickAction((machine, mouseButton) -> CustomMachineRenderer.addRenderBlock(machine.getId(), this.structure::getBlocks));
        info.setVisible(this.jeiVisible);
        return info;
    }
}