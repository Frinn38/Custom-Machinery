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
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class ClientPacketHandler {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void handleCraftingManagerStatusChangedPacket(BlockPos pos, MachineStatus status) {
        Minecraft instance = Minecraft.getInstance();
        if(instance.world != null) {
            TileEntity tile = instance.world.getTileEntity(pos);
            if(tile instanceof CustomMachineTile) {
                CustomMachineTile machineTile = (CustomMachineTile)tile;
                CraftingManager manager = machineTile.craftingManager;
                if(status != manager.getStatus()) {
                    manager.setStatus(status);
                    machineTile.requestModelDataUpdate();
                    instance.world.notifyBlockUpdate(tile.getPos(), tile.getBlockState(), tile.getBlockState(), Constants.BlockFlags.RERENDER_MAIN_THREAD);
                }
            }
        }
    }

    public static void handleRefreshCustomMachineTilePacket(BlockPos pos, ResourceLocation machine) {
        Minecraft instance = Minecraft.getInstance();
        if(instance.world != null) {
            TileEntity tile = instance.world.getTileEntity(pos);
            if(tile instanceof CustomMachineTile) {
                CustomMachineTile machineTile = (CustomMachineTile) tile;
                machineTile.setId(machine);
                machineTile.requestModelDataUpdate();
                instance.world.notifyBlockUpdate(pos, machineTile.getBlockState(), machineTile.getBlockState(), Constants.BlockFlags.RERENDER_MAIN_THREAD);
            }
        }
    }

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

    public static void handleUpdateContainerPacket(int windowId, List<IData<?>> data) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if(player != null && player.openContainer instanceof SyncableContainer && player.openContainer.windowId == windowId) {
            SyncableContainer container = (SyncableContainer)player.openContainer;
            data.forEach(container::handleData);
        }
    }
}
