package fr.frinn.custommachinery.common.integration.crafttweaker.requirements;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import fr.frinn.custommachinery.common.integration.crafttweaker.CTConstants;
import fr.frinn.custommachinery.api.integration.crafttweaker.RecipeCTBuilder;
import fr.frinn.custommachinery.common.requirement.LootTableRequirement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.OptionalFloat;

@ZenRegister
@Name(CTConstants.REQUIREMENT_LOOT_TABLE)
public interface LootTableRequirementCT<T> extends RecipeCTBuilder<T> {

    @Method
    default T lootTableOutput(String lootTable, @OptionalFloat float luck) {
        if(!Utils.isResourceNameValid(lootTable))
            return error("Invalid loot table id: {}", lootTable);

        ResourceLocation tableLoc = new ResourceLocation(lootTable);
        return addRequirement(new LootTableRequirement(tableLoc, luck));
    }
}
