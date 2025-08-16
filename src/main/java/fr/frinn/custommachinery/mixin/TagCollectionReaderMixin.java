package fr.frinn.custommachinery.mixin;

import fr.frinn.custommachinery.common.util.TagIndex;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.TagCollectionReader;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(value = TagCollectionReader.class)
public abstract class TagCollectionReaderMixin<T> {

    @Shadow
    @Final
    private String tagType;

    @Redirect(method = "buildTagCollectionFromMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/ITagCollection;getTagCollectionFromMap(Ljava/util/Map;)Lnet/minecraft/tags/ITagCollection;"))
    private ITagCollection<T> fillTagIndex(Map<ResourceLocation, ITag<T>> finalTagMap) {
        TagIndex.setNewTagIndex(finalTagMap, tagType);
        return ITagCollection.getTagCollectionFromMap(finalTagMap);
    }

}
