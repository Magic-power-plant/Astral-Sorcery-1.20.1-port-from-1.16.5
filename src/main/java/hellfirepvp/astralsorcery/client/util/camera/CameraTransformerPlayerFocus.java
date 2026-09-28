package hellfirepvp.astralsorcery.client.util.camera;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Port of 1.16.5 {@code CameraTransformerPlayerFocus}: swaps a client replacement entity into the world for the
 * duration of a camera flight and drives the {@link EntityCameraRenderView} along its path.
 */
public class CameraTransformerPlayerFocus extends CameraTransformerSettingsCache {

    private final EntityCameraRenderView entity;
    private final ICameraPersistencyFunction func;

    private EntityClientReplacement clientEntity;

    public CameraTransformerPlayerFocus(EntityCameraRenderView renderView, ICameraPersistencyFunction func) {
        this.entity = renderView;
        this.func = func;
    }

    @Override
    public void onStartTransforming(float pTicks) {
        super.onStartTransforming(pTicks);

        Minecraft mc = Minecraft.getInstance();
        EntityClientReplacement repl = new EntityClientReplacement();
        repl.load(mc.player.saveWithoutId(new CompoundTag()));
        mc.level.addPlayer(repl.getId(), repl);
        this.clientEntity = repl;

        entity.setAsRenderViewEntity();
    }

    @Override
    public void onStopTransforming(float pTicks) {
        super.onStopTransforming(pTicks);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && this.clientEntity != null) {
            mc.level.removeEntity(this.clientEntity.getId(), Entity.RemovalReason.DISCARDED);
        }

        if (mc.player != null && this.clientEntity != null) {
            Player player = mc.player;
            player.absMoveTo(this.clientEntity.getX(), this.clientEntity.getY(), this.clientEntity.getZ(), this.clientEntity.getYRot(), this.clientEntity.getXRot());
            player.setDeltaMovement(0, 0, 0);
        }

        ClientCameraUtil.resetCamera();

        if (mc.level != null) {
            entity.onStopTransforming();
        }
    }

    @Override
    public void transformRenderView(float pTicks) {
        super.transformRenderView(pTicks);

        Vector3 focus = entity.getCameraFocus();
        if (focus != null) {
            entity.transformToFocusOnPoint(focus, pTicks, true);
        }
    }

    @Override
    public ICameraPersistencyFunction getPersistencyFunction() {
        return func;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void onClientTick() {
        entity.tickCount++;

        if (clientEntity != null) {
            entity.moveEntityTick(entity, clientEntity, entity.tickCount);
        }
    }
}
