package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.CraftingResult;
import fr.frinn.custommachinery.common.data.component.ItemMachineComponent;
import fr.frinn.custommachinery.common.data.component.MachineComponentType;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemRequirement extends AbstractRequirement<ItemComponentHandler> {

    public static final ResourceLocation TYPE = new ResourceLocation(CustomMachinery.MODID, "item");

    @SuppressWarnings("deprecation")
    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(itemRequirementInstance ->
            itemRequirementInstance.group(
                    Codec.STRING.fieldOf("mode").forGetter(requirement -> requirement.getMode().toString()),
                    Registry.ITEM.fieldOf("item").forGetter(requirement -> requirement.item),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount)
            ).apply(itemRequirementInstance, (mode, item, amount) -> new ItemRequirement(MODE.value(mode), item, amount))
    );

    private Item item;
    private int amount;

    public ItemRequirement(MODE mode, Item item, int amount) {
        super(mode);
        this.item = item;
        this.amount = amount;
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
            return component.getItemAmount(this.item) >= this.amount;
        } else {
            return component.getSpaceForItem(this.item) >= this.amount;
        }
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component) {
        if(getMode() == MODE.INPUT) {
            int canExtract = component.getItemAmount(this.item);
            if(canExtract >= this.amount) {
                component.removeFromInputs(this.item, this.amount);
                return CraftingResult.success();
            }
            return CraftingResult.error(new StringTextComponent("Not enough " + this.item.getRegistryName() + ", " + canExtract + " found but " + this.amount + " needed !"));
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
            return CraftingResult.error(new TranslationTextComponent("Not enough space to store " + this.amount + " ").append(new TranslationTextComponent(this.item.getTranslationKey())));
        }
        return CraftingResult.pass();
    }
}
