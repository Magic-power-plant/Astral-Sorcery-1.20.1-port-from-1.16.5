package hellfirepvp.astralsorcery.common.blockentity;

import hellfirepvp.astralsorcery.common.entity.technical.EntityObservatoryHelper;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.base.TickableBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.blockentity.NamedInventoryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ObservatoryBlockEntity extends TickableBlockEntity implements NamedInventoryBlockEntity {

    @Nullable
    private UUID entityHelperRef = null;
    private Integer entityIdServerRef = null;

    public float observatoryYaw = 0, prevObservatoryYaw = 0;
    public float observatoryPitch = -45, prevObservatoryPitch = -45;

    public ObservatoryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.OBSERVATORY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.astralsorcery.observatory");
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getLevel().isClientSide()) {
            if (this.entityHelperRef == null) {
                this.createNewObservatoryEntity();
            } else {
                Entity helper;
                if ((helper = resolveEntity(this.entityHelperRef)) == null || !helper.isAlive()) {
                    this.createNewObservatoryEntity();
                }
            }
        }
    }

    public boolean isUsable() {
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                if (xx == 0 && zz == 0) {
                    continue;
                }
                BlockPos other = worldPosition.offset(xx, 0, zz);
                if (!MiscUtils.canSeeSky(this.getLevel(), other, false, true)) {
                    return false;
                }
            }
        }
        return MiscUtils.canSeeSky(this.getLevel(), this.worldPosition.above(), true, false);
    }

    private Entity createNewObservatoryEntity() {
        this.setEntityHelperRef(null);
        this.entityIdServerRef = null;

        EntityObservatoryHelper helper = EntityTypesAS.OBSERVATORY_HELPER.get().create(this.getLevel());
        helper.setFixedObservatoryPos(this.worldPosition);
        helper.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.1, worldPosition.getZ() + 0.5);
        helper.setYRot(0);
        helper.setXRot(0);
        this.getLevel().addFreshEntity(helper);

        this.setEntityHelperRef(helper.getUUID());
        this.entityIdServerRef = helper.getId();
        return helper;
    }

    @Nullable
    private Entity resolveEntity(UUID entityUUID) {
        if (entityUUID == null) {
            return null;
        }
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(worldPosition.offset(-3, -1, -3), worldPosition.offset(3, 2, 3)));
        for (Entity e : entities) {
            if (e.getUUID().equals(entityUUID)) {
                this.entityIdServerRef = e.getId();
                return e;
            }
        }
        return null;
    }

    @Nullable
    public Entity findRideableObservatoryEntity() {
        if (this.getEntityHelperRef() == null || this.entityIdServerRef == null) {
            return null;
        }
        return this.getLevel().getEntity(this.entityIdServerRef);
    }

    @Nullable
    public UUID getEntityHelperRef() {
        return entityHelperRef;
    }

    public void setEntityHelperRef(@Nullable UUID entityHelperRef) {
        this.entityHelperRef = entityHelperRef;
        markForUpdate();
    }

    public void updatePitchYaw(float pitch, float prevPitch, float yaw, float prevYaw) {
        this.observatoryPitch = pitch;
        this.prevObservatoryPitch = prevPitch;
        this.observatoryYaw = yaw;
        this.prevObservatoryYaw = prevYaw;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.entityHelperRef = NBTHelper.getUUID(compound, "entity", null);
        this.observatoryYaw = compound.getFloat("oYaw");
        this.observatoryPitch = compound.getFloat("oPitch");
        this.prevObservatoryYaw = compound.getFloat("oYawPrev");
        this.prevObservatoryPitch = compound.getFloat("oPitchPrev");
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        if (this.entityHelperRef != null) {
            compound.putUUID("entity", this.entityHelperRef);
        }
        compound.putFloat("oYaw", this.observatoryYaw);
        compound.putFloat("oPitch", this.observatoryPitch);
        compound.putFloat("oYawPrev", this.prevObservatoryYaw);
        compound.putFloat("oPitchPrev", this.prevObservatoryPitch);
    }
}
