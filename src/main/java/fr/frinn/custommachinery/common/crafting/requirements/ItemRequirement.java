package fr.frinn.custommachinery.common.crafting.requirements;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.Utils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;
import java.util.Optional;

public class ItemRequirement extends AbstractRequirement<ItemComponentHandler> {

    private static final Item DEFAULT_ITEM = Items.AIR;
    private static final ResourceLocation DEFAULT_TAG = new ResourceLocation(CustomMachinery.MODID, "dummy");

    @SuppressWarnings("deprecation")
    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(itemRequirementInstance ->
            itemRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    Registry.ITEM.optionalFieldOf("item", DEFAULT_ITEM).forGetter(requirement -> requirement.item),
                    ResourceLocation.CODEC.optionalFieldOf("tag", DEFAULT_TAG).forGetter(requirement -> requirement.tag != null ? TagCollectionManager.getManager().getItemTags().getDirectIdFromTag(requirement.tag) : DEFAULT_TAG),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount)
            ).apply(itemRequirementInstance, ItemRequirement::new)
    );

    private Item item;
    private ITag<Item> tag;
    private int amount;

    public ItemRequirement(MODE mode, Item item, ResourceLocation tagLocation, int amount) {
        super(mode);
        this.amount = amount;
        if(mode == MODE.OUTPUT) {
            if(item != DEFAULT_ITEM)
                this.item = item;
            else throw new IllegalArgumentException("You must specify an item for an Output Item Requirement");
        } else {
            if(item == DEFAULT_ITEM) {
                if(tagLocation == DEFAULT_TAG)
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
    }

    @Override
    public RequirementType getType() {
        return Registration.ITEM_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component) {
        if(getMode() == MODE.INPUT) {
            if(this.item != null && this.item != DEFAULT_ITEM)
                return component.getItemAmount(this.item) >= this.amount;
            else if(this.tag != null)
                return this.tag.getAllElements().stream().mapToInt(component::getItemAmount).sum() >= this.amount;
            else throw new IllegalStateException("Using Input Item Requirement with null item and tag");
        } else {
            if(this.item != null && this.item != DEFAULT_ITEM)
                return component.getSpaceForItem(this.item) >= this.amount;
            else throw new IllegalStateException("Using Output Item Requirement with null item");
        }
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component) {
        if(getMode() == MODE.INPUT) {
            if(this.item != null && this.item != DEFAULT_ITEM) {
                int canExtract = component.getItemAmount(this.item);
                if(canExtract >= this.amount) {
                    component.removeFromInputs(this.item, this.amount);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.input", new TranslationTextComponent(this.item.getTranslationKey()), this.amount, canExtract));
            } else if(this.tag != null) {
                int maxExtract = this.tag.getAllElements().stream().mapToInt(component::getItemAmount).sum();
                if(maxExtract >= this.amount) {
                    int toExtract = this.amount;
                    for (Item item : this.tag.getAllElements()) {
                        int canExtract = component.getItemAmount(item);
                        if(canExtract > 0) {
                            canExtract = Math.min(canExtract, toExtract);
                            component.removeFromInputs(item, canExtract);
                            toExtract -= canExtract;
                            if(toExtract == 0)
                                return CraftingResult.success();
                        }
                    }
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.input", Utils.getItemTagID(this.tag), this.amount, maxExtract));
            } else throw new IllegalStateException("Using Input Item Requirement with null item and tag");
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component) {
        if(getMode() == MODE.OUTPUT) {
            int canInsert = component.getSpaceForItem(this.item);
            if(canInsert >= this.amount) {
                component.addToOutputs(this.item, this.amount);
                return CraftingResult.success();
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.output", this.amount, new TranslationTextComponent(this.item.getTranslationKey())));
        }
        return CraftingResult.pass();
    }

    @Override
    public IIngredientType<?> getJEIIngredientType() {
        return VanillaTypes.ITEM;
    }

    @Override
    public Object asJEIIngredient() {
        return new ItemStack(this.item, this.amount);
    }

    @Override
    public void addJeiIngredients(IIngredients ingredients) {
        int maxStackSize = this.item.getDefaultInstance().getMaxStackSize();
        int toAdd = this.amount;
        while (toAdd > 0) {
            int added = MathHelper.clamp(toAdd, 0, maxStackSize);
            if(getMode() == MODE.INPUT)
                ingredients.setInput(VanillaTypes.ITEM, new ItemStack(this.item, added));
            else
                ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(this.item, added));
            toAdd -= added;
        }
    }
}
