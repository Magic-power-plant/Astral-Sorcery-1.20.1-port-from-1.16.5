package hellfirepvp.astralsorcery.client.util.camera;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Port of 1.16.5 {@code EntityCameraRenderView}: a detached local player used as camera entity.
 */
@OnlyIn(Dist.CLIENT)
public abstract class EntityCameraRenderView extends LocalPlayer {

    private Vector3 cameraFocus = null;

    public EntityCameraRenderView() {
        super(Minecraft.getInstance(),
                Minecraft.getInstance().level,
                Minecraft.getInstance().player.connection,
                Minecraft.getInstance().player.getStats(),
                Minecraft.getInstance().player.getRecipeBook(),
                false,
                false);

        getAbilities().mayfly = true;
        getAbilities().flying = true;
        getAbilities().invulnerable = true;
    }

    @Nullable
    public Vector3 getCameraFocus() {
        return cameraFocus;
    }

    public void setCameraFocus(@Nullable Vector3 cameraFocus) {
        this.cameraFocus = cameraFocus;
    }

    public void setAsRenderViewEntity() {
        Minecraft.getInstance().setCameraEntity(this);
    }

    public void transformToFocusOnPoint(Vector3 toFocus, float pTicks, boolean propagate) {
        Vector3 angles = Vector3.atEntityCorner(this).subtract(toFocus).copyToPolar();
        Vector3 prevAngles = new Vector3(xo, yo, zo).subtract(toFocus).copyToPolar();
        double pitch = 90 - angles.getY();
        double pitchPrev = 90 - prevAngles.getY();
        double yaw = -angles.getZ();
        double yawPrev = -prevAngles.getZ();

        if (propagate) {
            ClientCameraUtil.positionCamera(this, pTicks, getX(), getY(), getZ(), xo, yo, zo, yaw, yawPrev, pitch, pitchPrev);
        }
    }

    @Override
    public void turn(double yaw, double pitch) {}

    public abstract void moveEntityTick(EntityCameraRenderView entity, EntityClientReplacement replacementEntity, int ticksExisted);

    public abstract void onStopTransforming();

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public ItemStack getItemBySlot(EquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, @Nonnull ItemStack stack) {}

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
