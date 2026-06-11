package fr.frinn.custommachinery.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.ICustomMachineryAPI;
import fr.frinn.custommachinery.common.machine.CustomMachineJsonReloadListener;
import fr.frinn.custommachinery.common.network.SOpenEditScreenPacket;
import fr.frinn.custommachinery.common.network.SOpenFilePacket;
import fr.frinn.custommachinery.common.network.SOpenMachineCreationScreenPacket;
import fr.frinn.custommachinery.common.network.SOpenUpgradeCreationScreenPacket;
import fr.frinn.custommachinery.common.util.CMVerifier;
import fr.frinn.custommachinery.common.util.CMVerifier.Result;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.InactiveProfiler;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CMCommand {

    public static final SuggestionProvider<CommandSourceStack> ALL_MACHINES = SuggestionProviders.register(CustomMachinery.rl("all_machines"), (commandContext, suggestionsBuilder) -> suggestCMResource(editableMachines(), suggestionsBuilder));
    public static final SuggestionProvider<CommandSourceStack> ALL_TEMPLATES = SuggestionProviders.register(CustomMachinery.rl("all_templates"), (commandContext, suggestionsBuilder) -> suggestCMResource(editableTemplates(), suggestionsBuilder));

    public static LiteralArgumentBuilder<CommandSourceStack> register(String name) {
        return Commands.literal(name)
                .then(logging())
                .then(reload())
                .then(create())
                .then(edit())
                .then(verify())
                .then(editTemplate())
                .then(upgrade());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> logging() {
        return Commands.literal("log")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    if(ctx.getSource().getEntity() instanceof ServerPlayer player)
                        PacketDistributor.sendToPlayer(player, new SOpenFilePacket(new File("logs/custommachinery.log").toURI().toString()));
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
                        PacketDistributor.sendToPlayer(player, new SOpenMachineCreationScreenPacket());
                    return 0;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> edit() {
        return Commands.literal("edit")
                .requires(cs -> cs.hasPermission(2) && cs.isPlayer())
                .then(Commands.argument("machine", ResourceLocationArgument.id())
                        .suggests(ALL_MACHINES)
                        .executes(ctx -> {
                            if(ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                ResourceLocation machine = ResourceLocationArgument.getId(ctx, "machine");
                                if(!CustomMachinery.MACHINES.containsKey(machine) || CustomMachinery.MACHINES.get(machine).isDummy())
                                    player.sendSystemMessage(Component.translatable("custommachinery.command.edit.missing", machine.toString()).withStyle(ChatFormatting.GRAY));
                                else if(!CustomMachinery.MACHINES.get(machine).getLocation().canEdit())
                                    player.sendSystemMessage(Component.translatable("custommachinery.command.edit.cant", machine.toString()).withStyle(ChatFormatting.GRAY));
                                else
                                    PacketDistributor.sendToPlayer(player, new SOpenEditScreenPacket(machine));
                            }
                            return 0;
                        }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> editTemplate() {
        return Commands.literal("edit_template")
                .requires(cs -> cs.hasPermission(2) && cs.isPlayer())
                .then(Commands.argument("template", ResourceLocationArgument.id())
                        .suggests(ALL_TEMPLATES)
                        .executes(ctx -> {
                            if(ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                ResourceLocation template = ResourceLocationArgument.getId(ctx, "template");
                                if(!CustomMachinery.TEMPLATES.containsKey(template) || CustomMachinery.TEMPLATES.get(template).getFirst().isDummy())
                                    player.sendSystemMessage(Component.translatable("custommachinery.command.edit.missing", template.toString()).withStyle(ChatFormatting.GRAY));
                                else if(!CustomMachinery.TEMPLATES.get(template).getFirst().getLocation().canEdit())
                                    player.sendSystemMessage(Component.translatable("custommachinery.command.edit.cant", template.toString()).withStyle(ChatFormatting.GRAY));
                                else
                                    PacketDistributor.sendToPlayer(player, new SOpenEditScreenPacket(template));
                            }
                            return 0;
                        }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> upgrade() {
        return Commands.literal("upgrade")
                .requires(cs -> cs.hasPermission(2) && cs.isPlayer())
                .executes(ctx -> {
                    if(ctx.getSource().getEntity() instanceof ServerPlayer player) {
                        PacketDistributor.sendToPlayer(player, new SOpenUpgradeCreationScreenPacket());
                    }
                    return 0;
                });
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void reloadMachines(MinecraftServer server, @Nullable ServerPlayer player) {
        CustomMachineJsonReloadListener listener = new CustomMachineJsonReloadListener();
        listener.injectContext(new IContext() {
            @Override
            public <T> Map<ResourceLocation, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registryKey) {
                return server.registryAccess().registry(registryKey).map(registry -> registry.getTags().collect(Collectors.toMap(pair -> pair.getFirst().location(), pair -> (Collection<Holder<T>>)pair.getSecond().stream().toList()))).orElse(Collections.emptyMap());
            }
        }, server.registryAccess());
        listener.reload(CompletableFuture::completedFuture, server.getResourceManager(), InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE, server, server)
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

    private static List<ResourceLocation> editableMachines() {
        return CustomMachinery.MACHINES.entrySet().stream().filter(entry -> entry.getValue().getLocation().canEdit()).map(Entry::getKey).toList();
    }

    private static List<ResourceLocation> editableTemplates() {
        return CustomMachinery.TEMPLATES.entrySet().stream().filter(entry -> entry.getValue().getFirst().getLocation().canEdit()).map(Entry::getKey).toList();
    }

    private static ArgumentBuilder<CommandSourceStack, ?> verify() {
        return Commands.literal("verify")
                .requires(cs -> cs.hasPermission(2))
                .executes(ctx -> {
                    Result result = CMVerifier.verify(ctx.getSource().getLevel().getRecipeManager());
                    result.print(ICustomMachineryAPI.INSTANCE.logger());
                    if(result.errors() > 0)
                        ctx.getSource().sendSystemMessage(Component.translatable("custommachinery.command.verify.error", result.errors()).withStyle(ChatFormatting.RED).append(" ").append(Component.translatable("custommachinery.command.verify.log").withStyle(style -> style.withColor(ChatFormatting.GOLD).withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/cm log")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("custommachinery.command.verify.log.tooltip"))))));
                    else
                        ctx.getSource().sendSystemMessage(Component.translatable("custommachinery.command.verify.success").withStyle(ChatFormatting.GREEN).append(" ").append(Component.translatable("custommachinery.command.verify.log").withStyle(style -> style.withColor(ChatFormatting.GOLD).withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/cm log")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("custommachinery.command.verify.log.tooltip"))))));
                    return 0;
                });
    }
}
