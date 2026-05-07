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
import fr.frinn.custommachinery.common.requirement.data.IntegerDataRequirement.Operation;
import fr.frinn.custommachinery.impl.util.DoubleRange;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record DoubleDataRequirement(RequirementIOMode mode, String id, DoubleRange range, double value, Operation operation) implements IRequirement<DataMachineComponent> {

    public static final NamedCodec<DoubleDataRequirement> CODEC = NamedCodec.record(doubleDataRequirementInstance ->
            doubleDataRequirementInstance.group(
                    RequirementIOMode.CODEC.fieldOf("mode").forGetter(DoubleDataRequirement::mode),
                    NamedCodec.STRING.fieldOf("id").forGetter(DoubleDataRequirement::id),
                    DoubleRange.CODEC.optionalFieldOf("range", DoubleRange.ALL).forGetter(DoubleDataRequirement::range),
                    NamedCodec.DOUBLE.optionalFieldOf("value", 0d).forGetter(DoubleDataRequirement::value),
                    NamedCodec.enumCodec(Operation.class).optionalFieldOf("operation", Operation.SET).forGetter(DoubleDataRequirement::operation)
            ).apply(doubleDataRequirementInstance, DoubleDataRequirement::new), "Double data requirement"
    );

    @Override
    public RequirementType<DoubleDataRequirement> getType() {
        return Registration.DOUBLE_DATA_REQUIREMENT.get();
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
            return this.range.contains(numericTag.getAsDouble());

        return false;
    }

    @Override
    public void gatherRequirements(IRequirementList<DataMachineComponent> list) {
        if(this.mode == RequirementIOMode.INPUT)
            list.worldCondition((component, context) -> {
                Tag tag = getTag(this.id, component.getData());
                if(tag instanceof NumericTag numericTag) {
                    if(this.range.contains(numericTag.getAsDouble()))
                        return CraftingResult.success();
                }
                return CraftingResult.error(Component.translatable("custommachinery.requirements.data.error", this.id, this.range.toString(), tag == null ? "not found" : tag.getAsString()));
            });
        else
            list.processOnEnd((component, context) -> {
                String[] path = this.id.split("/");
                if(path.length == 1) {
                    if(this.operation == Operation.SET || component.getData().getDouble(this.id) == 0)
                        component.getData().putDouble(this.id, this.value);
                    else if(this.operation == Operation.ADD)
                        component.getData().putDouble(this.id, component.getData().getDouble(this.id) + this.value);
                    else if(this.operation == Operation.MUL)
                        component.getData().putDouble(this.id, component.getData().getDouble(this.id) * this.value);
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
                    if(this.operation == Operation.SET || tag.getDouble(tagId) == 0)
                        tag.putDouble(tagId, this.value);
                    else if(this.operation == Operation.ADD)
                        tag.putDouble(tagId, tag.getDouble(tagId) + this.value);
                    else if(this.operation == Operation.MUL)
                        tag.putDouble(tagId, tag.getDouble(tagId) * this.value);
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
}
