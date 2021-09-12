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
import java.util.Random;

public class ItemRequirement extends AbstractRequirement<ItemComponentHandler> implements IChanceableRequirement<ItemComponentHandler>, IJEIIngredientRequirement {

    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(itemRequirementInstance ->
            itemRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    Ingredient.ItemIngredient.CODEC.fieldOf("item").forGetter(requirement -> requirement.item),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codecs.COMPOUND_NBT_CODEC,"nbt", new CompoundNBT()).forGetter(requirement -> requirement.nbt),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0),"chance", 1.0D).forGetter(requirement -> requirement.chance),
                    CodecLogger.loggedOptional(Codec.STRING,"slot", "").forGetter(requirement -> requirement.slot)
            ).apply(itemRequirementInstance, (mode, item, amount, nbt, chance, slot) -> {
                    ItemRequirement requirement = new ItemRequirement(mode, item, amount, nbt, slot);
                    requirement.setChance(chance);
                    return requirement;
            })
    );

    private Ingredient.ItemIngredient item;
    private int amount;
    private CompoundNBT nbt;
    private double chance = 1.0D;
    private String slot;

    public ItemRequirement(MODE mode, Ingredient.ItemIngredient item, int amount, @Nullable CompoundNBT nbt, String slot) {
        super(mode);
        if(mode == MODE.OUTPUT && item.getObject() == null)
            throw new IllegalArgumentException("You can't use a Tag for an Output Item Requirement");
        this.item = item;
        this.amount = amount;
        this.nbt = nbt == null ? new CompoundNBT() : nbt;
        this.slot = slot;
    }

    @Override
    public RequirementType<ItemRequirement> getType() {
        return Registration.ITEM_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            return this.item.getAll().stream().mapToInt(item -> component.getItemAmount(this.slot, item, this.nbt)).sum() >= amount;
        } else {
            if(this.item.getObject() != null)
                return component.getSpaceForItem(this.slot, this.item.getObject(), this.nbt) >= amount;
            else throw new IllegalStateException("Can't use output item requirement with item tag");
        }
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            int maxExtract = this.item.getAll().stream().mapToInt(item -> component.getItemAmount(this.slot, item, this.nbt)).sum();
            if(maxExtract >= amount) {
                int toExtract = amount;
                for (Item item : this.item.getAll()) {
                    int canExtract = component.getItemAmount(this.slot, item, this.nbt);
                    if(canExtract > 0) {
                        canExtract = Math.min(canExtract, toExtract);
                        component.removeFromInputs(this.slot, item, canExtract, this.nbt);
                        toExtract -= canExtract;
                        if(toExtract == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.input", this.item.toString(), amount, maxExtract));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.OUTPUT) {
            if(this.item.getObject() != null) {
                Item item = this.item.getObject();
                int canInsert = component.getSpaceForItem(this.slot, item, this.nbt);
                if(canInsert >= amount) {
                    component.addToOutputs(this.slot, item, amount, this.nbt);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.output", amount, new TranslationTextComponent(item.getTranslationKey())));
            } else throw new IllegalStateException("Can't use output item requirement with item tag");
        }
        return CraftingResult.pass();
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
        return new ItemIngredientWrapper(this.getMode(), this.item, this.amount, this.chance, false, this.nbt, this.slot);
    }
}
