package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.cmd.argument.ArgumentTypeConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;

public class CommandConstellation {

    private CommandConstellation() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("constellation")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("memorize")
                        .then(Commands.argument("constellation", ArgumentTypeConstellation.any())
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            IConstellation cst = ctx.getArgument("constellation", IConstellation.class);
                                            return markConstellationMemorized(ctx.getSource(), target, cst);
                                        }))
                                .executes(ctx -> {
                                    IConstellation cst = ctx.getArgument("constellation", IConstellation.class);
                                    return markConstellationMemorized(ctx.getSource(), null, cst);
                                })))
                .then(Commands.literal("discover")
                        .then(Commands.argument("constellation", ArgumentTypeConstellation.any())
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            IConstellation cst = ctx.getArgument("constellation", IConstellation.class);
                                            return discoverConstellation(ctx.getSource(), target, cst);
                                        }))
                                .executes(ctx -> {
                                    IConstellation cst = ctx.getArgument("constellation", IConstellation.class);
                                    return discoverConstellation(ctx.getSource(), null, cst);
                                })));
    }

    private static int markConstellationMemorized(CommandSourceStack src, @Nullable Player target, IConstellation cst) throws CommandSyntaxException {
        //1.16.5 uses src.asPlayer(), which throws; 1.20.1 renamed that to getPlayerOrException(). The port picked
        //the similarly named but @Nullable getPlayer() instead, so a console/RCON/command-block invocation NPEd on
        //the first sendSystemMessage below - after the grant had already been made and saved.
        Player source = src.getPlayerOrException();
        target = target != null ? target : source;
        Component targetName = target.getDisplayName();
        PlayerProgress progress = ResearchHelper.getProgress(target, LogicalSide.SERVER);
        if (!progress.isValid() || progress.hasSeenConstellation(cst)) {
            source.sendSystemMessage(Component.literal("Failed! ").append(targetName).append(Component.literal(" has already seen ")).append(cst.getConstellationName())
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (ResearchManager.memorizeConstellation(cst, target)) {
            ResearchHelper.sendConstellationMemorizationMessage(target, progress, cst);
            source.sendSystemMessage(Component.literal("Success! ")
                    .withStyle(ChatFormatting.GREEN));
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendSystemMessage(Component.literal("Failed!").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int discoverConstellation(CommandSourceStack src, @Nullable Player target, IConstellation cst) throws CommandSyntaxException {
        //1.16.5 uses src.asPlayer(), which throws; 1.20.1 renamed that to getPlayerOrException(). The port picked
        //the similarly named but @Nullable getPlayer() instead, so a console/RCON/command-block invocation NPEd on
        //the first sendSystemMessage below - after the grant had already been made and saved.
        Player source = src.getPlayerOrException();
        target = target != null ? target : source;
        Component targetName = target.getDisplayName();
        PlayerProgress progress = ResearchHelper.getProgress(target, LogicalSide.SERVER);
        if (!progress.isValid() || progress.hasConstellationDiscovered(cst)) {
            source.sendSystemMessage(Component.literal("Failed! ").append(targetName).append(Component.literal(" has already discovered ")).append(cst.getConstellationName())
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (ResearchManager.discoverConstellation(cst, target)) {
            ResearchHelper.sendConstellationDiscoveryMessage(target, cst);
            source.sendSystemMessage(Component.literal("Success! ").withStyle(ChatFormatting.GREEN));
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendSystemMessage(Component.literal("Failed!").withStyle(ChatFormatting.RED));
            return 0;
        }
    }
}
