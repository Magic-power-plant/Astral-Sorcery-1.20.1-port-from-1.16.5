package hellfirepvp.astralsorcery.common.cmd.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockStateHelper;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CommandSerialize {

    private CommandSerialize() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("serialize")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.literal("hand")
                        .executes(CommandSerialize::serializeHand))
                .then(Commands.literal("look")
                        .executes(CommandSerialize::serializeLook));
    }

    private static int serializeHand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        String serialized = JsonHelper.serializeItemStack(held).toString();

        MutableComponent msg = Component.literal(serialized);
        Style s = Style.EMPTY.applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, serialized));
        msg.setStyle(s);

        context.getSource().sendSuccess(() -> msg, true);
        return Command.SINGLE_SUCCESS;
    }

    private static int serializeLook(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockHitResult result = MiscUtils.rayTraceLookBlock(player);
        BlockState state = result == null ? Blocks.AIR.defaultBlockState() : player.level().getBlockState(result.getBlockPos());
        String serialized = BlockStateHelper.serialize(state);

        MutableComponent msg = Component.literal(serialized);
        Style s = Style.EMPTY.applyFormat(ChatFormatting.GREEN)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Copy")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, serialized));
        msg.setStyle(s);

        context.getSource().sendSuccess(() -> msg, true);
        return Command.SINGLE_SUCCESS;
    }
}
