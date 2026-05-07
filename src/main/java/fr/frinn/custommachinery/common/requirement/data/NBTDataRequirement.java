package fr.frinn.custommachinery.common.requirement.data;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IRequirementList;
import fr.frinn.custommachinery.api.requirement.IRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.component.DataMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.requirement.data.StringDataRequirement.Comparator;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record NBTDataRequirement(RequirementIOMode mode, String id, CompoundTag value, Comparator comparator) implements IRequirement<DataMachineComponent> {

    public static final NamedCodec<NBTDataRequirement> CODEC = NamedCodec.record(integerDataRequirementInstance ->
            integerDataRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(NBTDataRequirement::mode),
                    NamedCodec.STRING.fieldOf("id").forGetter(NBTDataRequirement::id),
                    DefaultCodecs.NBT.fieldOf("value").forGetter(NBTDataRequirement::value),
                    NamedCodec.enumCodec(Comparator.class).optionalFieldOf("comparator", Comparator.EXACT).forGetter(NBTDataRequirement::comparator)
            ).apply(integerDataRequirementInstance, NBTDataRequirement::new), "Integer data requirement"
    );

    @Override
    public RequirementType<NBTDataRequirement> getType() {
        return Registration.NBT_DATA_REQUIREMENT.get();
    }

    @Override
    public MachineComponentType<DataMachineComponent> getComponentType() {
        return Registration.DATA_MACHINE_COMPONENT.get();
    }

    @Override
    public RequirementIOMode getMode() {
        return this.mode;
    }

    @Override
    public boolean test(DataMachineComponent component, ICraftingContext context) {
        if(this.mode == RequirementIOMode.OUTPUT)
            return true;

        if(getTag(this.id, component.getData()) instanceof CompoundTag compoundTag)
            return this.comparator == Comparator.EXACT ? this.value.equals(compoundTag) : this.containsAll(compoundTag, this.value);

        return false;
    }

    @Override
    public void gatherRequirements(IRequirementList<DataMachineComponent> list) {
        if(this.mode == RequirementIOMode.INPUT)
            list.worldCondition((component, context) -> {
                Tag tag = getTag(this.id, component.getData());
                if(tag instanceof CompoundTag compoundTag) {
                    if(this.comparator == Comparator.EXACT ? this.value.equals(compoundTag) : this.containsAll(compoundTag, this.value))
                        return CraftingResult.success();
                }
                return CraftingResult.error(Component.translatable("custommachinery.requirements.data.error", this.id, this.value, tag == null ? "not found" : tag.getAsString()));
            });
        else
            list.processOnEnd((component, context) -> {
                String[] path = this.id.split("/");
                if(this.id.isEmpty())
                    component.setData(this.value);
                else if(path.length == 1)
                    component.getData().put(this.id, this.value);
                else {
                    CompoundTag tag = component.getData();
                    for(int i = 0; i < path.length - 1; i++) {
                        if(tag.contains(path[i], Tag.TAG_COMPOUND))
                            tag = tag.getCompound(path[i]);
                        else {
                            CompoundTag newTag = new CompoundTag();
                            tag.put(path[i], newTag);
                            tag = newTag;
                        }
                    }
                    String tagId = path[path.length - 1];
                    tag.put(tagId, this.value);
                }
                return CraftingResult.success();
            });
    }

    @Nullable
    private Tag getTag(String id, CompoundTag base) {
        if(id.isEmpty())
            return base;

        String[] path = id.split("/");
        if(path.length == 1)
            return base.get(id);

        if(base.get(path[0]) instanceof CompoundTag compoundTag)
            return getTag(id.substring(path[0].length() + 1), compoundTag);

        return null;
    }

    //Check if tag1 contains at least all values from tag2
    private boolean containsAll(CompoundTag tag1, CompoundTag tag2) {
        for(String key : tag2.getAllKeys()) {
            if(!tag1.contains(key))
                return false;
            Tag tag = tag1.get(key);
            if(tag == null)
                return false;
            if(tag1.getTagType(key) != tag2.getTagType(key))
                return false;
            if(!tag.equals(tag2.get(key)))
                return false;
        }
        return true;
    }
}
