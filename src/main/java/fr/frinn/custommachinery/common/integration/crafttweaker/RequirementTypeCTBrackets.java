package fr.frinn.custommachinery.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.BracketDumper;
import com.blamejared.crafttweaker.api.annotations.BracketResolver;
import com.blamejared.crafttweaker.api.annotations.BracketValidator;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.brackets.CommandStringDisplayable;
import fr.frinn.custommachinery.common.crafting.requirements.RequirementType;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import org.openzen.zencode.java.ZenCodeType;

import java.awt.geom.RectangularShape;
import java.util.Collection;
import java.util.stream.Collectors;

@ZenRegister
@ZenCodeType.Name("mods.custommachinery.RequirementTypeBracket")
public class RequirementTypeCTBrackets {

    @BracketResolver("requirementtype")
    public static CTRequirementType parseBracket(String bracket) {
        RequirementType<?> type = Registration.REQUIREMENT_TYPE_REGISTRY.get().getValue(new ResourceLocation(bracket));
        return new CTRequirementType(type);
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
        return Registration.REQUIREMENT_TYPE_REGISTRY.get().getValues().stream().map(type -> "<requirementtype:" + type.getRegistryName() + ">").collect(Collectors.toList());
    }

    public static class CTRequirementType implements CommandStringDisplayable {

        private RequirementType<?> type;

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
