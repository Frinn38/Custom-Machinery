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
import fr.frinn.custommachinery.api.codec.CodecLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

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
                    CodecLogger.loggedOptional(COLOR_CODEC,"color", TextColor.fromLegacyFormat(ChatFormatting.WHITE)).forGetter(style -> style.getColor() == null ? TextColor.fromLegacyFormat(ChatFormatting.WHITE) : style.getColor()),
                    CodecLogger.loggedOptional(ResourceLocation.CODEC,"font", new ResourceLocation("default")).forGetter(Style::getFont)
            ).apply(styleInstance, (bold, italic, underlined, strikethrough, obfuscated, color, font) ->
                    Style.EMPTY
                    .withBold(bold)
                    .withItalic(italic)
                    .setUnderlined(underlined)
                    .setStrikethrough(strikethrough)
                    .setObfuscated(obfuscated)
                    .withColor(color)
                    .withFont(font)
            )
    );

    public static final Codec<Component> TEXT_COMPONENT_CODEC = RecordCodecBuilder.create(iTextComponentInstance ->
            iTextComponentInstance.group(
                    Codec.STRING.fieldOf("text").forGetter(iTextComponent -> iTextComponent instanceof TranslatableComponent ? ((TranslatableComponent)iTextComponent).getKey() : iTextComponent.getContents()),
                    STYLE_CODEC.forGetter(Component::getStyle)
            ).apply(iTextComponentInstance, (text, style) -> {
                            TranslatableComponent component = new TranslatableComponent(text);
                            component.setStyle(style);
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
        JsonElement json = new JsonParser().parse(jsonString);
        return TEXT_COMPONENT_CODEC.decode(JsonOps.INSTANCE, json).result().map(Pair::getFirst).orElse(TextComponent.EMPTY);
    }
}
