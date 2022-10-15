package fr.frinn.custommachinery.common.requirement;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemIngredient;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import fr.frinn.custommachinery.impl.codec.RegistrarCodec;
import fr.frinn.custommachinery.impl.requirement.AbstractChanceableRequirement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ItemTransformRequirement extends AbstractChanceableRequirement<ItemComponentHandler> implements IJEIIngredientRequirement<ItemStack> {

    public static final Codec<ItemTransformRequirement> CODEC = RecordCodecBuilder.create(itemTransformRequirementInstance ->
            itemTransformRequirementInstance.group(
                    IIngredient.ITEM.fieldOf("input").forGetter(requirement -> requirement.input),
                    CodecLogger.loggedOptional(Codec.intRange(1, Integer.MAX_VALUE), "input_amount", 1).forGetter(requirement -> requirement.inputAmount),
                    CodecLogger.loggedOptional(Codec.STRING, "input_slot", "").forGetter(requirement -> requirement.inputSlot),
                    CodecLogger.loggedOptional(Codecs.COMPOUND_NBT_CODEC, "input_nbt").forGetter(requirement -> Optional.ofNullable(requirement.inputNBT)),
                    CodecLogger.loggedOptional(RegistrarCodec.ITEM, "output", Items.AIR).forGetter(requirement -> requirement.output),
                    CodecLogger.loggedOptional(Codec.intRange(1, Integer.MAX_VALUE), "output_amount", 1).forGetter(requirement -> requirement.outputAmount),
                    CodecLogger.loggedOptional(Codec.STRING, "output_slot", "").forGetter(requirement -> requirement.outputSlot),
                    CodecLogger.loggedOptional(Codec.BOOL, "copy_nbt", true).forGetter(requirement -> requirement.copyNBT),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0D, 1.0D), "chance", 1.0D).forGetter(AbstractChanceableRequirement::getChance)
            ).apply(itemTransformRequirementInstance, (input, inputAmount, inputSlot, inputNBT, output, outputAmount, outputSlot, copyNBT, chance) -> {
                ItemTransformRequirement requirement = new ItemTransformRequirement(input, inputAmount, inputSlot, inputNBT.orElse(null), output, outputAmount, outputSlot, copyNBT, null);
                requirement.setChance(chance);
                return requirement;
            })
    );

    private final IIngredient<Item> input;
    private final int inputAmount;
    private final String inputSlot;
    @Nullable
    private final CompoundTag inputNBT;
    private final Item output;
    private final int outputAmount;
    private final String outputSlot;
    private final boolean copyNBT;
    @Nullable
    private final Function<CompoundTag, CompoundTag> nbt;

    public ItemTransformRequirement(IIngredient<Item> input, int inputAmount, String inputSlot, @Nullable CompoundTag inputNBT, Item output, int outputAmount, String outputSlot, boolean copyNBT, @Nullable Function<CompoundTag, CompoundTag> nbt) {
        super(RequirementIOMode.OUTPUT);
        this.input = input;
        this.inputAmount = inputAmount;
        this.inputSlot = inputSlot;
        this.inputNBT = inputNBT;
        this.output = output;
        this.outputAmount = outputAmount;
        this.outputSlot = outputSlot;
        this.copyNBT = copyNBT;
        this.nbt = nbt;
    }

    @Override
    public RequirementType<ItemTransformRequirement> getType() {
        return Registration.ITEM_TRANSFORM_REQUIREMENT.get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component, ICraftingContext context) {
        return this.input.getAll().stream().anyMatch(item -> {
            if(component.getItemAmount(this.inputSlot, item, this.inputNBT) < this.inputAmount)
                return false;
            CompoundTag inputNBT = component.getComponents().stream().filter(slot -> slot.getItemStack().getItem() == item).findFirst().map(slot -> slot.getItemStack().getTag()).map(CompoundTag::copy).orElse(null);
            CompoundTag outputNBT = null;
            if(this.nbt != null)
                outputNBT = this.nbt.apply(inputNBT);
            else if(this.copyNBT && inputNBT != null)
                outputNBT = inputNBT;
            return component.getSpaceForItem(this.outputSlot, this.output == Items.AIR ? item : this.output, outputNBT) >= this.outputAmount;
        });
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, ICraftingContext context) {
        for(Item item : this.input.getAll()) {
            if(component.getItemAmount(this.inputSlot, item, this.inputNBT) < this.inputAmount)
                continue;
            CompoundTag inputNBT = component.getComponents().stream().filter(slot -> slot.getItemStack().getItem() == item).findFirst().map(slot -> slot.getItemStack().getTag()).map(CompoundTag::copy).orElse(null);
            CompoundTag outputNBT = null;
            if(this.nbt != null)
                outputNBT = this.nbt.apply(inputNBT);
            else if(this.copyNBT && inputNBT != null)
                outputNBT = inputNBT;
            if(component.getSpaceForItem(this.outputSlot, this.output == Items.AIR ? item : this.output, outputNBT) < this.outputAmount)
                continue;
            component.removeFromInputs(this.inputSlot, item, this.inputAmount, null);
            component.addToOutputs(this.outputSlot, this.output == Items.AIR ? item : this.output, this.outputAmount, outputNBT);
            return CraftingResult.success();
        }
        return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.item_transform.error", this.input.toString(), this.inputAmount));
    }

    @Override
    public List<IJEIIngredientWrapper<ItemStack>> getJEIIngredientWrappers(IMachineRecipe recipe) {
        CompoundTag outputNBT = null;
        if(this.nbt != null)
            outputNBT = this.nbt.apply(this.inputNBT == null ? null : this.inputNBT.copy());
        else if(this.copyNBT && this.inputNBT != null)
            outputNBT = this.inputNBT;
        return Lists.newArrayList(
                new ItemIngredientWrapper(RequirementIOMode.INPUT, this.input, this.inputAmount, getChance(), false, this.inputNBT, this.inputSlot),
                new ItemIngredientWrapper(RequirementIOMode.OUTPUT, this.output == Items.AIR ? this.input : new ItemIngredient(this.output), this.outputAmount, getChance(), false, outputNBT, this.outputSlot)
        );
    }
}
