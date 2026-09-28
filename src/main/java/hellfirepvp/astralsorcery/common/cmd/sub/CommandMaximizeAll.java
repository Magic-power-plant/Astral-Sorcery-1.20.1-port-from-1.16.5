package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CommandMaximizeAll implements Command<CommandSourceStack> {

    private static final CommandMaximizeAll CMD = new CommandMaximizeAll();

    private CommandMaximizeAll() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("maximize")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            ctx.getSource().sendSuccess(() -> Component.literal("Success!").withStyle(ChatFormatting.GREEN), true);
                            maximizeAll(target);
                            return 0;
                        }))
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        maximizeAll(context.getSource().getPlayerOrException());
        context.getSource().sendSuccess(() -> Component.literal("Success!").withStyle(ChatFormatting.GREEN), true);
        return 0;
    }

    private static boolean maximizeAll(ServerPlayer entity) {
        return ResearchManager.forceMaximizeAll(entity);
    }
}
