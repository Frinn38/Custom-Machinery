package fr.frinn.custommachinery.impl.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.impl.codec.DefaultCodecs;
import fr.frinn.custommachinery.impl.codec.NamedMapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.InsertingContents;

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
                    DefaultCodecs.RESOURCE_LOCATION.optionalFieldOf("font", ResourceLocation.withDefaultNamespace("default")).forGetter(Style::getFont)
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
                    NamedCodec.lazy(TextComponentUtils::getCodec, "Text component").listOf().optionalFieldOf("childrens", Collections.emptyList()).forGetter(Component::getSiblings)
            ).apply(iTextComponentInstance, (text, style, childrens) -> {
                            MutableComponent component = text.map(Component::translatable, MutableComponent::create);
                            component.setStyle(style);
                            childrens.forEach(component::append);
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
        if(contents instanceof LiteralContents literal)
            return literal.text();
        else if(contents instanceof TranslatableContents translatable)
            return translatable.getKey();
        return component.getString();
    }

    @SuppressWarnings("unchecked")
    private static NamedCodec<ComponentContents> getComponentContentsCodec() {
        ComponentContents.Type<?>[] type = new ComponentContents.Type[]{PlainTextContents.TYPE, TranslatableContents.TYPE, KeybindContents.TYPE, ScoreContents.TYPE, SelectorContents.TYPE, NbtContents.TYPE, InsertingContents.TYPE};
        return NamedCodec.of(((MapCodec<ComponentContents>)ComponentSerialization.createLegacyComponentMatcher(type, ComponentContents.Type::codec, ComponentContents::type, "type")).codec(), "Component contents");
    }
}
