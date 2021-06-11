package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class RequirementType<T extends IRequirement<?>> extends ForgeRegistryEntry<RequirementType<? extends IRequirement<?>>> {

    private Codec<T> codec;

    public RequirementType(@Nonnull Codec<T> codec) {
        this.codec = codec;
    }

    @Nonnull
    public Codec<T> getCodec() {
        return this.codec;
    }

    public String getTranslationKey() {
        if(getRegistryName() == null)
            return "unknown";
        return "requirement." + getRegistryName().getNamespace() + "." + getRegistryName().getPath();
    }
}
