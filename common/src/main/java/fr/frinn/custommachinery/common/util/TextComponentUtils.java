package fr.frinn.custommachinery.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.impl.codec.CodecLogger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public class TextComponentUtils {

    public static final Codec<TextColor> COLOR_CODEC = Codec.STRING.comapFlatMap(encoded -> {
        TextColor color = TextColor.parseColor(encoded);
        if(color != null)
            return DataResult.success(color);
        return DataResult.error("Invalid color: " + encoded);
    }, TextColor::serialize).stable();

    public static final MapCodec<Style> STYLE_CODEC = RecordCodecBuilder.mapCodec(styleInstance ->
            styleInstance.group(
                    CodecLogger.loggedOptional(Codec.BOOL,"bold", false).forGetter(Style::isBold),
                    CodecLogger.loggedOptional(Codec.BOOL,"italic", false).forGetter(Style::isItalic),
                    CodecLogger.loggedOptional(Codec.BOOL,"underlined", false).forGetter(Style::isUnderlined),
                    CodecLogger.loggedOptional(Codec.BOOL,"strikethrough", false).forGetter(Style::isStrikethrough),
                    CodecLogger.loggedOptional(Codec.BOOL,"obfuscated", false).forGetter(Style::isObfuscated),
                    CodecLogger.loggedOptional(COLOR_CODEC,"color").forGetter(style -> Optional.ofNullable(style.getColor())),
                    CodecLogger.loggedOptional(ResourceLocation.CODEC,"font", new ResourceLocation("default")).forGetter(Style::getFont)
            ).apply(styleInstance, (bold, italic, underlined, strikethrough, obfuscated, color, font) ->
                    Style.EMPTY
                    .withBold(bold)
                    .withItalic(italic)
                    .withUnderlined(underlined)
                    .withStrikethrough(strikethrough)
                    .withObfuscated(obfuscated)
                    .withColor(color.orElse(null))
                    .withFont(font)
            )
    );

    public static final Codec<Component> TEXT_COMPONENT_CODEC = RecordCodecBuilder.create(iTextComponentInstance ->
            iTextComponentInstance.group(
                    Codec.STRING.fieldOf("text").forGetter(iTextComponent -> iTextComponent instanceof TranslatableComponent ? ((TranslatableComponent)iTextComponent).getKey() : iTextComponent.getContents()),
                    STYLE_CODEC.forGetter(Component::getStyle),
                    ExtraCodecs.lazyInitializedCodec(TextComponentUtils::getCodec).listOf().optionalFieldOf("childrens", Collections.emptyList()).forGetter(Component::getSiblings)
            ).apply(iTextComponentInstance, (text, style, childrens) -> {
                            TranslatableComponent component = new TranslatableComponent(text);
                            component.setStyle(style);
                            childrens.forEach(component::append);
                            return component;
                    }
            )
    );

    public static final Codec<Component> CODEC = Codec.either(TEXT_COMPONENT_CODEC, Codec.STRING)
            .xmap(either -> either.map(Function.identity(), TranslatableComponent::new), Either::left).stable();

    public static String toJsonString(Component component) {
        DataResult<JsonElement> result = TEXT_COMPONENT_CODEC.encodeStart(JsonOps.INSTANCE, component);
        return result.result().map(JsonElement::toString).orElse("");
    }

    public static Component fromJsonString(String jsonString) {
        JsonElement json = JsonParser.parseString(jsonString);
        return TEXT_COMPONENT_CODEC.decode(JsonOps.INSTANCE, json).result().map(Pair::getFirst).orElse(TextComponent.EMPTY);
    }

    private static Codec<Component> getCodec() {
        return TEXT_COMPONENT_CODEC;
    }
}
