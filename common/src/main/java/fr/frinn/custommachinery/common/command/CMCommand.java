package fr.frinn.custommachinery.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.frinn.custommachinery.common.machine.CustomMachineJsonReloadListener;
import fr.frinn.custommachinery.common.network.SOpenCreationScreenPacket;
import fr.frinn.custommachinery.common.network.SOpenFilePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.InactiveProfiler;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class CMCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register(String name) {
        return Commands.literal(name)
                .then(logging())
                .then(reload())
                .then(create());
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
                        reloadMachines(player);
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

    public static void reloadMachines(ServerPlayer player) {
        new CustomMachineJsonReloadListener().reload(CompletableFuture::completedFuture, player.getServer().getResourceManager(), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, player.getServer(), player.getServer())
                .thenRun(() -> player.sendSystemMessage(Component.translatable("custommachinery.command.reload").withStyle(ChatFormatting.GRAY)));
    }
}
