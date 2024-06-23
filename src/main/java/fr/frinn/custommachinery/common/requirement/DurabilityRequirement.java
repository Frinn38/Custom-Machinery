package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractChanceableRequirement;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CraftingHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class DurabilityRequirement extends AbstractChanceableRequirement<ItemComponentHandler> implements IJEIIngredientRequirement<ItemStack> {

    public static final NamedCodec<DurabilityRequirement> CODEC = NamedCodec.record(durabilityRequirementInstance ->
            durabilityRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    NamedCodec.of(CraftingHelper.makeIngredientCodec(true)).fieldOf("item").forGetter(requirement -> requirement.item),
                    NamedCodec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(requirement -> requirement.amount),
                    NamedCodec.BOOL.optionalFieldOf("break", false).forGetter(requirement -> requirement.canBreak),
                    NamedCodec.doubleRange(0.0D, 1.0D).optionalFieldOf("chance", 1.0D).forGetter(AbstractChanceableRequirement::getChance),
                    NamedCodec.STRING.optionalFieldOf("slot", "").forGetter(requirement -> requirement.slot)
            ).apply(durabilityRequirementInstance, (mode, item, amount, canBreak, chance, slot) -> {
                    DurabilityRequirement requirement = new DurabilityRequirement(mode, item, amount, canBreak, slot);
                    requirement.setChance(chance);
                    return requirement;
            }), "Durability requirement"
    );

    private final Ingredient item;
    private final int amount;
    private final String slot;
    private final boolean canBreak;

    public DurabilityRequirement(RequirementIOMode mode, Ingredient item, int amount, boolean canBreak, String slot) {
        super(mode);
        this.item = item;
        this.amount = amount;
        this.canBreak = canBreak;
        this.slot = slot;
    }

    @Override
    public RequirementType<DurabilityRequirement> getType() {
        return Registration.DURABILITY_REQUIREMENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT)
            return Arrays.stream(this.item.getItems()).mapToInt(item -> component.getDurabilityAmount(this.slot, item)).sum() >= amount;
        else
            return Arrays.stream(this.item.getItems()).mapToInt(item -> component.getSpaceForDurability(this.slot, item)).sum() >= amount;
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            int maxRemove = Arrays.stream(this.item.getItems()).mapToInt(item -> component.getDurabilityAmount(this.slot, item)).sum();
            if(maxRemove >= amount) {
                int toDamage = amount;
                for (ItemStack item : this.item.getItems()) {
                    int canDamage = component.getDurabilityAmount(this.slot, item);
                    if(canDamage > 0) {
                        canDamage = Math.min(canDamage, toDamage);
                        component.removeDurability(this.slot, item, canDamage, this.canBreak);
                        toDamage -= canDamage;
                        if(toDamage == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.durability.error.input", this.item, amount, maxRemove));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getIntegerModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.OUTPUT) {
            int maxRepair = Arrays.stream(this.item.getItems()).mapToInt(item -> component.getSpaceForDurability(this.slot, item)).sum();
            if(maxRepair >= amount) {
                int toRepair = amount;
                for (ItemStack item : this.item.getItems()) {
                    int canRepair = component.getSpaceForDurability(this.slot, item);
                    if(canRepair > 0) {
                        canRepair = Math.min(canRepair, toRepair);
                        component.repairItem(this.slot, item, canRepair);
                        toRepair -= canRepair;
                        if(toRepair == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(Component.translatable("custommachinery.requirements.durability.error.output", this.item, amount, maxRepair));
        }
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public List<IJEIIngredientWrapper<ItemStack>> getJEIIngredientWrappers(IMachineRecipe recipe) {
        return Collections.singletonList(new ItemIngredientWrapper(this.getMode(), this.item, this.amount, getChance(), true, this.slot, true));
    }
}
