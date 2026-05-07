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
import fr.frinn.custommachinery.impl.util.IntRange;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record IntegerDataRequirement(RequirementIOMode mode, String id, IntRange range, int value, Operation operation) implements IRequirement<DataMachineComponent> {

    public static final NamedCodec<IntegerDataRequirement> CODEC = NamedCodec.record(integerDataRequirementInstance ->
            integerDataRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(IntegerDataRequirement::mode),
                    NamedCodec.STRING.fieldOf("id").forGetter(IntegerDataRequirement::id),
                    IntRange.CODEC.optionalFieldOf("range", IntRange.ALL).forGetter(IntegerDataRequirement::range),
                    NamedCodec.INT.optionalFieldOf("value", 0).forGetter(IntegerDataRequirement::value),
                    NamedCodec.enumCodec(Operation.class).optionalFieldOf("operation", Operation.SET).forGetter(IntegerDataRequirement::operation)
            ).apply(integerDataRequirementInstance, IntegerDataRequirement::new), "Integer data requirement"
    );

    @Override
    public RequirementType<IntegerDataRequirement> getType() {
        return Registration.INTEGER_DATA_REQUIREMENT.get();
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

        if(getTag(this.id, component.getData()) instanceof NumericTag numericTag)
            return this.range.contains(numericTag.getAsInt());

        return false;
    }

    @Override
    public void gatherRequirements(IRequirementList<DataMachineComponent> list) {
        if(this.mode == RequirementIOMode.INPUT)
            list.worldCondition((component, context) -> {
                Tag tag = getTag(this.id, component.getData());
                if(tag instanceof NumericTag numericTag) {
                    if(this.range.contains(numericTag.getAsInt()))
                        return CraftingResult.success();
                }
                return CraftingResult.error(Component.translatable("custommachinery.requirements.data.error", this.id, this.range.toString(), tag == null ? "not found" : tag.getAsString()));
            });
        else
            list.processOnEnd((component, context) -> {
                String[] path = this.id.split("/");
                if(path.length == 1) {
                    if(this.operation == Operation.SET || component.getData().getInt(this.id) == 0)
                        component.getData().putInt(this.id, this.value);
                    else if(this.operation == Operation.ADD)
                        component.getData().putInt(this.id, component.getData().getInt(this.id) + this.value);
                    else if(this.operation == Operation.MUL)
                        component.getData().putInt(this.id, component.getData().getInt(this.id) * this.value);
                }
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
                    if(this.operation == Operation.SET || tag.getInt(tagId) == 0)
                        tag.putInt(tagId, this.value);
                    else if(this.operation == Operation.ADD)
                        tag.putInt(tagId, tag.getInt(tagId) + this.value);
                    else if(this.operation == Operation.MUL)
                        tag.putInt(tagId, tag.getInt(tagId) * this.value);
                }
                return CraftingResult.success();
            });
    }

    @Nullable
    private Tag getTag(String id, CompoundTag base) {
        String[] path = id.split("/");
        if(path.length == 1)
            return base.get(id);

        if(base.get(path[0]) instanceof CompoundTag compoundTag)
            return getTag(id.substring(path[0].length() + 1), compoundTag);

        return null;
    }

    public enum Operation {
        SET,
        ADD,
        MUL
    }
}
