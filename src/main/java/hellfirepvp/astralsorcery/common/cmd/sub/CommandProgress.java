package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.data.research.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

public class CommandProgress {

    private CommandProgress() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("progress")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("progress", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    String[] names = new String[ProgressionTier.values().length];
                                    for (int i = 0; i < ProgressionTier.values().length; i++) {
                                        names[i] = ProgressionTier.values()[i].name();
                                    }
                                    return SharedSuggestionProvider.suggest(names, builder);
                                })
                                .executes(ctx -> {
                                    ServerPlayer src = ctx.getSource().getPlayerOrException();
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    String tierName = StringArgumentType.getString(ctx, "progress");
                                    ProgressionTier goal;
                                    try {
                                        goal = ProgressionTier.valueOf(tierName.toUpperCase());
                                    } catch (IllegalArgumentException e) {
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("Invalid progression tier: " + tierName).withStyle(ChatFormatting.RED), true);
                                        return 0;
                                    }
                                    return pushPlayerToProgress(ctx.getSource(), target, goal);
                                })));
    }

    private static int pushPlayerToProgress(CommandSourceStack src, Player target, ProgressionTier goal) {
        Component targetName = target.getDisplayName();
        PlayerProgress progress = ResearchHelper.getProgress(target, LogicalSide.SERVER);
        if (!progress.isValid() || progress.getTierReached().isThisLaterOrEqual(goal)) {
            src.sendSuccess(() -> Component.literal("Failed! ").append(targetName).append(Component.literal("'s progress is higher or equal to ")).append(Component.literal(goal.name()))
                    .withStyle(ChatFormatting.RED), true);
            return 0;
        }
        ResearchProgression research = null;
        switch (goal) {
            case DISCOVERY:
                research = ResearchProgression.DISCOVERY;
                break;
            case BASIC_CRAFT:
                research = ResearchProgression.BASIC_CRAFT;
                break;
            case ATTUNEMENT:
                research = ResearchProgression.ATTUNEMENT;
                break;
            case CONSTELLATION_CRAFT:
                research = ResearchProgression.CONSTELLATION;
                break;
            case TRAIT_CRAFT:
                research = ResearchProgression.RADIANCE;
                break;
            case BRILLIANCE:
                research = ResearchProgression.BRILLIANCE;
                break;
            default:
                break;
        }
        if (research == null) {
            src.sendSuccess(() -> Component.literal("Invalid progression tier: " + goal.name()).withStyle(ChatFormatting.RED), true);
        }
        if (ResearchManager.grantProgress(target, goal) && ResearchManager.grantResearch(target, research)) {
            src.sendSuccess(() -> Component.literal("Success!").withStyle(ChatFormatting.GREEN), true);
            return Command.SINGLE_SUCCESS;
        } else {
            src.sendSuccess(() -> Component.literal("Failed!").withStyle(ChatFormatting.RED), true);
            return 0;
        }
    }
}
