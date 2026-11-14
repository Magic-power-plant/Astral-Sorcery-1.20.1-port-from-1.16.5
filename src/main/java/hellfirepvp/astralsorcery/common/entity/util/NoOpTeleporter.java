package hellfirepvp.astralsorcery.common.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;

public class NoOpTeleporter {

    private final BlockPos targetPos;

    public NoOpTeleporter(BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, java.util.function.Function<Boolean, Entity> repositionEntity) {
        Entity created = repositionEntity.apply(false);
        created.teleportTo(destWorld, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, java.util.Collections.emptySet(), entity.getYRot(), entity.getXRot());
        return created;
    }

    public static PortalInfo createPortalInfo(BlockPos pos) {
        return new PortalInfo(
                new net.minecraft.world.phys.Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5),
                net.minecraft.world.phys.Vec3.ZERO,
                0, 0
        );
    }
}
