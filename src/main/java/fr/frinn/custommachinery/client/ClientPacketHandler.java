package fr.frinn.custommachinery.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class ClientPacketHandler {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void handleStructureCreatorPacket(JsonElement keysJson, JsonElement patternJson) {
        PlayerEntity player = Minecraft.getInstance().player;
        if(player == null)
            return;
        JsonObject both = new JsonObject();
        both.add("keys", keysJson);
        both.add("pattern", patternJson);
        String ctKubeString = ".requireStructure(" + patternJson.toString() + ", " + keysJson.toString() + ")";
        ITextComponent jsonText = new StringTextComponent("[JSON]").modifyStyle(style -> style.mergeWithFormatting(TextFormatting.YELLOW).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(both.toString()))).setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, both.toString())));
        ITextComponent prettyJsonText = new StringTextComponent("[PRETTY JSON]").modifyStyle(style -> style.mergeWithFormatting(TextFormatting.GOLD).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(GSON.toJson(both)))).setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, GSON.toJson(both))));
        ITextComponent crafttweakerText = new StringTextComponent("[CRAFTTWEAKER]").modifyStyle(style -> style.mergeWithFormatting(TextFormatting.AQUA).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(ctKubeString))).setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ctKubeString)));
        ITextComponent kubeJSText = new StringTextComponent("[KUBEJS]").modifyStyle(style -> style.mergeWithFormatting(TextFormatting.DARK_PURPLE).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(ctKubeString))).setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ctKubeString)));
        ITextComponent message = new TranslationTextComponent("custommachinery.structure_creator.message", jsonText, prettyJsonText, crafttweakerText, kubeJSText);
        player.sendMessage(message, Util.DUMMY_UUID);
    }
}
