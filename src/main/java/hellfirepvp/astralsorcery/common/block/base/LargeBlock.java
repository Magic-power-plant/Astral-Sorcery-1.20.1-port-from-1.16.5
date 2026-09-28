package hellfirepvp.astralsorcery.common.block.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface LargeBlock {

    AABB getBlockSpace();

    default boolean canPlaceAt(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level world = ctx.getLevel();
        AABB box = this.getBlockSpace();

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        for (int xx = (int) box.minX; xx <= box.maxX; xx++) {
            for (int yy = (int) box.minY; yy <= box.maxY; yy++) {
                for (int zz = (int) box.minZ; zz <= box.maxZ; zz++) {
                    mPos.set(pos.getX() + xx, pos.getY() + yy, pos.getZ() + zz);
                    net.minecraft.world.level.block.state.BlockState existing = world.getBlockState(mPos);
                    if (!existing.isAir() && !existing.canBeReplaced(ctx)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
