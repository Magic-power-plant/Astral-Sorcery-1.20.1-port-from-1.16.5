package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.auxiliary.link.IItemLinkingTool;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;

/**
 * Port of 1.16.5 {@code ItemLinkingTool}: starts/continues {@link LinkHandler} sessions on block and entity interaction.
 */
public class ItemLinkingTool extends Item implements IItemLinkingTool {

    public ItemLinkingTool(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean shouldInterceptBlockInteract(LogicalSide side, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public boolean shouldInterceptEntityInteract(LogicalSide side, Player player, InteractionHand hand, Entity interacted) {
        return interacted instanceof Player;
    }

    @Override
    public boolean doBlockInteract(LogicalSide side, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        Level world = player.level();
        if (!world.isClientSide()) {
            LinkHandler.LinkSession session = LinkHandler.getActiveSession(player);
            if (session != null && session.getType() == LinkHandler.LinkType.ENTITY) {
                LinkHandler.RightClickResult result = LinkHandler.onInteractBlock(player, world, pos, player.isShiftKeyDown());
                if (result.shouldProcess()) {
                    LinkHandler.processInteraction(result, player, world, pos);
                    return true;
                }
            }
            LinkHandler.RightClickResult result = LinkHandler.onInteractBlock(player, world, pos, player.isShiftKeyDown());
            if (result.shouldProcess()) {
                LinkHandler.processInteraction(result, player, world, pos);
            }
        } else {
            player.swing(hand);
        }
        return true;
    }

    @Override
    public boolean doEntityInteract(LogicalSide side, Player player, InteractionHand hand, Entity interacted) {
        if (!(interacted instanceof LivingEntity target)) {
            return false;
        }
        Level world = player.level();
        if (!world.isClientSide()) {
            LinkHandler.LinkSession session = LinkHandler.getActiveSession(player);
            if (session == null || session.getType() == LinkHandler.LinkType.ENTITY) {
                LinkHandler.RightClickResult result = LinkHandler.onInteractEntity(player, target);
                if (result.shouldProcess()) {
                    LinkHandler.processInteraction(result, player, world, BlockPos.ZERO);
                }
            }
        } else {
            player.swing(hand);
        }
        return true;
    }
}
