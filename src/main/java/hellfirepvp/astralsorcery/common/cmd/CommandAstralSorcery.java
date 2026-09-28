package hellfirepvp.astralsorcery.common.cmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.cmd.sub.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandAstralSorcery {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> cmdAstralSorcery = dispatcher.register(
                Commands.literal("as")
                        .then(CommandAttune.register())
                        .then(CommandConstellation.register())
                        .then(CommandExp.register())
                        .then(CommandMaximizeAll.register())
                        .then(CommandReset.register())
                        .then(CommandProgress.register())
                        .then(CommandSerialize.register())
        );

        dispatcher.register(Commands.literal(AstralSorcery.MODID)
                .redirect(cmdAstralSorcery));
    }

}
