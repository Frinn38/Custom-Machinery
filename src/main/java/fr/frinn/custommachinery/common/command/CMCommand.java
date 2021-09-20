package fr.frinn.custommachinery.common.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.frinn.custommachinery.common.config.CMConfig;
import fr.frinn.custommachinery.common.network.NetworkManager;
import fr.frinn.custommachinery.common.network.SOpenFilePacket;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.io.File;

public class CMCommand {

    public static LiteralArgumentBuilder<CommandSource> register(String name) {
        return Commands.literal(name)
                .then(logging());
    }

    private static ArgumentBuilder<CommandSource, ?> logging() {
        return Commands.literal("log")
                .requires(cs -> cs.hasPermissionLevel(2))
                .executes(ctx -> {
                    if(ctx.getSource().getEntity() instanceof ServerPlayerEntity)
                        NetworkManager.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)ctx.getSource().getEntity()), new SOpenFilePacket(new File("logs/custommachinery.log").toURI().toString()));
                    return 0;
                })
                .then(Commands.literal("enableLogging")
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(new StringTextComponent("enableLogging = " + CMConfig.INSTANCE.enableLogging.get()), false);
                        return 0;
                    })
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(ctx -> {
                            boolean value = BoolArgumentType.getBool(ctx, "value");
                            CMConfig.INSTANCE.enableLogging.set(value);
                            return 0;
                        })
                    )
                );
    }
}
