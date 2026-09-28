package hellfirepvp.astralsorcery.common.item.base;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.LogicalSide;

public interface OverrideInteractItem {

    boolean shouldInterceptBlockInteract(LogicalSide side, Player player, InteractionHand hand, BlockPos pos, Direction face);

    default boolean shouldInterceptEntityInteract(LogicalSide side, Player player, InteractionHand hand, Entity interacted) {
        return false;
    }

    boolean doBlockInteract(LogicalSide side, Player player, InteractionHand hand, BlockPos pos, Direction face);

    default boolean doEntityInteract(LogicalSide side, Player player, InteractionHand hand, Entity interacted) {
        return false;
    }

}
