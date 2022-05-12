package fr.frinn.custommachinery.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.frinn.custommachinery.api.machine.MachineStatus;
import fr.frinn.custommachinery.api.network.IData;
import fr.frinn.custommachinery.common.crafting.CraftingManager;
import fr.frinn.custommachinery.common.init.CustomMachineTile;
import fr.frinn.custommachinery.common.network.SyncableContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class ClientPacketHandler {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void handleCraftingManagerStatusChangedPacket(BlockPos pos, MachineStatus status) {
        if(Minecraft.getInstance().level != null) {
            BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile) {
                CustomMachineTile machineTile = (CustomMachineTile)tile;
                CraftingManager manager = machineTile.craftingManager;
                if(status != manager.getStatus()) {
                    manager.setStatus(status);
                    machineTile.requestModelDataUpdate();
                    Minecraft.getInstance().level.sendBlockUpdated(tile.getBlockPos(), tile.getBlockState(), tile.getBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    public static void handleRefreshCustomMachineTilePacket(BlockPos pos, ResourceLocation machine) {
        if(Minecraft.getInstance().level != null) {
            BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(pos);
            if(tile instanceof CustomMachineTile) {
                CustomMachineTile machineTile = (CustomMachineTile) tile;
                machineTile.setId(machine);
                machineTile.requestModelDataUpdate();
                Minecraft.getInstance().level.sendBlockUpdated(pos, machineTile.getBlockState(), machineTile.getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public static void handleStructureCreatorPacket(JsonElement keysJson, JsonElement patternJson) {
        Player player = Minecraft.getInstance().player;
        if(player == null)
            return;
        JsonObject both = new JsonObject();
        both.add("keys", keysJson);
        both.add("pattern", patternJson);
        String ctKubeString = ".requireStructure(" + patternJson.toString() + ", " + keysJson.toString() + ")";
        Component jsonText = new TextComponent("[JSON]").withStyle(style -> style.applyFormats(ChatFormatting.YELLOW).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(both.toString()))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, both.toString())));
        Component prettyJsonText = new TextComponent("[PRETTY JSON]").withStyle(style -> style.applyFormats(ChatFormatting.GOLD).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(GSON.toJson(both)))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, GSON.toJson(both))));
        Component crafttweakerText = new TextComponent("[CRAFTTWEAKER]").withStyle(style -> style.applyFormats(ChatFormatting.AQUA).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(ctKubeString))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ctKubeString)));
        Component kubeJSText = new TextComponent("[KUBEJS]").withStyle(style -> style.applyFormats(ChatFormatting.DARK_PURPLE).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(ctKubeString))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ctKubeString)));
        Component message = new TranslatableComponent("custommachinery.structure_creator.message", jsonText, prettyJsonText, crafttweakerText, kubeJSText);
        player.sendMessage(message, Util.NIL_UUID);
    }

    public static void handleUpdateContainerPacket(int windowId, List<IData<?>> data) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null && player.containerMenu instanceof SyncableContainer && player.containerMenu.containerId == windowId) {
            SyncableContainer container = (SyncableContainer)player.containerMenu;
            data.forEach(container::handleData);
        }
    }
}
