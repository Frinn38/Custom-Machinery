package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.LootTableIngredientWrapper;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LootTableRequirement extends AbstractRequirement<ItemComponentHandler> implements IJEIIngredientRequirement {

    public static final Codec<LootTableRequirement> CODEC = RecordCodecBuilder.create(lootTableRequirementInstance ->
            lootTableRequirementInstance.group(
                    ResourceLocation.CODEC.fieldOf("table").forGetter(requirement -> requirement.lootTable),
                    Codec.FLOAT.optionalFieldOf("luck", 0.0F).forGetter(requirement -> requirement.luck)
            ).apply(lootTableRequirementInstance, LootTableRequirement::new)
    );

    private ResourceLocation lootTable;
    private float luck;
    private List<ItemStack> toOutput = Collections.emptyList();

    public LootTableRequirement(ResourceLocation lootTable, float luck) {
        super(MODE.OUTPUT);
        this.lootTable = lootTable;
        this.luck = luck;
        LootTableHelper.addTable(lootTable);
    }

    @Override
    public RequirementType<?> getType() {
        return Registration.LOOT_TABLE_REQUIREMENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component, CraftingContext context) {
        return true;
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, CraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, CraftingContext context) {
        if(getMode() == MODE.INPUT)
            return CraftingResult.pass();

        if(toOutput.isEmpty()) {
            LootTable table = context.getTile().getWorld().getServer().getLootTableManager().getLootTableFromLocation(this.lootTable);
            LootContext lootContext = new LootContext.Builder((ServerWorld) context.getTile().getWorld())
                    .withParameter(LootParameters.ORIGIN, Vector3d.copyCentered(context.getTile().getPos()))
                    .withParameter(LootParameters.BLOCK_ENTITY, context.getTile())
                    .withLuck((float) context.getModifiedvalue(this.luck, this, "luck"))
                    .build(Registration.CUSTOM_MACHINE_LOOT_PARAMERTER_SET);
            toOutput = table.generate(lootContext);
        }

        Iterator<ItemStack> iterator = toOutput.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            if(component.getSpaceForItem("", stack.getItem(), stack.getTag()) < stack.getCount())
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.output", stack.getCount(), new TranslationTextComponent(stack.getTranslationKey())));
            component.addToOutputs("", stack.getItem(), stack.getCount(), stack.getTag());
            iterator.remove();
        }
        return CraftingResult.success();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public LootTableIngredientWrapper getJEIIngredientWrapper() {
        return new LootTableIngredientWrapper(this.lootTable);
    }
}
