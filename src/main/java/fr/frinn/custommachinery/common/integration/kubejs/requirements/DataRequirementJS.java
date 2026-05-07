package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.data.DoubleDataRequirement;
import fr.frinn.custommachinery.common.requirement.data.IntegerDataRequirement;
import fr.frinn.custommachinery.common.requirement.data.IntegerDataRequirement.Operation;
import fr.frinn.custommachinery.common.requirement.data.NBTDataRequirement;
import fr.frinn.custommachinery.common.requirement.data.StringDataRequirement;
import fr.frinn.custommachinery.common.requirement.data.StringDataRequirement.Comparator;
import fr.frinn.custommachinery.impl.util.DoubleRange;
import fr.frinn.custommachinery.impl.util.IntRange;
import net.minecraft.nbt.CompoundTag;

public interface DataRequirementJS extends RecipeJSBuilder {

    /** INTEGER **/

    default RecipeJSBuilder requireIntData(String id, IntRange range) {
        return this.addRequirement(new IntegerDataRequirement(RequirementIOMode.INPUT, id, range, 0, Operation.SET));
    }

    default RecipeJSBuilder putIntData(String id, int value) {
        return this.addRequirement(new IntegerDataRequirement(RequirementIOMode.OUTPUT, id, IntRange.ALL, value, Operation.SET));
    }

    default RecipeJSBuilder addIntData(String id, int value) {
        return this.addRequirement(new IntegerDataRequirement(RequirementIOMode.OUTPUT, id, IntRange.ALL, value, Operation.ADD));
    }

    default RecipeJSBuilder mulIntData(String id, int value) {
        return this.addRequirement(new IntegerDataRequirement(RequirementIOMode.OUTPUT, id, IntRange.ALL, value, Operation.MUL));
    }

    /** DOUBLE **/

    default RecipeJSBuilder requireDoubleData(String id, DoubleRange range) {
        return this.addRequirement(new DoubleDataRequirement(RequirementIOMode.INPUT, id, range, 0, Operation.SET));
    }

    default RecipeJSBuilder putDoubleData(String id, double value) {
        return this.addRequirement(new DoubleDataRequirement(RequirementIOMode.OUTPUT, id, DoubleRange.ALL, value, Operation.SET));
    }

    default RecipeJSBuilder addDoubleData(String id, double value) {
        return this.addRequirement(new DoubleDataRequirement(RequirementIOMode.OUTPUT, id, DoubleRange.ALL, value, Operation.ADD));
    }

    default RecipeJSBuilder mulDoubleData(String id, double value) {
        return this.addRequirement(new DoubleDataRequirement(RequirementIOMode.OUTPUT, id, DoubleRange.ALL, value, Operation.MUL));
    }

    /** STRING **/

    default RecipeJSBuilder requireExactStringData(String id, String value) {
        return this.addRequirement(new StringDataRequirement(RequirementIOMode.INPUT, id, value, Comparator.EXACT));
    }

    default RecipeJSBuilder requireWeakStringData(String id, String value) {
        return this.addRequirement(new StringDataRequirement(RequirementIOMode.INPUT, id, value, Comparator.WEAK));
    }

    default RecipeJSBuilder putStringData(String id, String value) {
        return this.addRequirement(new StringDataRequirement(RequirementIOMode.OUTPUT, id, value, Comparator.EXACT));
    }

    /** NBT **/

    default RecipeJSBuilder requireExactNBTData(CompoundTag value) {
        return this.requireExactNBTData("", value);
    }

    default RecipeJSBuilder requireWeakNBTData(CompoundTag value) {
        return this.requireWeakNBTData("", value);
    }

    default RecipeJSBuilder requireExactNBTData(String id, CompoundTag value) {
        return this.addRequirement(new NBTDataRequirement(RequirementIOMode.INPUT, id, value, Comparator.EXACT));
    }

    default RecipeJSBuilder requireWeakNBTData(String id, CompoundTag value) {
        return this.addRequirement(new NBTDataRequirement(RequirementIOMode.INPUT, id, value, Comparator.WEAK));
    }

    default RecipeJSBuilder putNBTData(CompoundTag value) {
        return this.putNBTData("", value);
    }

    default RecipeJSBuilder putNBTData(String id, CompoundTag value) {
        return this.addRequirement(new NBTDataRequirement(RequirementIOMode.OUTPUT, id, value, Comparator.EXACT));
    }
}
