package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.block.util.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.block.util.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.entity.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.List;

public class EntityNocturnalSpark extends ThrowableProjectile {

    private static final AABB NO_DUPE_BOX = new AABB(0, 0, 0, 1, 1, 1).inflate(15);

    private static final EntityDataAccessor<Boolean> SPAWNING = SynchedEntityData.defineId(EntityNocturnalSpark.class, EntityDataSerializers.BOOLEAN);
    private int ticksSpawning = 0;

    public EntityNocturnalSpark(EntityType<EntityNocturnalSpark> type, Level level) {
        super(type, level);
    }

    public EntityNocturnalSpark(EntityType<EntityNocturnalSpark> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level);
    }

    public EntityNocturnalSpark(EntityType<EntityNocturnalSpark> type, LivingEntity thrower, Level level) {
        super(type, thrower, level);
        this.shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), 0F, 0.7F, 0.9F);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SPAWNING, false);
    }

    public void setSpawning() {
        this.setDeltaMovement(Vec3.ZERO);
        this.entityData.set(SPAWNING, true);
    }

    public boolean isSpawning() {
        return this.entityData.get(SPAWNING);
    }

    @Override
    public void tick() {
        super.tick();

        if (!isAlive()) {
            return;
        }

        if (!level().isClientSide()) {
            removeLights();
            if (isSpawning()) {
                ticksSpawning++;
                spawnCycle();
                removeDuplicates();

                if (ticksSpawning > 200) {
                    discard();
                }
            }
        } else {
            spawnEffects();
        }
    }

    private void removeLights() {
        if (this.level() instanceof ServerLevel) {
            ServerLevel sWorld = (ServerLevel) this.level();
            if (this.tickCount % 5 == 0) {
                List<BlockPos> lightPositions = BlockDiscoverer.searchForBlocksAround(
                        sWorld, this.blockPosition(), 8,
                        (world, pos, state) -> !(state.getBlock() instanceof AirBlock) && state.getDestroySpeed(world, pos) != -1 && state.getLightEmission() > 3);
                for (BlockPos light : lightPositions) {
                    if (!sWorld.removeBlock(light, false)) {
                        sWorld.removeBlock(light, false);
                    }
                }
            }
        }
    }

    private void removeDuplicates() {
        List<EntityNocturnalSpark> sparks = level().getEntitiesOfClass(EntityNocturnalSpark.class, NO_DUPE_BOX.move(blockPosition().getX(), blockPosition().getY(), blockPosition().getZ()));
        for (EntityNocturnalSpark spark : sparks) {
            if (this.equals(spark)) {
                continue;
            }
            if (!spark.isAlive() || !spark.isSpawning()) {
                continue;
            }
            spark.discard();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnEffects() {
        if (isSpawning()) {
            for (int i = 0; i < 15; i++) {
                Vector3 thisPos = Vector3.atEntityCorner(this).addY(1);
                thisPos.addX(this.level().random.nextFloat() * (2 + this.level().random.nextInt(4)) * (this.level().random.nextBoolean() ? 1 : -1));
                thisPos.addY(this.level().random.nextFloat() * (2 + this.level().random.nextInt(4)) * (this.level().random.nextBoolean() ? 1 : -1));
                thisPos.addZ(this.level().random.nextFloat() * (2 + this.level().random.nextInt(4)) * (this.level().random.nextBoolean() ? 1 : -1));
                FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(thisPos)
                        .setScaleMultiplier(4)
                        .alpha(VFXAlphaFunction.PYRAMID)
                        .setAlphaMultiplier(0.7F)
                        .color(VFXColorFunction.constant(Color.BLACK));
                if (this.level().random.nextInt(5) == 0) {
                    randomizeColor(p);
                }
                if (this.level().random.nextInt(20) == 0) {
                    Vector3 at = Vector3.atEntityCorner(this);
                    at.addX(this.level().random.nextFloat() * 2 * (this.level().random.nextBoolean() ? 1 : -1));
                    at.addY(this.level().random.nextFloat() * 2 * (this.level().random.nextBoolean() ? 1 : -1));
                    at.addZ(this.level().random.nextFloat() * 2 * (this.level().random.nextBoolean() ? 1 : -1));
                    Vector3 to = Vector3.atEntityCorner(this);
                    to.addX(this.level().random.nextFloat() * 2 * (this.level().random.nextBoolean() ? 1 : -1));
                    to.addY(this.level().random.nextFloat() * 2 * (this.level().random.nextBoolean() ? 1 : -1));
                    to.addZ(this.level().random.nextFloat() * 2 * (this.level().random.nextBoolean() ? 1 : -1));

                    Effects.of(EffectTemplatesAS.LIGHTNING)
                            .spawn(at)
                            .color(VFXColorFunction.constant(Color.BLACK));
                }
            }
        } else {
            FXFacingParticle p;
            for (int i = 0; i < 6; i++) {
                p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(Vector3.atEntityCorner(this))
                        .setMotion(new Vector3(
                            0.04F - this.level().random.nextFloat() * 0.08F,
                            0.04F - this.level().random.nextFloat() * 0.08F,
                            0.04F - this.level().random.nextFloat() * 0.08F
                        ))
                        .setScaleMultiplier(0.25F);
                randomizeColor(p);
            }

            p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.atEntityCorner(this));
            p.setScaleMultiplier(0.6F);
            randomizeColor(p);

            p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.atEntityCorner(this).add(getDeltaMovement().multiply(0.5, 0.5, 0.5)));
            p.setScaleMultiplier(0.6F);
            randomizeColor(p);
        }
    }

    private void spawnCycle() {
        if (this.level().random.nextInt(12) == 0 && level() instanceof ServerLevel) {
            BlockPos pos = blockPosition();
            pos = pos.offset(this.level().random.nextInt(2) - this.level().random.nextInt(2), 1, this.level().random.nextInt(2) - this.level().random.nextInt(2));
            pos = BlockUtils.firstSolidDown(level(), pos).above();

            if (pos.distSqr(this.blockPosition()) >= 16) {
                return;
            }
            EntityUtils.performWorldSpawningAt(
                    (ServerLevel) level(), pos, MobCategory.MONSTER, MobSpawnType.SPAWNER, true,
                    EntityUtils.SpawnConditionFlags.IGNORE_SPAWN_CONDITIONS | EntityUtils.SpawnConditionFlags.IGNORE_ENTITY_COLLISION);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void randomizeColor(FXFacingParticle p) {
        switch (this.level().random.nextInt(3)) {
            case 0:
                p.color(VFXColorFunction.constant(ColorsAS.DYE_PURPLE));
                break;
            case 1:
                p.color(VFXColorFunction.constant(ColorsAS.DYE_CYAN));
                break;
            case 2:
                p.color(VFXColorFunction.constant(ColorsAS.DYE_BLACK));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (HitResult.Type.ENTITY.equals(result.getType())) {
            return;
        }
        Vec3 hit = result.getLocation();
        this.setSpawning();
        this.setPos(hit.x, hit.y, hit.z);
    }
}
