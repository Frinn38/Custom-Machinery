package fr.frinn.custommachinery.common.network;

import fr.frinn.custommachinery.CustomMachinery;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CustomMachinery.MODID, bus = Bus.MOD)
public class PacketManager {

    @SubscribeEvent
    public static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // Server to Client
        registrar.playToClient(SLootTablesPacket.TYPE, SLootTablesPacket.CODEC, SLootTablesPacket::handle);
        registrar.playToClient(SOpenCreationScreenPacket.TYPE, SOpenCreationScreenPacket.CODEC, SOpenCreationScreenPacket::handle);
        registrar.playToClient(SOpenEditScreenPacket.TYPE, SOpenEditScreenPacket.CODEC, SOpenEditScreenPacket::handle);
        registrar.playToClient(SOpenFilePacket.TYPE, SOpenFilePacket.CODEC, SOpenFilePacket::handle);
        registrar.playToClient(SRefreshCustomMachineTilePacket.TYPE, SRefreshCustomMachineTilePacket.CODEC, SRefreshCustomMachineTilePacket::handle);
        registrar.playToClient(SUpdateContainerPacket.TYPE, SUpdateContainerPacket.CODEC, SUpdateContainerPacket::handle);
        registrar.playToClient(SUpdateMachineGuiElementsPacket.TYPE, SUpdateMachineGuiElementsPacket.CODEC, SUpdateMachineGuiElementsPacket::handle);
        registrar.playToClient(SUpdateMachinesPacket.TYPE, SUpdateMachinesPacket.CODEC, SUpdateMachinesPacket::handle);
        registrar.playToClient(SUpdateMachineAppearancePacket.TYPE, SUpdateMachineAppearancePacket.CODEC, SUpdateMachineAppearancePacket::handle);
        registrar.playToClient(SUpdateMachineStatusPacket.TYPE, SUpdateMachineStatusPacket.CODEC, SUpdateMachineStatusPacket::handle);
        registrar.playToClient(SUpdateUpgradesPacket.TYPE, SUpdateUpgradesPacket.CODEC, SUpdateUpgradesPacket::handle);

        // Client to Server
        registrar.playToServer(CAddMachinePacket.TYPE, CAddMachinePacket.CODEC, CAddMachinePacket::handle);
        registrar.playToServer(CButtonGuiElementPacket.TYPE, CButtonGuiElementPacket.CODEC, CButtonGuiElementPacket::handle);
        registrar.playToServer(CChangeSideModePacket.TYPE, CChangeSideModePacket.CODEC, CChangeSideModePacket::handle);
        registrar.playToServer(CEditMachinePacket.TYPE, CEditMachinePacket.CODEC, CEditMachinePacket::handle);
        registrar.playToServer(CGuiElementClickPacket.TYPE, CGuiElementClickPacket.CODEC, CGuiElementClickPacket::handle);
        registrar.playToServer(CPlaceStructurePacket.TYPE, CPlaceStructurePacket.CODEC, CPlaceStructurePacket::handle);
        registrar.playToServer(CRemoveMachinePacket.TYPE, CRemoveMachinePacket.CODEC, CRemoveMachinePacket::handle);
        registrar.playToServer(CSetFilterSlotItemPacket.TYPE, CSetFilterSlotItemPacket.CODEC, CSetFilterSlotItemPacket::handle);
    }
}
