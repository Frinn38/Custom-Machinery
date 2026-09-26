package fr.frinn.custommachinery.impl.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.NamedMapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.ExtraCodecs;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public class TextComponentUtils {

    public static final NamedCodec<TextColor> COLOR_CODEC = NamedCodec.STRING.comapFlatMap(TextColor::parseColor, TextColor::serialize, "Text color");

    public static final NamedMapCodec<Style> STYLE_CODEC = NamedCodec.record(styleInstance ->
            styleInstance.group(
                    NamedCodec.BOOL.optionalFieldOf("bold", false).forGetter(Style::isBold),
                    NamedCodec.BOOL.optionalFieldOf("italic", false).forGetter(Style::isItalic),
                    NamedCodec.BOOL.optionalFieldOf("underlined", false).forGetter(Style::isUnderlined),
                    NamedCodec.BOOL.optionalFieldOf("strikethrough", false).forGetter(Style::isStrikethrough),
                    NamedCodec.BOOL.optionalFieldOf("obfuscated", false).forGetter(Style::isObfuscated),
                    COLOR_CODEC.optionalFieldOf("color").forGetter(style -> Optional.ofNullable(style.getColor())),
                    NamedCodec.of(FontDescription.CODEC).optionalFieldOf("font", FontDescription.DEFAULT).forGetter(Style::getFont)
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
                    NamedCodec.either(NamedCodec.STRING, getComponentContentsCodec()).fieldOf("text").forGetter(component -> Either.right(component.getContents())),
                    STYLE_CODEC.forGetter(Component::getStyle),
                    NamedCodec.lazy(TextComponentUtils::getCodec, "Text component").listOf().optionalFieldOf("children", Collections.emptyList()).forGetter(Component::getSiblings)
            ).apply(iTextComponentInstance, (text, style, children) -> {
                            MutableComponent component = text.map(Component::translatable, MutableComponent::create);
                            component.setStyle(style);
                            children.forEach(component::append);
                            return component;
                    }
            ),
            "Text component"
    );

    public static final NamedCodec<Component> CODEC = NamedCodec.either(TEXT_COMPONENT_CODEC, NamedCodec.STRING)
            .xmap(either -> either.map(Function.identity(), Component::translatable), Either::left, "Text component");

    private static NamedCodec<Component> getCodec() {
        return TEXT_COMPONENT_CODEC;
    }

    public static String getString(Component component) {
        ComponentContents contents = component.getContents();
        if(contents instanceof LiteralContents(String text))
            return text;
        else if(contents instanceof TranslatableContents translatable)
            return translatable.getKey();
        return component.getString();
    }

    private static NamedCodec<ComponentContents> getComponentContentsCodec() {
        ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends ComponentContents>> contentTypes = new ExtraCodecs.LateBoundIdMapper<>();
        contentTypes.put("text", PlainTextContents.MAP_CODEC);
        contentTypes.put("translatable", TranslatableContents.MAP_CODEC);
        contentTypes.put("keybind", KeybindContents.MAP_CODEC);
        contentTypes.put("score", ScoreContents.MAP_CODEC);
        contentTypes.put("selector", SelectorContents.MAP_CODEC);
        contentTypes.put("nbt", NbtContents.MAP_CODEC);
        contentTypes.put("object", ObjectContents.MAP_CODEC);
        return NamedCodec.of(ComponentSerialization.createLegacyComponentMatcher(contentTypes, ComponentContents::codec, "type").codec(), "Component contents");
    }

    public static String toJSON(Component component) {
        return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, component).result().map(json -> new Gson().toJson(json)).orElse("");
    }

    public static Component fromJSON(String json) {
        return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, new Gson().fromJson(json, JsonElement.class)).result().orElse(Component.empty());
    }
}
