package hellfirepvp.astralsorcery.common.util.collision;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class CollisionHelper {

    /**
     * 1.16.5: onCollision(VoxelShapeSpliterator iterator, Consumer&lt;? super VoxelShape&gt; action)
     * 1.20.1: net.minecraft.util.math.shapes.VoxelShapeSpliterator became the generic
     *         net.minecraft.world.level.BlockCollisions&lt;T&gt;. The type argument only describes what
     *         BlockCollisions#resultProvider maps the (pos, shape) pair to; everything this method
     *         touches (the scanned box and the colliding entity) is independent of it, so the
     *         parameter is declared as BlockCollisions&lt;?&gt; and the caller keeps its own T.
     */
    public static boolean onCollision(BlockCollisions<?> iterator, Consumer<? super VoxelShape> action) {
        //1.16.5 read iterator.entity directly; 1.20.1 BlockCollisions has no such field and wraps
        //the entity into BlockCollisions#context instead (see getCollidingEntity).
        Entity entity = getCollidingEntity(iterator);
        if (!CollisionManager.needsCustomCollision(entity)) {
            return false;
        }
        AABB box = CollisionManager.getIteratorBoundingBoxes(iterator, entity);
        if (box == null) {
            return false;
        }

        VoxelShape floor = Shapes.create(box);
        //1.16.5: VoxelShapes.compare(a, b, IBooleanFunction.AND) -> 1.20.1 Shapes.joinIsNotEmpty(a, b, BooleanOp.AND)
        //1.16.5: iterator.aabb.grow(1.0E-7D)                     -> 1.20.1 iterator.box.inflate(1.0E-7D)
        if (Shapes.joinIsNotEmpty(floor, Shapes.create(iterator.box.inflate(1.0E-7D)), BooleanOp.AND)) {
            action.accept(floor);
            return true;
        }
        return false;
    }

    /**
     * 1.20.1 only: replacement for the 1.16.5 field VoxelShapeSpliterator#entity. BlockCollisions
     * only keeps the CollisionContext its constructor built from the entity - that is an
     * EntityCollisionContext for every entity-driven scan (CollisionContext#of(Entity)) and the
     * entity-less EntityCollisionContext.EMPTY for CollisionContext#empty(), whose getEntity() is
     * null. Nullable either way, exactly like the 1.16.5 field was.
     */
    @Nullable
    private static Entity getCollidingEntity(BlockCollisions<?> iterator) {
        CollisionContext context = iterator.context;
        if (context instanceof EntityCollisionContext entityContext) {
            return entityContext.getEntity();
        }
        return null;
    }

    @Nullable
    public static Vec3 onEntityCollision(Vec3 allowedMovement, Entity entity) {
        if (!CollisionManager.needsCustomCollision(entity)) {
            return null;
        }
        List<AABB> additionalBoxes = CollisionManager.getAdditionalBoundingBoxes(entity);
        //1.16.5: AxisAlignedBB#grow(double) -> 1.20.1 AABB#inflate(double)
        AABB entityBox = entity.getBoundingBox().inflate(1.0E-7D);
        for (AABB box : additionalBoxes) {
            //1.16.5: VoxelShape#getAllowedOffset(Axis, AxisAlignedBB, double) -> 1.20.1 VoxelShape#collide(Axis, AABB, double)
            double newYMovement = Shapes.create(box).collide(Direction.Axis.Y, entityBox, allowedMovement.y);
            allowedMovement = new Vec3(allowedMovement.x, newYMovement, allowedMovement.z);
        }

        return allowedMovement;
    }
}
