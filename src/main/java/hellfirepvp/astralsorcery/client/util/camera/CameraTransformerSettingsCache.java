package hellfirepvp.astralsorcery.client.util.camera;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Player;

/**
 * Port of 1.16.5 {@code CameraTransformerSettingsCache}: caches view bobbing, gui visibility, camera type,
 * flight state and player position/rotation while a camera transformer is active and restores them afterwards.
 */
public abstract class CameraTransformerSettingsCache implements ICameraTransformer {

    private boolean active = false;

    private boolean viewBobbing = false, hideGui = false, flying = false;
    private CameraType thirdPersonView;

    private Vector3 startPosition;
    private float startYaw, startPitch;

    @Override
    public void onStartTransforming(float pTicks) {
        Minecraft mc = Minecraft.getInstance();

        this.viewBobbing = mc.options.bobView().get();
        this.hideGui = mc.options.hideGui;
        this.thirdPersonView = mc.options.getCameraType();
        Player player = mc.player;
        this.flying = player.getAbilities().flying;
        this.startPosition = new Vector3(player.getX(), player.getY(), player.getZ());
        this.startYaw = player.getYRot();
        this.startPitch = player.getXRot();
        player.setDeltaMovement(0, 0, 0);
        this.active = true;
    }

    @Override
    public void onStopTransforming(float pTicks) {
        if (active) {
            Options settings = Minecraft.getInstance().options;
            settings.bobView().set(viewBobbing);
            settings.hideGui = hideGui;
            settings.setCameraType(thirdPersonView);
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                player.getAbilities().flying = flying;
                player.absMoveTo(startPosition.getX(), startPosition.getY(), startPosition.getZ(), startYaw, startPitch);
                player.setDeltaMovement(0, 0, 0);
            }
            this.active = false;
        }
    }

    @Override
    public void transformRenderView(float pTicks) {
        if (!active) {
            return;
        }

        Options settings = Minecraft.getInstance().options;
        settings.hideGui = true;
        settings.bobView().set(false);
        settings.setCameraType(CameraType.THIRD_PERSON_BACK);
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.getAbilities().flying = true;
            player.setDeltaMovement(0, 0, 0);
        }
    }

}
