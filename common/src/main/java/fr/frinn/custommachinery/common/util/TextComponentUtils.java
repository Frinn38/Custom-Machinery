package fr.frinn.custommachinery.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.codec.NamedMapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public class TextComponentUtils {

    public static final NamedCodec<TextColor> COLOR_CODEC = NamedCodec.STRING.comapFlatMap(encoded -> {
        TextColor color = TextColor.parseColor(encoded);
        if(color != null)
            return DataResult.success(color);
        return DataResult.error("Invalid color: " + encoded);
    }, TextColor::serialize, "Text color");

    public static final NamedMapCodec<Style> STYLE_CODEC = NamedCodec.record(styleInstance ->
            styleInstance.group(
                    NamedCodec.BOOL.optionalFieldOf("bold", false).forGetter(Style::isBold),
                    NamedCodec.BOOL.optionalFieldOf("italic", false).forGetter(Style::isItalic),
                    NamedCodec.BOOL.optionalFieldOf("underlined", false).forGetter(Style::isUnderlined),
                    NamedCodec.BOOL.optionalFieldOf("strikethrough", false).forGetter(Style::isStrikethrough),
                    NamedCodec.BOOL.optionalFieldOf("obfuscated", false).forGetter(Style::isObfuscated),
                    COLOR_CODEC.optionalFieldOf("color").forGetter(style -> Optional.ofNullable(style.getColor())),
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("font", new ResourceLocation("default")).forGetter(Style::getFont)
            ).apply(styleInstance, (bold, italic, underlined, strikethrough, obfuscated, color, font) ->
                    Style.EMPTY
                    .withBold(bold)
                    .withItalic(italic)
                    .withUnderlined(underlined)
                    .withStrikethrough(strikethrough)
                    .withObfuscated(obfuscated)
                    .withColor(color.orElse(null))
                    .withFont(font)
            ),
            "Style"
    );

    public static final NamedCodec<Component> TEXT_COMPONENT_CODEC = NamedCodec.record(iTextComponentInstance ->
            iTextComponentInstance.group(
                    NamedCodec.STRING.fieldOf("text").forGetter(Component::getString),
                    STYLE_CODEC.forGetter(Component::getStyle),
                    NamedCodec.lazy(TextComponentUtils::getCodec, "Text component").listOf().optionalFieldOf("childrens", Collections.emptyList()).forGetter(Component::getSiblings)
            ).apply(iTextComponentInstance, (text, style, childrens) -> {
                            MutableComponent component = Component.translatable(text);
                            component.setStyle(style);
                            childrens.forEach(component::append);
                            return component;
                    }
            ),
            "Text component"
    );

    public static final NamedCodec<Component> CODEC = NamedCodec.either(TEXT_COMPONENT_CODEC, NamedCodec.STRING)
            .xmap(either -> either.map(Function.identity(), Component::translatable), Either::left, "Text component");

    public static String toJsonString(Component component) {
        DataResult<JsonElement> result = TEXT_COMPONENT_CODEC.encodeStart(JsonOps.INSTANCE, component);
        return result.result().map(JsonElement::toString).orElse("");
    }

    public static Component fromJsonString(String jsonString) {
        JsonElement json = JsonParser.parseString(jsonString);
        return TEXT_COMPONENT_CODEC.decode(JsonOps.INSTANCE, json).result().map(Pair::getFirst).orElse(Component.empty());
    }

    private static NamedCodec<Component> getCodec() {
        return TEXT_COMPONENT_CODEC;
    }
}
