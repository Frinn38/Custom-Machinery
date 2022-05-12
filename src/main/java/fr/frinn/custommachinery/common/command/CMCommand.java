package fr.frinn.custommachinery.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SOpenFilePacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.io.File;

public class CMCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register(String name) {
        return Commands.literal(name)
                .then(logging());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> logging() {
        return Commands.literal("log")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    if(ctx.getSource().getEntity() instanceof ServerPlayer)
                        NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)ctx.getSource().getEntity()), new SOpenFilePacket(new File("logs/custommachinery.log").toURI().toString()));
                    return 0;
                });
    }
}
