package fr.frinn.custommachinery;

import fr.frinn.custommachinery.common.command.CMCommand;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.buildinggadgets.BuildingGadgetsIntegration;
import fr.frinn.custommachinery.common.integration.theoneprobe.TOPInfoProvider;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.CustomMachineJsonReloadListener;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SLootTablesPacket;
import fr.frinn.custommachinery.common.network.SUpdateMachinesPacket;
import fr.frinn.custommachinery.common.network.SUpdateUpgradesPacket;
import fr.frinn.custommachinery.common.upgrade.MachineUpgrade;
import fr.frinn.custommachinery.common.upgrade.UpgradesCustomReloadListener;
import fr.frinn.custommachinery.common.util.CMLogger;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(CustomMachinery.MODID)
public class CustomMachinery {

    public static final String MODID = "custommachinery";

    public static Logger LOGGER = LogManager.getLogger("Custom Machinery");

    public static final Map<ResourceLocation, CustomMachine> MACHINES = new HashMap<>();
    public static final List<MachineUpgrade> UPGRADES = new ArrayList<>();

    public CustomMachinery() {
        CMLogger.init();

        final IEventBus MOD_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        MOD_BUS.addListener(this::commonSetup);
        MOD_BUS.addListener(EventPriority.LOWEST, this::sendIMCMessages);
        Registration.BLOCKS.register(MOD_BUS);
        Registration.ITEMS.register(MOD_BUS);
        Registration.TILE_ENTITIES.register(MOD_BUS);
        Registration.CONTAINERS.register(MOD_BUS);
        Registration.RECIPE_SERIALIZERS.register(MOD_BUS);
        Registration.RECIPE_TYPES.register(MOD_BUS);
        Registration.GUI_ELEMENTS.register(MOD_BUS);
        Registration.MACHINE_COMPONENTS.register(MOD_BUS);
        Registration.REQUIREMENTS.register(MOD_BUS);
        Registration.APPEARANCE_PROPERTIES.register(MOD_BUS);
        Registration.DATAS.register(MOD_BUS);

        if(ModList.get().isLoaded("buildinggadgets"))
            BuildingGadgetsIntegration.init(MOD_BUS);

        final IEventBus FORGE_BUS = MinecraftForge.EVENT_BUS;
        FORGE_BUS.addListener(this::addReloadListener);
        FORGE_BUS.addListener(this::serverStarting);
        FORGE_BUS.addListener(EventPriority.HIGHEST, this::datapackSync);
        FORGE_BUS.addListener(this::registerCommands);
        FORGE_BUS.addListener(this::onReload);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CMConfig.INSTANCE.getSpec());
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        NetworkManager.registerMessages();
        Registration.registerComponentVariants();
    }

    public void sendIMCMessages(final InterModEnqueueEvent event) {
        if(ModList.get().isLoaded("theoneprobe"))
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPInfoProvider::new);

        if(ModList.get().isLoaded("buildinggadgets"))
            BuildingGadgetsIntegration.sendIMC();
    }

    public void addReloadListener(final AddReloadListenerEvent event) {
        event.addListener(new CustomMachineJsonReloadListener());
        event.addListener(new UpgradesCustomReloadListener());
    }

    public void serverStarting(final ServerStartingEvent event) {
        LootTableHelper.generate(event.getServer());
    }

    public void datapackSync(final OnDatapackSyncEvent event) {
        if(event.getPlayer() != null) {
            ServerPlayer player = event.getPlayer();
            NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateMachinesPacket(CustomMachinery.MACHINES));
            NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SUpdateUpgradesPacket(CustomMachinery.UPGRADES));
            NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SLootTablesPacket(LootTableHelper.getLoots()));
        } else {
            NetworkManager.CHANNEL.send(PacketDistributor.ALL.noArg(), new SUpdateMachinesPacket(CustomMachinery.MACHINES));
            NetworkManager.CHANNEL.send(PacketDistributor.ALL.noArg(), new SUpdateUpgradesPacket(CustomMachinery.UPGRADES));
            LootTableHelper.generate(event.getPlayerList().getServer());
            NetworkManager.CHANNEL.send(PacketDistributor.ALL.noArg(), new SLootTablesPacket(LootTableHelper.getLoots()));
        }
    }

    public void registerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(CMCommand.register("custommachinery"));
        event.getDispatcher().register(CMCommand.register("cm"));
    }

    public void onReload(final CommandEvent event) {
        if(event.getParseResults().getReader().getString().startsWith("/reload"))
            CMLogger.reset();
    }
}
