package hellfirepvp.astralsorcery.common.util.collision;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.*;

public class CollisionManager {

    private static final List<CustomCollisionHandler> customHandlers = new ArrayList<>();

    private static final int maxCacheSize = 20;
    //1.16.5 keyed this LRU cache on the VoxelShapeSpliterator instance that is currently scanning;
    //1.20.1 renamed that class to the generic net.minecraft.world.level.BlockCollisions<T>. The type
    //argument is irrelevant for identity, hence the wildcard.
    private static final LinkedList<BlockCollisions<?>> accessList = new LinkedList<>();
    private static final Map<BlockCollisions<?>, List<AABB>> instanceFlags = new HashMap<>();

    public static void init() {
        register(new PlayerWalkableAir());
    }

    public static void register(CustomCollisionHandler handler) {
        customHandlers.add(handler);
    }

    @Nullable
    public static AABB getIteratorBoundingBoxes(BlockCollisions<?> iterator, @Nullable Entity entity) {
        if (!instanceFlags.containsKey(iterator)) {
            List<AABB> additionalBoundingBoxes = getAdditionalBoundingBoxes(entity);
            if (additionalBoundingBoxes.isEmpty()) {
                return null;
            }
            removeOldestEntry();
            instanceFlags.put(iterator, additionalBoundingBoxes);
            accessList.addFirst(iterator);
        }
        List<AABB> boxes = instanceFlags.get(iterator);
        if (boxes == null || boxes.isEmpty()) {
            return null;
        }
        markActive(iterator);
        return boxes.remove(0);
    }

    public static boolean needsCustomCollision(@Nullable Entity entity) {
        for (CustomCollisionHandler handler : customHandlers) {
            if (handler.shouldAddCollisionFor(entity)) {
                return true;
            }
        }
        return false;
    }

    public static List<AABB> getAdditionalBoundingBoxes(@Nullable Entity entity) {
        List<AABB> additionalCollision = new ArrayList<>();
        AABB entityBox = entity != null ? entity.getBoundingBox() : new AABB(BlockPos.ZERO);
        customHandlers.stream()
                .filter(handler -> handler.shouldAddCollisionFor(entity))
                .forEach(handler -> handler.addCollision(entity, entityBox, additionalCollision));
        return additionalCollision;
    }

    private static void removeOldestEntry() {
        if (accessList.size() >= maxCacheSize) {
            BlockCollisions<?> oldest;
            //Apparently the list can be both >= 20 elements in size AND empty at the same time.
            try {
                oldest = accessList.removeLast();
            } catch (NoSuchElementException exc) {
                if (accessList.isEmpty()) {
                    return;
                }
                try {
                    oldest = accessList.get(accessList.size() - 1);
                } catch (Exception e) {
                    return;
                }
            }
            if (oldest != null) {
                instanceFlags.remove(oldest);
            }
        }
    }

    private static void markActive(BlockCollisions<?> it) {
        if (accessList.remove(it)) {
            accessList.addFirst(it);
        }
    }
}
