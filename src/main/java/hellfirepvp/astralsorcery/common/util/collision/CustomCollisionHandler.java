package hellfirepvp.astralsorcery.common.util.collision;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public interface CustomCollisionHandler {

    boolean shouldAddCollisionFor(Entity entity);

    void addCollision(Entity entity, AABB testBox, List<AABB> additionalCollision);
}
