package fr.frinn.custommachinery.api.integration.jei;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo.TooltipPredicate;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.util.TextComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DisplayInfoTemplate {

    private static final ResourceLocation DEFAULT_TEXTURE = ICustomMachineryAPI.INSTANCE.rl("textures/gui/create_icon.png");

    public static final NamedCodec<Pair<Component, TooltipPredicate>> TOOLTIPS = NamedCodec.either(NamedCodec.pair(TextComponentUtils.CODEC.fieldOf("text"), TooltipPredicate.CODEC.fieldOf("predicate")), TextComponentUtils.CODEC)
            .xmap(either -> either.map(Function.identity(), component -> Pair.of(component, TooltipPredicate.ALWAYS)), Either::left, "Tooltips");

    public static final NamedCodec<DisplayInfoTemplate> CODEC = NamedCodec.record(displayInfoTemplateInstance ->
            displayInfoTemplateInstance.group(
                    TOOLTIPS.listOf().optionalFieldOf("tooltips", Collections.emptyList()).forGetter(template -> template.tooltips),
                    DefaultCodecs.ITEM_OR_STACK.optionalFieldOf("item").forGetter(template -> Optional.ofNullable(template.stack)),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("icon", DEFAULT_TEXTURE).forGetter(template -> template.icon),
                    NamedCodec.intRange(1, 128).optionalFieldOf("width", 10).forGetter(template -> template.width),
                    NamedCodec.intRange(1, 128).optionalFieldOf("height", 10).forGetter(template -> template.height),
                    NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("u", 0).forGetter(template -> template.u),
                    NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("v", 0).forGetter(template -> template.v),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("atlas").forGetter(template -> Optional.ofNullable(template.atlas)),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("sprite").forGetter(template -> Optional.ofNullable(template.sprite))
            ).apply(displayInfoTemplateInstance, (tooltips, stack, icon, width, height, u, v, atlas, sprite) -> {
                        DisplayInfoTemplate template = new DisplayInfoTemplate();
                        tooltips.forEach(template::tooltip);
                        stack.ifPresent(template::item);
                        template.texture(icon, width, height, u, v);
                        atlas.ifPresent(a -> sprite.ifPresent(s -> template.sprite(a, s)));
                        return template;
                    }
            ), "Display info template"
    );

    private final List<Pair<Component, TooltipPredicate>> tooltips = new ArrayList<>();
    @Nullable
    private ItemStack stack;
    private ResourceLocation icon = DEFAULT_TEXTURE;
    private int width = 10;
    private int height = 10;
    private int u = 0;
    private int v = 0;
    @Nullable
    private ResourceLocation atlas;
    @Nullable
    private ResourceLocation sprite;

    private DisplayInfoTemplate tooltip(Pair<Component, TooltipPredicate> pair) {
        this.tooltips.add(pair);
        return this;
    }

    public DisplayInfoTemplate tooltip(Component component, TooltipPredicate predicate) {
        return this.tooltip(Pair.of(component, predicate));
    }

    public DisplayInfoTemplate tooltip(Component component) {
        return this.tooltip(component, TooltipPredicate.ALWAYS);
    }

    public DisplayInfoTemplate item(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public DisplayInfoTemplate texture(ResourceLocation icon, int width, int height, int u, int v) {
        this.icon = icon;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        return this;
    }

    public DisplayInfoTemplate sprite(ResourceLocation atlas, ResourceLocation sprite) {
        this.atlas = atlas;
        this.sprite = sprite;
        return this;
    }

    public List<Pair<Component, TooltipPredicate>> getTooltips() {
        return this.tooltips;
    }

    public void build(IDisplayInfo info) {
        this.tooltips.forEach(pair -> info.addTooltip(pair.getFirst(), pair.getSecond()));
        if(this.stack != null)
            info.setItemIcon(this.stack);
        else if(this.atlas != null && this.sprite != null)
            info.setSpriteIcon(this.atlas, this.sprite);
        else
            info.setTextureIcon(this.icon, this.width, this.height, this.u, this.v);
    }
}
