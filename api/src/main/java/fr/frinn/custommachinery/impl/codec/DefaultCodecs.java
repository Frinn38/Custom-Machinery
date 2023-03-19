package fr.frinn.custommachinery.impl.codec;

import com.mojang.serialization.DataResult;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class DefaultCodecs {

    public static final NamedCodec<ResourceLocation> RESOURCE_LOCATION = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeResourceLocation, ResourceLocation::toString, "Resource location");
    public static final NamedCodec<Character> CHARACTER = NamedCodec.STRING.comapFlatMap(DefaultCodecs::decodeCharacter, Object::toString, "Character");
    public static final NamedCodec<CompoundTag> COMPOUND_TAG = NamedCodec.of(CompoundTag.CODEC, "Compound nbt");

    public static final NamedCodec<SoundEvent> SOUND_EVENT = RESOURCE_LOCATION.xmap(SoundEvent::new, SoundEvent::getLocation, "Sound event");

    public static final NamedCodec<Direction> DIRECTION = NamedCodec.enumCodec(Direction.class);

    public static final NamedCodec<ItemStack> ITEM_STACK = NamedCodec.record(itemStackInstance ->
            itemStackInstance.group(
                    RegistrarCodec.ITEM.fieldOf("id").forGetter(ItemStack::getItem),
                    NamedCodec.INT.optionalFieldOf("Count", 1).forGetter(ItemStack::getCount),
                    COMPOUND_TAG.optionalFieldOf("tag").forGetter(stack -> Optional.ofNullable(stack.getTag()))
            ).apply(itemStackInstance, (item, count, nbt) -> {
                ItemStack stack = item.getDefaultInstance();
                stack.setCount(count);
                nbt.ifPresent(stack::setTag);
                return stack;
            }), "Item stack"
    );

    public static <T> NamedCodec<TagKey<T>> tagKey(ResourceKey<Registry<T>> registry) {
        return RESOURCE_LOCATION.xmap(rl -> TagKey.create(registry, rl), TagKey::location, "Tag: " + registry.location());
    }

    private static DataResult<ResourceLocation> decodeResourceLocation(String encoded) {
        try {
            return DataResult.success(new ResourceLocation(encoded));
        } catch (ResourceLocationException e) {
            return DataResult.error(e.getMessage());
        }
    }

    private static DataResult<Character> decodeCharacter(String encoded) {
        if(encoded.length() != 1)
            return DataResult.error("Invalid character : \"" + encoded + "\" must be a single character !");
        return DataResult.success(encoded.charAt(0));
    }
}
