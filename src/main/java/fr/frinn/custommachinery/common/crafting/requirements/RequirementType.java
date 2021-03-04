package fr.frinn.custommachinery.common.crafting.requirements;

import com.mojang.serialization.Codec;
import fr.frinn.custommachinery.common.init.Registration;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class RequirementType<T extends IRequirement<?>> extends ForgeRegistryEntry<RequirementType<? extends IRequirement<?>>> {

    public static final Codec<RequirementType<?>> CODEC = ResourceLocation.CODEC.xmap(Registration.REQUIREMENT_TYPE_REGISTRY::getValue, RequirementType::getRegistryName);

    private Codec<T> codec;

    public RequirementType(@Nonnull Codec<T> codec) {
        this.codec = codec;
    }

    @Nonnull
    public Codec<T> getCodec() {
        return this.codec;
    }
}
