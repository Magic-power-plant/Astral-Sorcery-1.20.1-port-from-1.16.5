package hellfirepvp.astralsorcery.client.util.camera.path;

import hellfirepvp.astralsorcery.client.util.camera.EntityCameraRenderView;
import hellfirepvp.astralsorcery.client.util.camera.EntityClientReplacement;
import hellfirepvp.astralsorcery.client.util.camera.ICameraPersistencyFunction;
import hellfirepvp.astralsorcery.client.util.camera.ICameraStopListener;
import hellfirepvp.astralsorcery.client.util.camera.ICameraTickListener;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.LinkedList;

/**
 * Port of 1.16.5 {@code CameraPath}: a camera render view that interpolates along a list of timed path points.
 */
@OnlyIn(Dist.CLIENT)
public class CameraPath extends EntityCameraRenderView implements ICameraPersistencyFunction {

    LinkedList<PathPoint> pathPoints = new LinkedList<>();
    private final Vector3 startVector, focus;

    private ICameraTickListener delegate;
    private ICameraStopListener stopDelegate;

    private int totalTickDuration = 0;
    private boolean expired = false;
    private boolean stopped = false;

    CameraPath(Vector3 startPoint, Vector3 focusPoint, @Nullable ICameraTickListener tick) {
        this.startVector = startPoint;
        this.focus = focusPoint;
        this.setPosRaw(startPoint.getX(), startPoint.getY(), startPoint.getZ());
        this.xo = getX();
        this.yo = getY();
        this.zo = getZ();
        this.delegate = tick;
        setCameraFocus(focusPoint);
        transformToFocusOnPoint(focusPoint, 0, false);
    }

    public void setTickListener(ICameraTickListener delegate) {
        this.delegate = delegate;
    }

    public void setStopListener(ICameraStopListener delegate) {
        this.stopDelegate = delegate;
    }

    void addPoint(Vector3 point, int ticks) {
        this.pathPoints.addLast(new PathPoint(point, ticks));
        this.totalTickDuration += ticks;
    }

    @Override
    public void moveEntityTick(EntityCameraRenderView entity, EntityClientReplacement replacement, int ticksExisted) {
        if (delegate != null) {
            delegate.onCameraTick(entity, replacement);
        }
        setCameraFocus(Vector3.atEntityCorner(replacement));
        this.expired = this.tickCount > totalTickDuration;
        if (pathPoints.isEmpty()) {
            this.expired = true;
        } else {
            Vector3 position = queryByTicks(ticksExisted);
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            this.setPosRaw(position.getX(), position.getY(), position.getZ());
        }
    }

    @Override
    public void onStopTransforming() {
        if (stopDelegate != null && Minecraft.getInstance().level != null) {
            stopDelegate.onCameraStop();
        }
    }

    private Vector3 queryByTicks(int ticks) {
        if (ticks <= 0) {
            return startVector;
        }
        int acc = 0;
        PathPoint current = null;
        Vector3 prev;
        for (PathPoint point : pathPoints) {
            int accumulator = acc + point.ticksToGetThere;

            prev = current == null ? startVector : current.dstPoint;
            current = point;

            if (accumulator >= ticks) {
                int interp = current.ticksToGetThere - (accumulator - ticks);
                int dstJump = current.ticksToGetThere;
                return current.dstPoint.clone().subtract(prev).divide(dstJump).multiply(Mth.clamp(interp, 1, dstJump)).add(prev);
            } else {
                acc = accumulator;
            }
        }
        return pathPoints.getLast().dstPoint; //Doesn't happen since the list isn't empty.
    }

    @Override
    public boolean isExpired() {
        return expired;
    }

    @Override
    public void setExpired() {
        this.expired = true;
    }

    @Override
    public void forceStop() {
        this.stopped = true;
        setExpired();
    }

    @Override
    public boolean wasForciblyStopped() {
        return stopped;
    }

    CameraPath copy() {
        CameraPath c = new CameraPath(this.startVector, this.focus, this.delegate);
        for (PathPoint fp : this.pathPoints) {
            c.pathPoints.addLast(new PathPoint(fp.dstPoint, fp.ticksToGetThere));
        }
        c.totalTickDuration = this.totalTickDuration;
        return c;
    }

    private static class PathPoint {

        private final Vector3 dstPoint;
        private final int ticksToGetThere;

        PathPoint(Vector3 dstPoint, int ticksToGetThere) {
            this.dstPoint = dstPoint;
            this.ticksToGetThere = ticksToGetThere;
        }
    }
}
