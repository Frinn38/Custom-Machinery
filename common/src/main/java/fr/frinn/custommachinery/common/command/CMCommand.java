package fr.frinn.custommachinery.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.machine.CustomMachineJsonReloadListener;
import fr.frinn.custommachinery.common.network.SOpenCreationScreenPacket;
import fr.frinn.custommachinery.common.network.SOpenEditScreenPacket;
import fr.frinn.custommachinery.common.network.SOpenFilePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.InactiveProfiler;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class CMCommand {

    public static final SuggestionProvider<CommandSourceStack> ALL_MACHINES = SuggestionProviders.register(new ResourceLocation(CustomMachinery.MODID, "all_machines"), (commandContext, suggestionsBuilder) -> suggestCMResource(CustomMachinery.MACHINES.keySet(), suggestionsBuilder));

    public static LiteralArgumentBuilder<CommandSourceStack> register(String name) {
        return Commands.literal(name)
                .then(logging())
                .then(reload())
                .then(create())
                .then(edit());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> logging() {
        return Commands.literal("log")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    if(ctx.getSource().getEntity() instanceof ServerPlayer player)
                        new SOpenFilePacket(new File("logs/custommachinery.log").toURI().toString()).sendTo(player);
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> reload() {
        return Commands.literal("reload")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    if(ctx.getSource().getEntity() instanceof ServerPlayer player)
                        reloadMachines(player.server, player);
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("create")
                .requires(cs -> cs.hasPermission(2) && cs.isPlayer())
                .executes(ctx -> {
                    if(ctx.getSource().getEntity() instanceof ServerPlayer player)
                        new SOpenCreationScreenPacket().sendTo(player);
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> edit() {
        return Commands.literal("edit")
                .requires(cs -> cs.hasPermission(2) && cs.isPlayer())
                .then(Commands.argument("machine", ResourceLocationArgument.id())
                        .suggests(ALL_MACHINES)
                        .executes(ctx -> {
                            if(ctx.getSource().getEntity() instanceof ServerPlayer player)
                                new SOpenEditScreenPacket(ResourceLocationArgument.getId(ctx, "machine")).sendTo(player);
                            return 0;
                        }));
    }

    public static void reloadMachines(MinecraftServer server, @Nullable ServerPlayer player) {
        new CustomMachineJsonReloadListener().reload(CompletableFuture::completedFuture, server.getResourceManager(), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, server, server)
                .thenRun(() -> {
                    if(player != null)
                        player.sendSystemMessage(Component.translatable("custommachinery.command.reload").withStyle(ChatFormatting.GRAY));
                });
    }

    private static CompletableFuture<Suggestions> suggestCMResource(Iterable<ResourceLocation> resources, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        boolean bl = string.indexOf(58) > -1;
        for (ResourceLocation object : resources) {
            if (bl) {
                String string2 = object.toString();
                if (!SharedSuggestionProvider.matchesSubStr(string, string2)) continue;
                builder.suggest(object.toString());
                continue;
            }
            if (!SharedSuggestionProvider.matchesSubStr(string, object.getNamespace()) && (!object.getNamespace().equals(CustomMachinery.MODID) || !SharedSuggestionProvider.matchesSubStr(string, object.getPath()))) continue;
            builder.suggest(object.toString());
        }
        return builder.buildFuture();
    }
}
