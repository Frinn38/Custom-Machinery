package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Random;

public class DurabilityRequirement extends AbstractRequirement<ItemComponentHandler> implements IChanceableRequirement<ItemComponentHandler>, IJEIIngredientRequirement {

    public static final Codec<DurabilityRequirement> CODEC = RecordCodecBuilder.create(durabilityRequirementInstance ->
            durabilityRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    Registry.ITEM.fieldOf("item").forGetter(requirement -> requirement.item),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(requirement -> requirement.amount),
                    Codecs.COMPOUND_NBT_CODEC.optionalFieldOf("nbt", new CompoundNBT()).forGetter(requirement -> requirement.nbt),
                    Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("chance", 1.0D).forGetter(requirement -> requirement.chance),
                    Codec.STRING.optionalFieldOf("slot", "").forGetter(requirement -> requirement.slot)
            ).apply(durabilityRequirementInstance, DurabilityRequirement::new)
    );

    private Item item;
    private int amount;
    private CompoundNBT nbt;
    private double chance;
    private String slot;

    public DurabilityRequirement(MODE mode, Item item, int amount, CompoundNBT nbt, double chance, String slot) {
        super(mode);
        if(!item.isDamageable())
            throw new IllegalArgumentException("Invalid Item in Durability requirement: " + item.getRegistryName() + " can't be damaged !");
        this.item = item;
        this.amount = amount;
        this.nbt = nbt;
        this.chance = chance;
        this.slot = slot;
        this.itemIngredientWrapper = new ItemIngredientWrapper(this.getMode(), this.item, this.amount, null, this.chance, true, this.nbt, this.slot);
    }

    @Override
    public RequirementType<DurabilityRequirement> getType() {
        return Registration.DURABILITY_REQUIREMENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT)
            return component.getDurabilityAmount(this.slot, this.item, this.nbt) >= amount;
        else
            return component.getSpaceForDurability(this.slot, this.item, this.nbt) >= amount;
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            int canRemove = component.getDurabilityAmount(this.slot, this.item, this.nbt);
            if(canRemove >= amount) {
                component.removeDurability(this.slot, this.item, amount, this.nbt);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.durability.error.input", new TranslationTextComponent(this.item.getTranslationKey()), amount, canRemove));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.OUTPUT) {
            int maxRepair = component.getSpaceForDurability(this.slot, this.item, this.nbt);
            if(maxRepair >= amount) {
                component.repairItem(this.slot, this.item, amount, this.nbt);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.items.error.durability.output", new TranslationTextComponent(this.item.getTranslationKey()), amount, maxRepair));
        }
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean testChance(ItemComponentHandler component, Random rand, CraftingContext context) {
        double chance = context.getModifiedvalue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    private ItemIngredientWrapper itemIngredientWrapper;
    @Override
    public ItemIngredientWrapper getJEIIngredientWrapper() {
        return this.itemIngredientWrapper;
    }
}
