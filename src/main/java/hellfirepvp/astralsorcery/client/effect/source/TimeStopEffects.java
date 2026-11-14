package hellfirepvp.astralsorcery.client.effect.source;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.data.config.registry.BlockEntityAccelerationBlacklistRegistry;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.List;
import java.util.Map;
import java.util.Random;
import hellfirepvp.astralsorcery.common.util.time.TimeStopEffectHelper;
import hellfirepvp.astralsorcery.common.util.time.TimeStopZone;

public final class TimeStopEffects {
    private static final Random rand = new Random();

    @OnlyIn(Dist.CLIENT)
    public static void playEntityParticles(LivingEntity e) {
        double x = e.getX() - e.getBbWidth() / 2F + rand.nextFloat() * e.getBbWidth();
        double y = e.getY() + rand.nextFloat() * e.getBbHeight();
        double z = e.getZ() - e.getBbWidth() / 2F + rand.nextFloat() * e.getBbWidth();
        playParticles(x, y, z);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playEntityParticles(PktPlayEffect ev) {
        Vector3 at = ByteBufUtils.readVector(ev.getExtraData());
        playParticles(at.getX(), at.getY(), at.getZ());
    }

    @OnlyIn(Dist.CLIENT)
    static void playParticles(double x, double y, double z) {
        Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(new Vector3(x, y, z))
                .alpha(VFXAlphaFunction.FADE_OUT)
                .color(VFXColorFunction.WHITE)
                .setScaleMultiplier(0.3F + rand.nextFloat() * 0.5F)
                .setMaxAge(40 + rand.nextInt(20));
    }

    @OnlyIn(Dist.CLIENT)
    public static void playClientTickEffect(TimeStopEffectHelper effect) {
        BlockPos position = effect.getPosition();
        float range = effect.getRange();
        TimeStopZone.EntityTargetController targetController = effect.getTargetController();
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class,
                new AABB(-range, -range, -range, range, range, range).move(position.getX(), position.getY(), position.getZ()),
                e -> e != null && e.isAlive() && targetController.shouldFreezeEntity(e));

        for (LivingEntity e : entities) {
            if (rand.nextInt(3) == 0) {
                playEntityParticles(e);
            }
        }

        int minX = Mth.floor((position.getX() - range) / 16.0D);
        int maxX = Mth.floor((position.getX() + range) / 16.0D);
        int minZ = Mth.floor((position.getZ() - range) / 16.0D);
        int maxZ = Mth.floor((position.getZ() + range) / 16.0D);

        for (int xx = minX; xx <= maxX; ++xx) {
            for (int zz = minZ; zz <= maxZ; ++zz) {
                LevelChunk ch = world.getChunk(xx, zz);
                if (!ch.isEmpty()) {
                    Map<BlockPos, BlockEntity> map = ch.getBlockEntities();
                    for (Map.Entry<BlockPos, BlockEntity> teEntry : map.entrySet()) {
                        BlockEntity te = teEntry.getValue();
                        if (BlockEntityAccelerationBlacklistRegistry.INSTANCE.canBeInfluenced(te) && te.getBlockPos().distSqr(position) <= range * range) {

                            double x = te.getBlockPos().getX() + rand.nextFloat();
                            double y = te.getBlockPos().getY() + rand.nextFloat();
                            double z = te.getBlockPos().getZ() + rand.nextFloat();

                            playParticles(x, y, z);
                        }
                    }
                }
            }
        }

        Vector3 pos;
        for (int i = 0; i < 10; i++) {
            pos = Vector3.random().normalize().multiply(rand.nextFloat() * range).add(position);
            playParticles(pos.getX(), pos.getY(), pos.getZ());
        }

        if (rand.nextInt(4) == 0) {
            Vector3 rand1 = Vector3.random().normalize().multiply(rand.nextFloat() * range).add(position);
            Vector3 rand2 = Vector3.random().normalize().multiply(rand.nextFloat() * range).add(position);
            if (rand1.distance(rand2) > 10) {
                Vector3 dir = rand1.vectorFromHereTo(rand2);
                rand2 = rand1.clone().add(dir.normalize().multiply(10));
            }
            // TODO_PORT[AS-VFX-01]: Lightning effect builder pending API stabilization
            // Effects.of(EffectTemplatesAS.LIGHTNING)
            //         .spawn(rand1)
            //         .makeDefault(rand2)
            //         .color(VFXColorFunction.WHITE);
        }
    }

}
