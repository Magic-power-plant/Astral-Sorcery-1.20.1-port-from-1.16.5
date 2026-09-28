package hellfirepvp.astralsorcery.client.util.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Port of 1.16.5 {@code ClientCameraUtil}. Positions both the camera entity and the real client player so that
 * render interpolation, head/body rotation and the camera bob values agree with the camera path.
 */
public class ClientCameraUtil {

    public static void positionCamera(Player renderView, float pTicks, double x, double y, double z, double prevX, double prevY, double prevZ, double yaw, double yawPrev, double pitch, double pitchPrev) {
        double dYaw = Mth.positiveModulo(yaw - yawPrev, 360d);
        // Use the smaller arc
        if (dYaw > 180) {
            dYaw -= 360;
        }
        yawPrev = yaw - dYaw;
        float iYaw = Mth.lerp(pTicks, (float) yawPrev, (float) yaw);
        float iPitch = Mth.lerp(pTicks, (float) pitchPrev, (float) pitch);

        Minecraft mc = Minecraft.getInstance();
        Entity rv = mc.getCameraEntity();
        if (rv == null || !rv.equals(renderView)) {
            mc.setCameraEntity(renderView);
            rv = renderView;
        }
        applyPosition((Player) rv, x, y, z, prevX, prevY, prevZ, iYaw, iPitch);

        Player player = mc.player;
        if (player != null && player != rv) {
            applyPosition(player, x, y, z, prevX, prevY, prevZ, iYaw, iPitch);
        }
    }

    private static void applyPosition(Player render, double x, double y, double z, double prevX, double prevY, double prevZ, float iYaw, float iPitch) {
        render.setPosRaw(x, y, z);
        render.xo = prevX;
        render.yo = prevY;
        render.zo = prevZ;
        render.xOld = prevX;
        render.yOld = prevY;
        render.zOld = prevZ;

        render.setYRot(iYaw);
        render.yRotO = iYaw;
        render.yHeadRot = iYaw;
        render.yHeadRotO = iYaw;
        render.yBodyRot = iYaw;
        render.yBodyRotO = iYaw;
        render.setXRot(iPitch);
        render.xRotO = iPitch;
        if (render instanceof LocalPlayer localPlayer) {
            //1.16.5 aligned cameraYaw/prevCameraYaw; the 1.20.1 equivalents are the camera bob fields.
            localPlayer.yBob = iYaw;
            localPlayer.yBobO = iYaw;
        }
    }

    public static void resetCamera() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Player player = mc.player;
            mc.setCameraEntity(player);

            if (mc.screen != null) {
                mc.setScreen(null);
            }
        }
    }
}
