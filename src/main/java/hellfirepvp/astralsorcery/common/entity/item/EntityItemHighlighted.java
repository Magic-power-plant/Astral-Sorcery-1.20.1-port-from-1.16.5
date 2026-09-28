package hellfirepvp.astralsorcery.common.entity.item;

import hellfirepvp.astralsorcery.common.util.reflection.ReflectionHelper;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import javax.annotation.Nullable;
import java.awt.*;

public class EntityItemHighlighted extends EntityCustomItemReplacement {

    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(EntityItemHighlighted.class, EntityDataSerializers.INT);
    private static final int NO_COLOR = 0xFF000000;

    public EntityItemHighlighted(EntityType<? extends EntityItemHighlighted> type, Level world) {
        super(type, world);
        ReflectionHelper.setSkipItemPhysicsRender(this);
        refreshDimensions();
    }

    public EntityItemHighlighted(EntityType<? extends EntityItemHighlighted> type, Level world, double x, double y, double z) {
        this(type, world);
        this.setPos(x, y, z);
        this.setYRot(this.level().random.nextFloat() * 360.0F);
        this.setDeltaMovement(this.level().random.nextDouble() * 0.2D - 0.1D, 0.2D, this.level().random.nextDouble() * 0.2D - 0.1D);
    }

    public EntityItemHighlighted(EntityType<? extends EntityItemHighlighted> type, Level world, double x, double y, double z, ItemStack stack) {
        this(type, world, x, y, z);
        this.setItem(stack);
        this.lifespan = stack.isEmpty() ? 6000 : stack.getEntityLifespan(world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_COLOR, NO_COLOR);
    }

    public void applyColor(@Nullable Color color) {
        this.entityData.set(DATA_COLOR, color == null ? NO_COLOR : (color.getRGB() & 0x00FFFFFF));
    }

    public boolean hasColor() {
        return this.entityData.get(DATA_COLOR) != NO_COLOR;
    }

    @Nullable
    public Color getHighlightColor() {
        if (!hasColor()) {
            return null;
        }
        int colorInt = this.entityData.get(DATA_COLOR);
        return new Color(colorInt, false);
    }

    @Override
    public void tick() {
        boolean onGround = this.onGround();
        super.tick();
        if (this.onGround() != onGround) {
            refreshDimensions();
        }
    }

    @Override
    public void setOnGround(boolean grounded) {
        boolean updateSize = onGround() != grounded;
        super.setOnGround(grounded);
        if (updateSize) {
            refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        if (!this.onGround()) {
            return EntityType.ITEM.getDimensions();
        }
        return this.getType().getDimensions();
    }
}
