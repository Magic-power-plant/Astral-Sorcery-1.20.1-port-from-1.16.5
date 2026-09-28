package hellfirepvp.astralsorcery.client.util.camera.path;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.camera.CameraTransformerPlayerFocus;
import hellfirepvp.astralsorcery.client.util.camera.ClientCameraManager;
import hellfirepvp.astralsorcery.client.util.camera.ICameraStopListener;
import hellfirepvp.astralsorcery.client.util.camera.ICameraTickListener;
import hellfirepvp.astralsorcery.client.util.camera.ICameraTransformer;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Port of 1.16.5 {@code CameraPathBuilder}.
 */
@OnlyIn(Dist.CLIENT)
public class CameraPathBuilder {

    private final CameraPath path;

    private CameraPathBuilder(Vector3 start, Vector3 cameraFocus) {
        this.path = new CameraPath(start, cameraFocus, null);
    }

    public CameraPathBuilder(CameraPath path) {
        this.path = path;
    }

    public static CameraPathBuilder builder(Vector3 start, Vector3 cameraFocus) {
        return new CameraPathBuilder(start, cameraFocus);
    }

    public CameraPathBuilder addPoint(Vector3 nextPoint, int ticksToFlyThere) {
        if (ticksToFlyThere < 0) {
            AstralSorcery.LOG.warn("Tried to add a point with negative tick-timespan to a camera flight. Skipping...");
            return this;
        }
        this.path.addPoint(nextPoint, ticksToFlyThere);
        return this;
    }

    public CameraPathBuilder addCircularPoints(Vector3 centerOffset, double radius, int amountOfPointsOnCircle, int ticksBetweenEachPoint) {
        return addCircularPoints(centerOffset, (deg) -> radius, amountOfPointsOnCircle, ticksBetweenEachPoint);
    }

    public CameraPathBuilder addCircularPoints(Vector3 centerOffset, DynamicRadiusGetter radiusFn, int amountOfPointsOnCircle, int ticksBetweenEachPoint) {
        if (ticksBetweenEachPoint < 0) {
            AstralSorcery.LOG.warn("Tried to add a point with negative tick-timespan to a camera flight. Skipping...");
            return this;
        }
        double degPerPoint = 360D / ((double) amountOfPointsOnCircle);
        for (int i = 0; i < amountOfPointsOnCircle; i++) {
            double deg = i * degPerPoint;
            Vector3 point = Vector3.RotAxis.Y_AXIS.clone().perpendicular().normalize().multiply(radiusFn.getRadius(deg)).rotate(Math.toRadians(deg), Vector3.RotAxis.Y_AXIS).add(centerOffset);
            addPoint(point, ticksBetweenEachPoint);
        }
        return this;
    }

    public CameraPathBuilder copy() {
        return new CameraPathBuilder(this.path.copy());
    }

    public CameraPathBuilder setTickDelegate(ICameraTickListener delegate) {
        this.path.setTickListener(delegate);
        return this;
    }

    public CameraPathBuilder setStopDelegate(ICameraStopListener delegate) {
        this.path.setStopListener(delegate);
        return this;
    }

    public ICameraTransformer finishAndStart() {
        if (this.path.pathPoints.size() <= 0) {
            AstralSorcery.LOG.warn("Tried to start a camera path without any points! Skipping...");
            return null;
        }

        CameraTransformerPlayerFocus cameraTransformer = new CameraTransformerPlayerFocus(this.path, this.path);
        ClientCameraManager.INSTANCE.addTransformer(cameraTransformer);
        return cameraTransformer;
    }

    public interface DynamicRadiusGetter {

        double getRadius(double degree);

        static DynamicRadiusGetter dyanmicIncrease(double base, double incPerStep) {
            return new DynamicRadiusGetter() {

                private final double baseDst = base;
                private int count = 0;

                @Override
                public double getRadius(double degree) {
                    double rad = baseDst + count * incPerStep;
                    count++;
                    return rad;
                }
            };
        }

    }
}
