package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingContext;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.common.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Random;

public class ItemRequirement extends AbstractRequirement<ItemComponentHandler> implements IChanceableRequirement<ItemComponentHandler>, IJEIIngredientRequirement {

    private static final Item DEFAULT_ITEM = Items.AIR;
    private static final ResourceLocation DEFAULT_TAG = new ResourceLocation(CustomMachinery.MODID, "dummy");

    @SuppressWarnings("deprecation")
    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(itemRequirementInstance ->
            itemRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    Registry.ITEM.optionalFieldOf("item", DEFAULT_ITEM).forGetter(requirement -> requirement.item),
                    ResourceLocation.CODEC.optionalFieldOf("tag", DEFAULT_TAG).forGetter(requirement -> requirement.tag != null ? Utils.getItemTagID(requirement.tag) : DEFAULT_TAG),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    Codecs.COMPOUND_NBT_CODEC.optionalFieldOf("nbt", new CompoundNBT()).forGetter(requirement -> requirement.nbt),
                    Codec.DOUBLE.optionalFieldOf("chance", 1.0D).forGetter(requirement -> requirement.chance),
                    Codec.STRING.optionalFieldOf("slot", "").forGetter(requirement -> requirement.slot)
            ).apply(itemRequirementInstance, ItemRequirement::new)
    );

    private Item item;
    private ITag<Item> tag;
    private int amount;
    private CompoundNBT nbt;
    private double chance;
    private String slot;

    public ItemRequirement(MODE mode, Item item, ResourceLocation tagLocation, int amount, CompoundNBT nbt, double chance, String slot) {
        super(mode);
        this.amount = amount;
        this.nbt = nbt == null || nbt.isEmpty() ? null : nbt;
        if(mode == MODE.OUTPUT) {
            if(item != DEFAULT_ITEM && item != null)
                this.item = item;
            else throw new IllegalArgumentException("You must specify an item for an Output Item Requirement");
        } else {
            if(item == DEFAULT_ITEM || item == null) {
                if(tagLocation == DEFAULT_TAG || tagLocation == null)
                    throw  new IllegalArgumentException("You must specify either an item or an item tag for an Input Item Requirement");
                ITag<Item> tag = TagCollectionManager.getManager().getItemTags().get(tagLocation);
                if(tag == null)
                    throw new IllegalArgumentException("The item tag: " + tagLocation + " doesn't exist");
                if(!tag.getAllElements().isEmpty())
                    this.tag = tag;
                else throw new IllegalArgumentException("The item tag: " + tagLocation + " doesn't contains any item");
            } else {
                this.item = item;
            }
        }
        this.chance = MathHelper.clamp(chance, 0.0D, 1.0D);
        this.slot = slot;
        this.itemIngredientWrapper = new ItemIngredientWrapper(this.getMode(), this.item, this.amount, this.tag, this.chance, false, this.nbt, this.slot);
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
            if(this.item != null && this.item != DEFAULT_ITEM)
                return component.getItemAmount(this.slot, this.item, this.nbt) >= amount;
            else if(this.tag != null)
                return this.tag.getAllElements().stream().mapToInt(item -> component.getItemAmount(this.slot, item, this.nbt)).sum() >= amount;
            else throw new IllegalStateException("Using Input Item Requirement with null item and tag");
        } else {
            if(this.item != null && this.item != DEFAULT_ITEM)
                return component.getSpaceForItem(this.slot, this.item, this.nbt) >= amount;
            else throw new IllegalStateException("Using Output Item Requirement with null item");
        }
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.INPUT) {
            if(this.item != null && this.item != DEFAULT_ITEM) {
                int canExtract = component.getItemAmount(this.slot, this.item, this.nbt);
                if(canExtract >= amount) {
                    component.removeFromInputs(this.slot, this.item, amount, this.nbt);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.input", new TranslationTextComponent(this.item.getTranslationKey()), amount, canExtract));
            } else if(this.tag != null) {
                int maxExtract = this.tag.getAllElements().stream().mapToInt(item -> component.getItemAmount(this.slot, item, this.nbt)).sum();
                if(maxExtract >= amount) {
                    int toExtract = amount;
                    for (Item item : this.tag.getAllElements()) {
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
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.input", Utils.getItemTagID(this.tag), amount, maxExtract));
            } else throw new IllegalStateException("Using Input Item Requirement with null item and tag");
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, CraftingContext context) {
        int amount = (int)context.getModifiedvalue(this.amount, this, null);
        if(getMode() == MODE.OUTPUT) {
            if(this.item != null && this.item != DEFAULT_ITEM) {
                int canInsert = component.getSpaceForItem(this.slot, this.item, this.nbt);
                if(canInsert >= amount) {
                    component.addToOutputs(this.slot, this.item, amount, this.nbt);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.output", amount, new TranslationTextComponent(this.item.getTranslationKey())));
            } else throw new IllegalStateException("Using Output Item Requirement with null item");
        }
        return CraftingResult.pass();
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
