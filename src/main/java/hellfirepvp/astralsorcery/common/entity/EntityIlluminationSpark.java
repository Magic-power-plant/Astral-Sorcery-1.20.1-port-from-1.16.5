package hellfirepvp.astralsorcery.common.entity;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.effect.visual.Effects;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.block.util.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public class EntityIlluminationSpark extends ThrowableProjectile {

    public EntityIlluminationSpark(EntityType<EntityIlluminationSpark> type, Level level) {
        super(type, level);
    }

    public EntityIlluminationSpark(EntityType<EntityIlluminationSpark> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level);
    }

    public EntityIlluminationSpark(EntityType<EntityIlluminationSpark> type, LivingEntity thrower, Level level) {
        super(type, thrower, level);
        this.shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), 0F, 0.7F, 0.9F);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            spawnEffects();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnEffects() {
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

    @OnlyIn(Dist.CLIENT)
    private void randomizeColor(FXFacingParticle p) {
        switch (this.level().random.nextInt(3)) {
            case 0:
                p.color(VFXColorFunction.constant(ColorsAS.DYE_YELLOW));
                break;
            case 1:
                p.color(VFXColorFunction.constant(ColorsAS.DYE_ORANGE));
                break;
            case 2:
                p.color(VFXColorFunction.constant(ColorsAS.DYE_WHITE));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (level().isClientSide()) {
            return;
        }
        if (!(result instanceof BlockHitResult) || !(this.getOwner() instanceof Player)) {
            discard();
            return;
        }
        Player player = (Player) this.getOwner();
        BlockHitResult brtr = (BlockHitResult) result;

        BlockPlaceContext bCtx = new BlockPlaceContext(new UseOnContext(level(), player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, brtr));

        BlockPos pos = bCtx.getClickedPos();
        if (!BlockUtils.isReplaceable(level(), pos)) {
            pos = pos.relative(bCtx.getClickedFace());
        }

        if (!ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(level().dimension(), level(), pos), bCtx.getClickedFace())) {
            level().setBlockAndUpdate(pos, BlocksAS.FLARE_LIGHT.get().defaultBlockState());
        }
        discard();
    }
}
