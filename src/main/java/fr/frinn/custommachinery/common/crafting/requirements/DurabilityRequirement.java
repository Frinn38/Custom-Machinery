package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.utils.CodecLogger;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Ingredient;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class DurabilityRequirement extends AbstractRequirement<ItemComponentHandler> implements IChanceableRequirement<ItemComponentHandler>, IJEIIngredientRequirement {

    public static final Codec<DurabilityRequirement> CODEC = RecordCodecBuilder.create(durabilityRequirementInstance ->
            durabilityRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    Ingredient.ItemIngredient.CODEC.fieldOf("item").forGetter(requirement -> requirement.item),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codecs.COMPOUND_NBT_CODEC,"nbt", new CompoundNBT()).forGetter(requirement -> requirement.nbt),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0D, 1.0D),"chance", 1.0D).forGetter(requirement -> requirement.chance),
                    CodecLogger.loggedOptional(Codec.STRING,"slot", "").forGetter(requirement -> requirement.slot)
            ).apply(durabilityRequirementInstance, (mode, item, amount, nbt, chance, slot) -> {
                    DurabilityRequirement requirement = new DurabilityRequirement(mode, item, amount, nbt, slot);
                    requirement.setChance(chance);
                    return requirement;
            })
    );

    private Ingredient.ItemIngredient item;
    private int amount;
    private CompoundNBT nbt;
    private double chance = 1.0D;
    private String slot;

    public DurabilityRequirement(MODE mode, Ingredient.ItemIngredient item, int amount, @Nullable CompoundNBT nbt, String slot) {
        super(mode);
        if(item.getAll().stream().noneMatch(Item::isDamageable))
            throw new IllegalArgumentException("Invalid Item in Durability requirement: " + item + " can't be damaged !");
        this.item = item;
        this.amount = amount;
        this.nbt = nbt == null ? new CompoundNBT() : nbt;
        this.slot = slot;
    }

    @Override
    public RequirementType<DurabilityRequirement> getType() {
        return Registration.DURABILITY_REQUIREMENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT)
            return this.item.getAll().stream().mapToInt(item -> component.getDurabilityAmount(this.slot, item, this.nbt)).sum() >= amount;
        else
            return this.item.getAll().stream().mapToInt(item -> component.getSpaceForDurability(this.slot, item, this.nbt)).sum() >= amount;
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            int maxRemove = this.item.getAll().stream().mapToInt(item -> component.getDurabilityAmount(this.slot, item, this.nbt)).sum();
            if(maxRemove >= amount) {
                int toDamage = amount;
                for (Item item : this.item.getAll()) {
                    int canDamage = component.getDurabilityAmount(this.slot, item, this.nbt);
                    if(canDamage > 0) {
                        canDamage = Math.min(canDamage, toDamage);
                        component.removeDurability(this.slot, item, canDamage, this.nbt);
                        toDamage -= canDamage;
                        if(toDamage == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.durability.error.input", this.item, amount, maxRemove));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.OUTPUT) {
            int maxRepair = this.item.getAll().stream().mapToInt(item -> component.getSpaceForDurability(this.slot, item, this.nbt)).sum();
            if(maxRepair >= amount) {
                int toRepair = amount;
                for (Item item : this.item.getAll()) {
                    int canRepair = component.getSpaceForDurability(this.slot, item, this.nbt);
                    if(canRepair > 0) {
                        canRepair = Math.min(canRepair, toRepair);
                        component.repairItem(this.slot, item, canRepair, this.nbt);
                        toRepair -= canRepair;
                        if(toRepair == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.items.error.durability.output", this.item, amount, maxRepair));
        }
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public void setChance(double chance) {
        this.chance = MathHelper.clamp(chance, 0.0, 1.0);
    }

    @Override
    public boolean testChance(ItemComponentHandler component, Random rand, CraftingContext context) {
        double chance = context.getModifiedvalue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    @Override
    public ItemIngredientWrapper getJEIIngredientWrapper() {
        return new ItemIngredientWrapper(this.getMode(), this.item, this.amount, this.chance, true, this.nbt, this.slot);
    }
}
