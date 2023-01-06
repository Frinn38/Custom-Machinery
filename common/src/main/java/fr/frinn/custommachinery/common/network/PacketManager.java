package fr.frinn.custommachinery.common.network;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import fr.frinn.custommachinery.CustomMachinery;

public class PacketManager {

    public static final SimpleNetworkManager MANAGER = SimpleNetworkManager.create(CustomMachinery.MODID);

    //Server to Client
    public static final MessageType UPDATE_MACHINES = MANAGER.registerS2C("update_machines", SUpdateMachinesPacket::decode);
    public static final MessageType UPDATE_UPGRADES = MANAGER.registerS2C("update_upgrades", SUpdateUpgradesPacket::decode);
    public static final MessageType UPDATE_CONSTAINER = MANAGER.registerS2C("update_container", SUpdateContainerPacket::decode);
    public static final MessageType UPDATE_MACHINE_STATUS = MANAGER.registerS2C("update_machine_status", SUpdateMachineStatusPacket::decode);
    public static final MessageType REFRESH_MACHINE_TILE = MANAGER.registerS2C("refresh_machine_tile", SRefreshCustomMachineTilePacket::decode);
    public static final MessageType LOOT_TABLES = MANAGER.registerS2C("loot_tables", SLootTablesPacket::decode);
    public static final MessageType OPEN_FILE = MANAGER.registerS2C("open_file", SOpenFilePacket::decode);

    //Client to Server
    public static final MessageType ADD_MACHINE = MANAGER.registerC2S("add_machine", CAddMachinePacket::decode);
    public static final MessageType REMOVE_MACHINE = MANAGER.registerC2S("remove_machine", CRemoveMachinePacket::decode);
    public static final MessageType ELEMENT_CLICKED = MANAGER.registerC2S("element_clicked", CGuiElementClickPacket::decode);
    public static final MessageType CHANGE_SIDE_MODE = MANAGER.registerC2S("change_side_mode", CChangeSideModePacket::decode);

    public static void init() {}
}
