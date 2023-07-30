package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo.TooltipPredicate;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.IDelayedRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.render.CustomMachineRenderer;
import fr.frinn.custommachinery.common.component.StructureMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.network.CPlaceStructurePacket;
import fr.frinn.custommachinery.common.util.BlockStructure;
import fr.frinn.custommachinery.common.util.PartialBlockState;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.requirement.AbstractDelayedChanceableRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StructureRequirement extends AbstractDelayedChanceableRequirement<StructureMachineComponent> implements ITickableRequirement<StructureMachineComponent>, IDisplayInfoRequirement {

    public static final NamedCodec<StructureRequirement> CODEC = NamedCodec.record(structureRequirementInstance ->
            structureRequirementInstance.group(
                    NamedCodec.STRING.listOf().listOf().fieldOf("pattern").forGetter(requirement -> requirement.pattern),
                    NamedCodec.unboundedMap(DefaultCodecs.CHARACTER, IIngredient.BLOCK, "Map<Character, Block>").fieldOf("keys").forGetter(requirement -> requirement.keys),
                    NamedCodec.BOOL.optionalFieldOf("destroy", false).forGetter(requirement -> requirement.destroy),
                    NamedCodec.BOOL.optionalFieldOf("drops", true).forGetter(requirement -> requirement.drops),
                    NamedCodec.doubleRange(0.0, 1.0).optionalFieldOf("chance", 1.0).forGetter(AbstractDelayedChanceableRequirement::getChance),
                    NamedCodec.doubleRange(0.0, 1.0).optionalFieldOf("delay", 0.0).forGetter(IDelayedRequirement::getDelay)
            ).apply(structureRequirementInstance, (pattern, keys, destroy, drops, chance, delay) -> {
                    StructureRequirement requirement = new StructureRequirement(pattern, keys, destroy, drops);
                    requirement.setChance(chance);
                    requirement.setDelay(delay);
                    return requirement;
            }), "Structure requirement"
    );

    private final List<List<String>> pattern;
    private final Map<Character, IIngredient<PartialBlockState>> keys;
    private final boolean destroy;
    private final boolean drops;
    private final BlockStructure structure;

    public StructureRequirement(List<List<String>> pattern, Map<Character, IIngredient<PartialBlockState>> keys, boolean destroy, boolean drops) {
        super(RequirementIOMode.INPUT);
        this.pattern = pattern;
        this.keys = keys;
        this.destroy = destroy;
        this.drops = drops;
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
        if(getDelay() == 0.0D && this.destroy)
            component.destroyStructure(this.structure, this.drops);
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(StructureMachineComponent component, ICraftingContext context) {
        if(getDelay() == 1.0D && this.destroy)
            component.destroyStructure(this.structure, this.drops);
        return CraftingResult.success();
    }

    @Override
    public MachineComponentType<StructureMachineComponent> getComponentType() {
        return Registration.STRUCTURE_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(StructureMachineComponent component, ICraftingContext context) {
        if(this.destroy && getDelay() < context.getRemainingTime() / context.getRecipe().getRecipeTime())
            return CraftingResult.pass();

        if(component.checkStructure(this.structure))
            return CraftingResult.success();
        else return CraftingResult.error(Component.translatable("custommachinery.requirements.structure.error"));
    }

    @Override
    public CraftingResult execute(StructureMachineComponent component, ICraftingContext context) {
        if(getDelay() != 0.0D && getDelay() != 1.0D && this.destroy)
            component.destroyStructure(this.structure, this.drops);
        return CraftingResult.pass();
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.addTooltip(Component.translatable("custommachinery.requirements.structure.info"));
        info.addTooltip(Component.translatable("custommachinery.requirements.structure.click"));
        this.pattern.stream().flatMap(List::stream).flatMap(s -> s.chars().mapToObj(c -> (char)c)).collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).forEach((key, amount) -> {
            IIngredient<PartialBlockState> ingredient = this.keys.get(key);
            if(ingredient != null && amount > 0)
                info.addTooltip(Component.translatable("custommachinery.requirements.structure.list", amount, Utils.getBlockName(ingredient).withStyle(ChatFormatting.GOLD)));
        });
        if(this.destroy) {
            if(this.drops)
                info.addTooltip(Component.translatable("custommachinery.requirements.structure.break").withStyle(ChatFormatting.DARK_RED));
            else
                info.addTooltip(Component.translatable("custommachinery.requirements.structure.destroy").withStyle(ChatFormatting.DARK_RED));
        }
        info.addTooltip(Component.translatable("custommachinery.requirements.structure.creative").withStyle(ChatFormatting.DARK_PURPLE), TooltipPredicate.CREATIVE);
        info.setClickAction((machine, recipe, mouseButton) -> {
            if(ClientHandler.isShiftKeyDown())
                new CPlaceStructurePacket(machine.getId(), this.pattern, this.keys).sendToServer();
            else
                CustomMachineRenderer.addRenderBlock(machine.getId(), this.structure::getBlocks);
        });
        info.setItemIcon(Items.STRUCTURE_BLOCK);
    }
}