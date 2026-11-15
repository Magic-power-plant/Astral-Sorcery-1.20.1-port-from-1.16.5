package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingSprite;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectBootes;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.data.config.entry.EntityConfig;
import hellfirepvp.astralsorcery.common.effect.visual.Effects;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.damage.DamageUtil;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.entity.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class EntityFlare extends Mob {

    private static final int RANDOM_WANDER_RANGE = 31;

    private int entityAge = 0;
    private Vector3 currentMoveTarget = null;

    private boolean ambient = false;
    private int followingEntityId = -1;

    private Object texClientSprite = null;

    public EntityFlare(EntityType<EntityFlare> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1);
    }

    public static void spawnAmbientFlare(Level world, BlockPos at) {
        if (world.isClientSide() || EntityConfig.CONFIG.flareAmbientSpawnChance.get() <= 0) {
            return;
        }
        float nightPercent = DayTimeHelper.getCurrentDaytimeDistribution(world);
        if (world.random.nextInt(EntityConfig.CONFIG.flareAmbientSpawnChance.get()) == 0 && world.random.nextFloat() < nightPercent) {
            MiscUtils.executeWithChunk(world, at, () -> {
                if (world.isEmptyBlock(at)) {
                    EntityFlare flare = EntityTypesAS.FLARE.get().create(world);
                    flare.setPos(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5);
                    flare.setAmbient(true);
                    world.addFreshEntity(flare);
                }
            });
        }
    }

    public EntityFlare setAmbient(boolean ambient) {
        this.ambient = ambient;
        return this;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public EntityFlare setFollowingTarget(LivingEntity entity) {
        this.followingEntityId = entity.getId();
        return this;
    }

    @Nullable
    public LivingEntity getFollowingTarget() {
        if (this.followingEntityId == -1) {
            return null;
        }
        Entity e = level().getEntity(this.followingEntityId);
        if (e == null || !e.isAlive() || !(e instanceof LivingEntity)) {
            return null;
        }
        return (LivingEntity) e;
    }

    @Override
    public void tick() {
        super.tick();

        this.entityAge++;

        if (this.level().isClientSide()) {
            this.tickClient();
        } else {
            if (this.isAmbient() && this.entityAge > 600 && random.nextInt(600) == 0) {
                DamageUtil.attackEntityFrom(this, CommonProxy.DAMAGE_SOURCE_STELLAR, 1F);
            }

            if (this.isAlive()) {
                if (EntityConfig.CONFIG.flareAttackBats.get() && random.nextInt(30) == 0) {
                    Bat closest = EntityUtils.getClosestEntity(this.level(), Bat.class, this.getBoundingBox().inflate(10), Vector3.atEntityCenter(this));
                    if (closest != null) {
                        this.doLightningAttack(closest, 100F);
                    }
                }
                if (EntityConfig.CONFIG.flareAttackPhantoms.get() && random.nextInt(30) == 0) {
                    Phantom closest = EntityUtils.getClosestEntity(this.level(), Phantom.class, this.getBoundingBox().inflate(10), Vector3.atEntityCenter(this));
                    if (closest != null) {
                        this.doLightningAttack(closest, 100F);
                    }
                }

                if (this.isAmbient()) {
                    boolean atTarget = this.currentMoveTarget == null || this.currentMoveTarget.distance(this) < 5.0;
                    if (atTarget) {
                        this.currentMoveTarget = null;
                    }
                    if (this.currentMoveTarget == null && random.nextInt(150) == 0) {
                        BlockPos newTarget = this.blockPosition()
                                .offset(random.nextInt(RANDOM_WANDER_RANGE) * (random.nextBoolean() ? 1 : -1),
                                        random.nextInt(RANDOM_WANDER_RANGE) * (random.nextBoolean() ? 1 : -1),
                                        random.nextInt(RANDOM_WANDER_RANGE) * (random.nextBoolean() ? 1 : -1));

                        if (newTarget.getY() > 1 && newTarget.getY() < 254 && new Vector3(newTarget).distance(this) >= 5.0) {
                            MiscUtils.executeWithChunk(this.level(), newTarget, () -> {
                                this.currentMoveTarget = new Vector3(newTarget);
                            });
                        }
                    }
                } else if (this.getTarget() != null) {
                    if (!this.getTarget().isAlive() || (this.getFollowingTarget() != null && this.getFollowingTarget().distanceTo(this) > 30.0F)) {
                        this.setTarget(null);
                    } else {
                        Vector3 newTarget = Vector3.atEntityCenter(this.getTarget()).addY(1.5F);

                        if (newTarget.getY() > 1 && newTarget.getY() < 254 && newTarget.distance(this) >= 3.0) {
                            this.currentMoveTarget = newTarget;
                        } else {
                            this.currentMoveTarget = null;
                        }
                    }
                } else if (this.followingEntityId != -1) {
                    LivingEntity following = this.getFollowingTarget();
                    if (following == null) {
                        DamageUtil.attackEntityFrom(this, CommonProxy.DAMAGE_SOURCE_STELLAR, 1F);
                    } else {
                        MantleEffectBootes effect = ItemMantle.getEffect(following, ConstellationsAS.bootes);
                        if (effect == null) {
                            DamageUtil.attackEntityFrom(this, CommonProxy.DAMAGE_SOURCE_STELLAR, 1F);
                            return;
                        }

                        if (this.getTarget() != null && !this.getTarget().isAlive()){
                            this.setTarget(null);
                        }
                        if(this.getTarget() == null) {
                            Vector3 newTarget = Vector3.atEntityCenter(following).addY(2.5F);

                            if (newTarget.distance(this) >= 2.0) {
                                this.currentMoveTarget = newTarget;
                            } else {
                                this.currentMoveTarget = null;
                            }
                        }
                    }
                } else {
                    DamageUtil.attackEntityFrom(this, CommonProxy.DAMAGE_SOURCE_STELLAR, 1F);
                    return;
                }

                LivingEntity target = this.getTarget();
                if (target != null && target.isAlive() && target.distanceTo(this) < 10 && random.nextInt(40) == 0) {
                    DamageUtil.shotgunAttack(target, e -> this.doLightningAttack(e, 2F + random.nextFloat() * 2F));
                }

                this.doMovement();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        if (this.texClientSprite == null) {
            this.texClientSprite = Effects.of(EffectTemplatesAS.FACING_SPRITE)
                    .spawn(Vector3.atEntityCorner(this).addY(this.getBbHeight() / 2));
        }

        if (random.nextBoolean()) {
            FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.atEntityCorner(this)
                            .add(random.nextFloat() * 0.2 * (random.nextBoolean() ? 1 : -1),
                                    this.getBbHeight() / 2 + random.nextFloat() * 0.2 * (random.nextBoolean() ? 1 : -1),
                                    random.nextFloat() * 0.2 * (random.nextBoolean() ? 1 : -1)))
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.15F + random.nextFloat() * 0.1F);
            if (random.nextBoolean()) {
                p.color(VFXColorFunction.WHITE);
            }
        }
    }

    private void doLightningAttack(LivingEntity target, float damage) {
        DamageUtil.attackEntityFrom(target, CommonProxy.DAMAGE_SOURCE_STELLAR, damage);
        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.LIGHTNING)
                .addData(buf -> {
                    ByteBufUtils.writeVector(buf, Vector3.atEntityCorner(this).addY(getBbHeight() / 2F));
                    ByteBufUtils.writeVector(buf, Vector3.atEntityCorner(target).addY(target.getBbHeight() / 2F));
                    buf.writeInt(ColorsAS.EFFECT_LIGHTNING.getRGB());
                });
        PacketChannel.CHANNEL.send(PacketDistributor.NEAR.with(() -> PacketChannel.pointFromPos(this.level(), new Vec3(this.getX(), this.getY(), this.getZ()), 32)), pkt);
    }

    private void doMovement() {
        if (this.currentMoveTarget != null) {
            Vec3 motion = this.getDeltaMovement();
            double motionX = (Math.signum(this.currentMoveTarget.getX() - this.getX()) * 0.5D - motion.x()) * (this.isAmbient() ? 0.01D : 0.025D);
            double motionY = (Math.signum(this.currentMoveTarget.getY() - this.getY()) * 0.7D - motion.y()) * (this.isAmbient() ? 0.01D : 0.025D);
            double motionZ = (Math.signum(this.currentMoveTarget.getZ() - this.getZ()) * 0.5D - motion.z()) * (this.isAmbient() ? 0.01D : 0.025D);
            this.setDeltaMovement(motion.add(motionX, motionY, motionZ));
            this.zza = 0.2F;
        }
    }

    @Override
    public void push(Entity entity) {
        if(!(entity instanceof Player)) {
            super.push(entity);
        }
    }

    @Override
    public boolean hurt(DamageSource damageSrc, float damageAmount) {
        boolean result = super.hurt(damageSrc, damageAmount);
        this.setHealth(0F);
        return result;
    }

    public boolean checkSpawn(LevelAccessor level, MobSpawnType spawnType) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void tickDeath() {
        this.discard();

        if (this.level().isClientSide()) {
            this.tickClientDeathEffects();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickClientDeathEffects() {
        if (this.texClientSprite != null) {
            ((FXFacingSprite) this.texClientSprite).requestRemoval();
        }

        List<Vector3> posList = MiscUtils.getCirclePositions(
                Vector3.atEntityCorner(this).addY(this.getBbHeight() / 2),
                Vector3.positiveYRandom(),
                0.3,
                10);
        posList.addAll(MiscUtils.getCirclePositions(
                Vector3.atEntityCorner(this).addY(this.getBbHeight() / 2),
                Vector3.positiveYRandom(),
                0.8,
                20));

        posList.forEach(pos -> {
            FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos.add(Vector3.random().multiply(0.45F)))
                    .setScaleMultiplier(0.1F + random.nextFloat() * 0.25F)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setMaxAge(30 + random.nextInt(40));
            if (random.nextBoolean()) {
                p.color(VFXColorFunction.WHITE);
            }
        });

        for (int i = 0; i < 10; i++) {
            FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.atEntityCorner(this)
                            .add(random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1),
                                    this.getBbHeight() / 2 + random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1),
                                    random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1)))
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setMotion(Vector3.random().multiply(0.05F))
                    .setScaleMultiplier(0.25F + random.nextFloat() * 0.1F)
                    .setMaxAge(40 + random.nextInt(40));
            if (random.nextBoolean()) {
                p.color(VFXColorFunction.WHITE);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putInt("AS_entityAge", this.entityAge);
        compound.putBoolean("AS_ambient", this.ambient);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        this.entityAge = compound.getInt("AS_entityAge");
        this.ambient = compound.getBoolean("AS_ambient");
    }
}
