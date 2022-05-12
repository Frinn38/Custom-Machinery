package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.requirement.IChanceableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.Random;

public class DurabilityRequirement extends AbstractRequirement<ItemComponentHandler> implements IChanceableRequirement<ItemComponentHandler>, IJEIIngredientRequirement<ItemStack> {

    public static final Codec<DurabilityRequirement> CODEC = RecordCodecBuilder.create(durabilityRequirementInstance ->
            durabilityRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    IIngredient.ITEM.fieldOf("item").forGetter(requirement -> requirement.item),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codecs.COMPOUND_NBT_CODEC,"nbt", new CompoundTag()).forGetter(requirement -> requirement.nbt),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0D, 1.0D),"chance", 1.0D).forGetter(requirement -> requirement.chance),
                    CodecLogger.loggedOptional(Codec.STRING,"slot", "").forGetter(requirement -> requirement.slot)
            ).apply(durabilityRequirementInstance, (mode, item, amount, nbt, chance, slot) -> {
                    DurabilityRequirement requirement = new DurabilityRequirement(mode, item, amount, nbt, slot);
                    requirement.setChance(chance);
                    return requirement;
            })
    );

    private final IIngredient<Item> item;
    private final int amount;
    private final CompoundTag nbt;
    private double chance = 1.0D;
    private final String slot;
    private final Lazy<ItemIngredientWrapper> wrapper;

    public DurabilityRequirement(RequirementIOMode mode, IIngredient<Item> item, int amount, @Nullable CompoundTag nbt, String slot) {
        super(mode);
        this.item = item;
        this.amount = amount;
        this.nbt = nbt == null ? new CompoundTag() : nbt;
        this.slot = slot;
        this.wrapper = Lazy.of(() -> new ItemIngredientWrapper(this.getMode(), this.item, this.amount, this.chance, true, this.nbt, this.slot));
    }

    @Override
    public RequirementType<DurabilityRequirement> getType() {
        return Registration.DURABILITY_REQUIREMENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT)
            return this.item.getAll().stream().mapToInt(item -> component.getDurabilityAmount(this.slot, item, this.nbt)).sum() >= amount;
        else
            return this.item.getAll().stream().mapToInt(item -> component.getSpaceForDurability(this.slot, item, this.nbt)).sum() >= amount;
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT) {
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
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.durability.error.input", this.item, amount, maxRemove));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.OUTPUT) {
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
            return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.items.error.durability.output", this.item, amount, maxRepair));
        }
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public void setChance(double chance) {
        this.chance = Mth.clamp(chance, 0.0, 1.0);
    }

    @Override
    public boolean shouldSkip(ItemComponentHandler component, Random rand, ICraftingContext context) {
        double chance = context.getModifiedValue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    @Override
    public ItemIngredientWrapper getJEIIngredientWrapper() {
        return this.wrapper.get();
    }
}
