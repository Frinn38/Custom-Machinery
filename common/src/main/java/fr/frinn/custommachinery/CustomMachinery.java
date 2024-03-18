package fr.frinn.custommachinery;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandPerformEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import fr.frinn.custommachinery.api.component.variant.RegisterComponentVariantEvent;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.command.CMCommand;
import fr.frinn.custommachinery.common.component.variant.ComponentVariantRegistry;
import fr.frinn.custommachinery.common.init.BoxCreatorItem;
import fr.frinn.custommachinery.common.init.CustomMachineBlock;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.config.CMConfig;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.machine.CustomMachineJsonReloadListener;
import fr.frinn.custommachinery.common.network.PacketManager;
import fr.frinn.custommachinery.common.network.SLootTablesPacket;
import fr.frinn.custommachinery.common.network.SUpdateMachinesPacket;
import fr.frinn.custommachinery.common.network.SUpdateUpgradesPacket;
import fr.frinn.custommachinery.common.upgrade.Upgrades;
import fr.frinn.custommachinery.common.upgrade.UpgradesCustomReloadListener;
import fr.frinn.custommachinery.common.util.CMLogger;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CustomMachinery {

    public static final String MODID = "custommachinery";

    public static Logger LOGGER = LogManager.getLogger("Custom Machinery");

    public static final Map<ResourceLocation, CustomMachine> MACHINES = new HashMap<>();
    public static final BiMap<ResourceLocation, CustomMachineBlock> CUSTOM_BLOCK_MACHINES = HashBiMap.create();
    public static final Upgrades UPGRADES = new Upgrades();

    public static void init() {
        ConfigHolder<CMConfig> config = AutoConfig.register(CMConfig.class, Toml4jConfigSerializer::new);
        config.registerSaveListener((holder, cmConfig) -> {
            CMLogger.setDebugLevel(cmConfig.debugLevel.getLevel());
            return InteractionResult.SUCCESS;
        });

        CMLogger.init();

        Registration.BLOCKS.register();
        Registration.ITEMS.register();
        Registration.TILE_ENTITIES.register();
        Registration.CONTAINERS.register();
        Registration.RECIPE_SERIALIZERS.register();
        Registration.RECIPE_TYPES.register();
        Registration.CREATIVE_TABS.register();
        Registration.GUI_ELEMENTS.register();
        Registration.MACHINE_COMPONENTS.register();
        Registration.REQUIREMENTS.register();
        Registration.APPEARANCE_PROPERTIES.register();
        Registration.DATAS.register();
        Registration.PROCESSORS.register();

        RegisterComponentVariantEvent.EVENT.register(Registration::registerComponentVariants);

        LifecycleEvent.SETUP.register(CustomMachinery::setup);

        LifecycleEvent.SERVER_STARTING.register(CustomMachinery::serverStarting);

        ReloadListenerRegistry.register(PackType.SERVER_DATA, new CustomMachineJsonReloadListener());
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new UpgradesCustomReloadListener());

        CommandRegistrationEvent.EVENT.register(CustomMachinery::registerCommands);

        EnvExecutor.runInEnv(Env.CLIENT, () -> ClientHandler::init);

        InteractionEvent.LEFT_CLICK_BLOCK.register(CustomMachinery::boxRendererLeftClick);

        CommandPerformEvent.EVENT.register(CustomMachinery::onReloadStart);
    }

    private static void setup() {
        PacketManager.init();
        ComponentVariantRegistry.init();
    }

    private static void serverStarting(final MinecraftServer server) {
        LootTableHelper.generate(server);
    }

    public static void syncData(ServerPlayer player) {
            new SUpdateMachinesPacket(CustomMachinery.MACHINES).sendTo(player);
            new SUpdateUpgradesPacket(CustomMachinery.UPGRADES.getAllUpgrades()).sendTo(player);
            new SLootTablesPacket(LootTableHelper.getLoots()).sendTo(player);
    }

    private static void registerCommands(final CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext registry, final Commands.CommandSelection selection) {
        dispatcher.register(CMCommand.register("custommachinery"));
        dispatcher.register(CMCommand.register("cm"));
    }

    private static EventResult boxRendererLeftClick(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if(!player.level().isClientSide() && player.getItemInHand(hand).getItem() instanceof BoxCreatorItem)
            BoxCreatorItem.setSelectedBlock(true, player.getItemInHand(hand), pos);
        return EventResult.pass();
    }

    private static EventResult onReloadStart(CommandPerformEvent event) {
        if(event.getResults().getReader().getString().equals("reload") && event.getResults().getContext().getSource().hasPermission(2))
            CMLogger.reset();
        return EventResult.pass();
    }
}
