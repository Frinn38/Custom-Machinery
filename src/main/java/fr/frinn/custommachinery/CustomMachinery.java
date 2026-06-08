package fr.frinn.custommachinery;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import fr.frinn.custommachinery.common.command.CMCommand;
import fr.frinn.custommachinery.common.component.handler.FluidComponentHandler;
import fr.frinn.custommachinery.common.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.init.BoxCreatorItem;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.theoneprobe.TOPInfoProvider;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.CustomMachineJsonReloadListener;
import fr.frinn.custommachinery.common.machine.MachineLocation;
import fr.frinn.custommachinery.common.network.SLootTablesPacket;
import fr.frinn.custommachinery.common.network.SUpdateMachinesPacket;
import fr.frinn.custommachinery.common.network.SUpdateTemplatesPacket;
import fr.frinn.custommachinery.common.network.SUpdateUpgradesPacket;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.UpgradeLocation;
import fr.frinn.custommachinery.common.upgrade.Upgrades;
import fr.frinn.custommachinery.common.upgrade.UpgradesCustomReloadListener;
import fr.frinn.custommachinery.common.util.CMLogger;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import fr.frinn.custommachinery.impl.util.TextureInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

@Mod(CustomMachinery.MODID)
public class CustomMachinery {

    public static final String MODID = "custommachinery";

    public static final Logger LOGGER = LogManager.getLogger("Custom Machinery");

    public static final Map<ResourceLocation, CustomMachine> MACHINES = new HashMap<>();
    public static final Map<ResourceLocation, Pair<CustomMachine, Component>> TEMPLATES = new HashMap<>();
    public static final BiMap<ResourceLocation, CustomMachineBlock> CUSTOM_BLOCK_MACHINES = HashBiMap.create();
    public static final Upgrades UPGRADES = new Upgrades();

    public CustomMachinery(final ModContainer CONTAINER, final IEventBus MOD_BUS) {
        CONTAINER.registerConfig(Type.COMMON, CMConfig.CONFIG_SPEC);

        Registration.APPEARANCE_PROPERTIES.register(MOD_BUS);
        Registration.BLOCKS.register(MOD_BUS);
        Registration.CREATIVE_TABS.register(MOD_BUS);
        Registration.DATA_COMPONENTS.register(MOD_BUS);
        Registration.DATAS.register(MOD_BUS);
        Registration.ITEMS.register(MOD_BUS);
        Registration.GUI_ELEMENTS.register(MOD_BUS);
        Registration.LOOT_ITEM_FUNCTIONS.register(MOD_BUS);
        Registration.MACHINE_COMPONENTS.register(MOD_BUS);
        Registration.MENUS.register(MOD_BUS);
        Registration.PROCESSORS.register(MOD_BUS);
        Registration.RECIPE_SERIALIZERS.register(MOD_BUS);
        Registration.RECIPE_TYPES.register(MOD_BUS);
        Registration.REQUIREMENTS.register(MOD_BUS);
        Registration.TILE_ENTITIES.register(MOD_BUS);

        MOD_BUS.addListener(this::commonSetup);
        MOD_BUS.addListener(this::sendIMCMessages);
        MOD_BUS.addListener(this::registerCapabilities);
        MOD_BUS.addListener(this::reloadConfig);

        final IEventBus GAME_BUS = NeoForge.EVENT_BUS;

        GAME_BUS.addListener(this::serverStarting);
        GAME_BUS.addListener(this::syncDatapacks);
        GAME_BUS.addListener(this::registerReloadListener);
        GAME_BUS.addListener(this::registerCommands);
        GAME_BUS.addListener(this::boxRendererLeftClick);
        GAME_BUS.addListener(this::onReloadStart);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        CMLogger.init();
    }

