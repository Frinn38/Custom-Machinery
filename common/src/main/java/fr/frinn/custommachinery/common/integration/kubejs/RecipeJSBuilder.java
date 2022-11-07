package fr.frinn.custommachinery.common.integration.kubejs;

import fr.frinn.custommachinery.api.requirement.IRequirement;

public interface RecipeJSBuilder {

    RecipeJSBuilder addRequirement(IRequirement<?> requirement);

    RecipeJSBuilder error(String error, Object... args);
}
