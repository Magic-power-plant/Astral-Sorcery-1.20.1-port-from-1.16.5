package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.cmd.argument.ArgumentTypeConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CommandAttune implements Command<CommandSourceStack> {

    private static final CommandAttune CMD = new CommandAttune();

    private CommandAttune() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("attune")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("constellation", ArgumentTypeConstellation.major())
                                .executes(CMD)));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        IMajorConstellation cst = (IMajorConstellation) context.getArgument("constellation", IConstellation.class);

        if (ResearchManager.setAttunedConstellation(player, cst)) {
            context.getSource().sendSuccess(() ->
                    Component.literal("Success! Player has been attuned to ").append(
                            ((net.minecraft.network.chat.MutableComponent) cst.getConstellationName()).withStyle(ChatFormatting.BLUE)
                    ).withStyle(ChatFormatting.GREEN), true);
        } else {
            context.getSource().sendSuccess(() ->
                    Component.literal("Failed! Player specified doesn't seem to have the research progress necessary!").withStyle(ChatFormatting.RED), true);
        }
        return 0;
    }
}
