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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Function;

public class TextComponentUtils {

    public static final Codec<Color> COLOR_CODEC = Codec.STRING.comapFlatMap(encoded -> {
        Color color = Color.fromHex(encoded);
        if(color != null)
            return DataResult.success(color);
        return DataResult.error("Invalid color: " + encoded);
    }, Color::getName).stable();

    public static final MapCodec<Style> STYLE_CODEC = RecordCodecBuilder.mapCodec(styleInstance ->
            styleInstance.group(
                    CodecLogger.loggedOptional(Codec.BOOL,"bold", false).forGetter(Style::getBold),
                    CodecLogger.loggedOptional(Codec.BOOL,"italic", false).forGetter(Style::getItalic),
                    CodecLogger.loggedOptional(Codec.BOOL,"underlined", false).forGetter(Style::getUnderlined),
                    CodecLogger.loggedOptional(Codec.BOOL,"strikethrough", false).forGetter(Style::getStrikethrough),
                    CodecLogger.loggedOptional(Codec.BOOL,"obfuscated", false).forGetter(Style::getObfuscated),
                    CodecLogger.loggedOptional(COLOR_CODEC,"color", Color.fromTextFormatting(TextFormatting.WHITE)).forGetter(style -> style.getColor() == null ? Color.fromTextFormatting(TextFormatting.WHITE) : style.getColor()),
                    CodecLogger.loggedOptional(ResourceLocation.CODEC,"font", new ResourceLocation("default")).forGetter(Style::getFontId)
            ).apply(styleInstance, (bold, italic, underlined, strikethrough, obfuscated, color, font) ->
                    Style.EMPTY
                    .setBold(bold)
                    .setItalic(italic)
                    .setUnderlined(underlined)
                    .setStrikethrough(strikethrough)
                    .setObfuscated(obfuscated)
                    .setColor(color)
                    .setFontId(font)
            )
    );

    public static final Codec<ITextComponent> TEXT_COMPONENT_CODEC = RecordCodecBuilder.create(iTextComponentInstance ->
            iTextComponentInstance.group(
                    Codec.STRING.fieldOf("text").forGetter(iTextComponent -> iTextComponent instanceof TranslationTextComponent ? ((TranslationTextComponent)iTextComponent).getKey() : iTextComponent.getUnformattedComponentText()),
                    STYLE_CODEC.forGetter(ITextComponent::getStyle)
            ).apply(iTextComponentInstance, (text, style) -> {
                            TranslationTextComponent component = new TranslationTextComponent(text);
                            component.setStyle(style);
                            return component;
                    }
            )
    );

    public static final Codec<ITextComponent> CODEC = Codec.either(TEXT_COMPONENT_CODEC, Codec.STRING)
            .xmap(either -> either.map(Function.identity(), TranslationTextComponent::new), Either::left).stable();

    public static String toJsonString(ITextComponent component) {
        DataResult<JsonElement> result = TEXT_COMPONENT_CODEC.encodeStart(JsonOps.INSTANCE, component);
        return result.result().map(JsonElement::toString).orElse("");
    }

    public static ITextComponent fromJsonString(String jsonString) {
        JsonElement json = new JsonParser().parse(jsonString);
        return TEXT_COMPONENT_CODEC.decode(JsonOps.INSTANCE, json).result().map(Pair::getFirst).orElse(StringTextComponent.EMPTY);
    }
}
