/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.util.collision.CollisionHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinEntity
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5: Entity#collideBoundingBoxHeuristically(Entity, Vector3d, AxisAlignedBB, World,
 *         ISelectionContext, ReuseableStream&lt;VoxelShape&gt;) with @At(value = "RETURN", ordinal = 1).
 * 1.20.1: Entity#collideBoundingBox(Entity, Vec3, AABB, Level, List&lt;VoxelShape&gt;) - the
 *         single-axis heuristic fast path was removed together with ReuseableStream, so the method
 *         has exactly one RETURN (verified with javap: the only areturn is the
 *         collideWithShapes(...) tail call). Plain @At("RETURN") therefore hits the same
 *         "full collision result" position that ordinal = 1 hit in 1.16.5.
 */
@Mixin(Entity.class)
public class MixinEntity {

    @Inject(method = "collideBoundingBox", at = @At("RETURN"), cancellable = true)
    private static void addCustomCollision(Entity entity, Vec3 vec, AABB collisionBox, Level world, List<VoxelShape> potentialHits, CallbackInfoReturnable<Vec3> cir) {
        if (entity == null) {
            return;
        }
        Vec3 allowedMovement = CollisionHelper.onEntityCollision(vec, entity);
        if (allowedMovement != null) {
            cir.setReturnValue(allowedMovement);
        }
    }
}
