package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import fr.frinn.custommachinery.CustomMachinery;

public class PacketManager {

    public static final SimpleNetworkManager MANAGER = SimpleNetworkManager.create(CustomMachinery.MODID);

    //Server to Client
    public static final MessageType UPDATE_MACHINES = MANAGER.registerS2C("update_machines", SUpdateMachinesPacket::read);
    public static final MessageType UPDATE_UPGRADES = MANAGER.registerS2C("update_upgrades", SUpdateUpgradesPacket::read);
    public static final MessageType UPDATE_CONTAINER = MANAGER.registerS2C("update_container", SUpdateContainerPacket::read);
    public static final MessageType UPDATE_MACHINE_STATUS = MANAGER.registerS2C("update_machine_status", SUpdateMachineStatusPacket::read);
    public static final MessageType REFRESH_MACHINE_TILE = MANAGER.registerS2C("refresh_machine_tile", SRefreshCustomMachineTilePacket::read);
    public static final MessageType LOOT_TABLES = MANAGER.registerS2C("loot_tables", SLootTablesPacket::read);
    public static final MessageType OPEN_FILE = MANAGER.registerS2C("open_file", SOpenFilePacket::read);
    public static final MessageType UPDATE_MACHINE_APPEARANCE = MANAGER.registerS2C("update_machine_appearance", SUpdateMachineAppearancePacket::read);

    //Client to Server
    public static final MessageType ADD_MACHINE = MANAGER.registerC2S("add_machine", CAddMachinePacket::read);
    public static final MessageType REMOVE_MACHINE = MANAGER.registerC2S("remove_machine", CRemoveMachinePacket::read);
    public static final MessageType ELEMENT_CLICKED = MANAGER.registerC2S("element_clicked", CGuiElementClickPacket::read);
    public static final MessageType CHANGE_SIDE_MODE = MANAGER.registerC2S("change_side_mode", CChangeSideModePacket::read);
    public static final MessageType UPDATE_MACHINE_DATA = MANAGER.registerC2S("button_gui_element", CButtonGuiElementPacket::read);
    public static final MessageType PLACE_STRUCTURE = MANAGER.registerC2S("place_structure", CPlaceStructurePacket::read);

    public static void init() {}
}
