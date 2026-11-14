package hellfirepvp.astralsorcery.common.entity.technical;

import com.google.common.collect.Iterables;
import hellfirepvp.astralsorcery.common.container.ContainerObservatory;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.ObservatoryBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityObservatoryHelper extends Entity {

    private static final EntityDataAccessor<BlockPos> FIXED = SynchedEntityData.defineId(EntityObservatoryHelper.class, EntityDataSerializers.BLOCK_POS);

    public EntityObservatoryHelper(EntityType<EntityObservatoryHelper> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(FIXED, BlockPos.ZERO);
    }

    public void setFixedObservatoryPos(BlockPos pos) {
        this.entityData.set(FIXED, pos);
    }

    public BlockPos getFixedObservatoryPos() {
        return this.entityData.get(FIXED);
    }

    @Nullable
    public ObservatoryBlockEntity getAssociatedObservatory() {
        BlockPos at = this.getFixedObservatoryPos();
        ObservatoryBlockEntity observatory = MiscUtils.getBlockEntityAt(this.level(), at, ObservatoryBlockEntity.class, true);
        if (observatory == null) {
            return null;
        }
        UUID helperRef = observatory.getEntityHelperRef();
        if (helperRef == null || !helperRef.equals(this.getUUID())) {
            return null;
        }
        return observatory;
    }

    @Override
    public void tick() {
        super.tick();

        this.noPhysics = true;

        ObservatoryBlockEntity observatory;
        if ((observatory = this.getAssociatedObservatory()) == null) {
            if (!this.level().isClientSide()) {
                this.discard();
            }
            return;
        }

        Entity riding = Iterables.getFirst(this.getPassengers(), null);
        if (riding instanceof Player) {
            this.applyObservatoryRotationsFrom(observatory, (Player) riding, true);
        } else {
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        if (!observatory.isUsable()) {
            this.ejectPassengers();
        }
    }

    public void applyObservatoryRotationsFrom(ObservatoryBlockEntity to, Player riding, boolean updateBlockEntity) {
        if (riding.containerMenu instanceof ContainerObservatory) {
            this.setYRot(riding.yHeadRot);
            this.yRotO = riding.yHeadRotO;
            this.setXRot(riding.getXRot());
            this.xRotO = riding.xRotO;
        } else  {
            this.setYRot(riding.yBodyRot);
            this.yRotO = riding.yBodyRotO;
        }

        to.updatePitchYaw(this.getXRot(), this.xRotO, this.getYRot(), this.yRotO);
        if (updateBlockEntity) {
            to.markForUpdate();
        }

        double xOffset = -0.85;
        double zOffset = 0.15;
        double yawRad = -Math.toRadians(to.observatoryYaw);
        double xComp = 0.5F + Math.sin(yawRad) * xOffset - Math.cos(yawRad) * zOffset;
        double zComp = 0.5F + Math.cos(yawRad) * xOffset + Math.sin(yawRad) * zOffset;
        Vector3 pos = new Vector3(to.getBlockPos()).add(xComp, 0.4F, zComp);
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        if (!super.canAddPassenger(entity)) {
            return false;
        }
        ObservatoryBlockEntity observatory = this.getAssociatedObservatory();
        return observatory != null && observatory.isUsable();
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    public boolean isBurning() {
        return false;
    }

    public boolean isGlowing() {
        return false;
    }

    public boolean isPushedByWater() {
        return false;
    }

    public boolean isImmuneToExplosions() {
        return true;
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    public boolean canPassengerSteer() {
        return false;
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return new ItemStack(BlocksAS.OBSERVATORY.get());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {}
}
