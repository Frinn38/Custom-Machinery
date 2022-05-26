package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.BracketDumper;
import com.blamejared.crafttweaker.api.annotation.BracketResolver;
import com.blamejared.crafttweaker.api.annotation.BracketValidator;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.bracket.CommandStringDisplayable;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Name;

import java.util.Collection;

@ZenRegister
@Name("mods.custommachinery.RequirementTypeBracket")
public class RequirementTypeCTBrackets {

    @BracketResolver("requirementtype")
    public static CTRequirementType parseBracket(String bracket) {
        return new CTRequirementType(Registration.REQUIREMENT_TYPE_REGISTRY.get().getValue(new ResourceLocation(bracket)));
    }

    @BracketValidator("requirementtype")
    public static boolean validateBracket(String bracket) {
        ResourceLocation requirementTypeLocation;
        try {
            requirementTypeLocation = new ResourceLocation(bracket);
        } catch (ResourceLocationException e) {
            throw new IllegalArgumentException("Invalid Requirement Type bracket: " + bracket, e);
        }
        if(!Registration.REQUIREMENT_TYPE_REGISTRY.get().containsKey(requirementTypeLocation))
            throw new IllegalArgumentException("Unknown Requirement type: " + requirementTypeLocation);
        return true;
    }

    @BracketDumper("requirementtype")
    public static Collection<String> dumpBrackets() {
        return Registration.REQUIREMENT_TYPE_REGISTRY.get().getValues().stream().map(type -> "<requirementtype:" + type.getRegistryName() + ">").toList();
    }

    public static class CTRequirementType implements CommandStringDisplayable {

        private final RequirementType<?> type;

        public CTRequirementType(RequirementType<?> type) {
            this.type = type;
        }

        public RequirementType<?> getType() {
            return this.type;
        }

        @Override
        public String getCommandString() {
            return "<requirementtype:" + type.getRegistryName() + ">";
        }
    }
}