    private void sendIMCMessages(final InterModEnqueueEvent event) {
        if(ModList.get().isLoaded("theoneprobe"))
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPInfoProvider::new);
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                ItemHandler.BLOCK,
                Registration.CUSTOM_MACHINE_TILE.get(),
                (be, side) -> be.getComponentManager().getComponentHandler(Registration.ITEM_MACHINE_COMPONENT.get())
                        .map(handler -> ((ItemComponentHandler)handler).getItemHandlerForSide(side))
                        .orElse(null)
        );
        event.registerBlockEntity(
                FluidHandler.BLOCK,
                Registration.CUSTOM_MACHINE_TILE.get(),
                (be, side) -> be.getComponentManager().getComponentHandler(Registration.FLUID_MACHINE_COMPONENT.get())
                        .map(handler -> ((FluidComponentHandler)handler).getFluidHandler(side))
                        .orElse(null)
        );
        event.registerBlockEntity(
                EnergyStorage.BLOCK,
                Registration.CUSTOM_MACHINE_TILE.get(),
                (be, side) -> be.getComponentManager().getComponent(Registration.ENERGY_MACHINE_COMPONENT.get())
                        .map(energy -> energy.getEnergyStorage(side))
                        .orElse(null)
        );
    }

    private void reloadConfig(final ModConfigEvent.Reloading event) {
        if(event.getConfig().getSpec() == CMConfig.CONFIG_SPEC)
            CMLogger.setDebugLevel(CMConfig.CONFIG.debugLevel.get().getLevel());
    }

    private void serverStarting(final ServerStartingEvent event) {
        LootTableHelper.generate(event.getServer());
        //Check machine file timestamps here because the server is not available on first load in CustomMachineJsonReloadListener
        //On /reload the timestamps will be checked by the CustomMachineJsonReloadListener because server will be available then
        MACHINES.forEach((id, machine) -> {
            if(!machine.getLocation().canEdit())
                return;
            File file = machine.getLocation().getFile(event.getServer());
            if(file != null && file.exists()) {
                try {
                    BasicFileAttributes attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();
                    machine.setLocation(MachineLocation.fromLoader(machine.getLocation().loader(), machine.getId(), machine.getLocation().packName(), attributes.creationTime(), attributes.lastModifiedTime()));
                } catch (IOException ignored) {

                }
            }
        });
        //Same for upgrades
        Map<UpgradeLocation, MachineUpgrade> timestamped = new HashMap<>();
        UPGRADES.getAllUpgrades().forEach((location, upgrade) -> {
            if(!location.canEdit())
                timestamped.put(location, upgrade);
            else {
                File file = location.getFile(event.getServer());
                if(file != null && file.exists()) {
                    try {
                        BasicFileAttributes attributes = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class).readAttributes();
                        timestamped.put(UpgradeLocation.fromLoader(location.loader(), location.id(), location.packName(), attributes.creationTime(), attributes.lastModifiedTime()), upgrade);
                    } catch (IOException ignored) {
                        timestamped.put(location, upgrade);
                    }
                }
            }
        });
        UPGRADES.refresh(timestamped);
    }

    private void syncDatapacks(final OnDatapackSyncEvent event) {
        if(event.getPlayer() != null)
            CustomMachinery.syncData(event.getPlayer());
        else {
            LootTableHelper.generate(event.getPlayerList().getServer());
            event.getPlayerList().getPlayers().forEach(CustomMachinery::syncData);
        }
    }

    private void registerReloadListener(final AddReloadListenerEvent event) {
        event.addListener(new CustomMachineJsonReloadListener());
        event.addListener(new UpgradesCustomReloadListener());
    }

    public static void syncData(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SUpdateMachinesPacket(CustomMachinery.MACHINES));
        PacketDistributor.sendToPlayer(player, new SUpdateTemplatesPacket(CustomMachinery.TEMPLATES));
        PacketDistributor.sendToPlayer(player, new SUpdateUpgradesPacket(CustomMachinery.UPGRADES.getAllUpgrades()));
        PacketDistributor.sendToPlayer(player, new SLootTablesPacket(LootTableHelper.getLoots()));
    }

    private void registerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(CMCommand.register("custommachinery"));
        event.getDispatcher().register(CMCommand.register("cm"));
    }

    private void boxRendererLeftClick(final PlayerInteractEvent.LeftClickBlock event) {
        if(event.getEntity() instanceof ServerPlayer player && player.getItemInHand(event.getHand()).getItem() instanceof BoxCreatorItem)
            BoxCreatorItem.setSelectedBlock(true, player.getItemInHand(event.getHand()), event.getPos());
    }

    private void onReloadStart(final CommandEvent event) {
        if(event.getParseResults().getReader().getString().equals("reload") && event.getParseResults().getContext().getSource().hasPermission(2))
            CMLogger.reset();
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static TextureInfo texture(String path) {
        return new TextureInfo(rl(path));
    }
}
