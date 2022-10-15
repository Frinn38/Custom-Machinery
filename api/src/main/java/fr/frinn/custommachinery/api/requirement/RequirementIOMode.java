package fr.frinn.custommachinery.api.requirement;

import fr.frinn.custommachinery.api.ICustomMachineryAPI;

import java.util.Locale;

/**
 * Represents the mode, INPUT or OUTPUT of an IRequirement.
 * Each IRequirement have a mode which can be fixed or chosen by the recipe maker.
 */
public enum RequirementIOMode {
    /**
     * For requirements that requires or consume something, usually at the start of the process.
     */
    INPUT,

    /**
     * For requirements that produce something, usually at the end of the process.
     */
    OUTPUT;

    public static RequirementIOMode value(String mode) {
        return valueOf(mode.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }

    public String getTranslationKey() {
        return ICustomMachineryAPI.INSTANCE.modid() + ".requirement.mode." + this;
    }
}
