/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ConstellationEffectEntityCollect;
import hellfirepvp.astralsorcery.common.data.config.registry.TechnicalEntityRegistry;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.blockentity.RitualPedestalBlockEntity;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.damage.DamageSourceUtil;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectDiscidia
 * Created by HellFirePvP
 * Date: 23.11.2019 / 21:35
 */
public class CEffectDiscidia extends ConstellationEffectEntityCollect<LivingEntity> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("discidia");
    public static DiscidiaConfig CONFIG = new DiscidiaConfig();

    public CEffectDiscidia(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.discidia, LivingEntity.class,
                entity -> entity.isAlive() && TechnicalEntityRegistry.INSTANCE.canAffect(entity));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level world, BlockPos pos, RitualPedestalBlockEntity pedestal, float alphaMultiplier, boolean extended) {
        Vector3 playAt = new Vector3(pos).add(0.5, 0.5, 0.5);
        //1.16.5: TileEntity#getPos -> 1.20.1: BlockEntity#getBlockPos
        if (pos.equals(pedestal.getBlockPos())) {
            playAt.add(
                    rand.nextFloat() * 0.1 * (rand.nextBoolean() ? 1 : -1),
                    rand.nextFloat() * 5,
                    rand.nextFloat() * 0.1 * (rand.nextBoolean() ? 1 : -1));
        }
        Vector3 motion = Vector3.random().setY(0).multiply(0.05);

        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(playAt)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .motion(VFXMotionController.decelerate(() -> motion))
                .color(VFXColorFunction.constant(ColorsAS.CONSTELLATION_DISCIDIA))
                .setScaleMultiplier(0.4F)
                .setMaxAge(35);
    }

    @Override
    public boolean playEffect(Level world, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        boolean didEffect = false;

        float damage = CONFIG.damage.get().floatValue(); //Randomize?..
        Player owner = this.getOwningPlayerInWorld(world, pos);
        DamageSource src = owner == null ? CommonProxy.DAMAGE_SOURCE_STELLAR :
                DamageSourceUtil.withEntityDirect(CommonProxy.DAMAGE_SOURCE_STELLAR, owner);
        List<LivingEntity> entities = this.collectEntities(world, pos, properties);
        for (LivingEntity entity : entities) {
            if (rand.nextInt(6) != 0) {
                continue;
            }
            //1.16.5: Entity#getClassification(boolean) survives as Forge's IForgeEntity#getClassification(boolean);
            //EntityClassification -> MobCategory. 1.16.5 MobEntity -> 1.20.1 Mob, Effects.RESISTANCE ->
            //MobEffects.DAMAGE_RESISTANCE.
            if (properties.isCorrupted() && entity instanceof Mob && entity.getClassification(false) == MobCategory.MONSTER) {
                entity.heal(damage);
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 1));
            } else {
                if (entity instanceof Player) {
                    continue;
                }
                if (entity.equals(owner)) {
                    continue;
                }
                DamageUtil.shotgunAttack(entity, e -> DamageUtil.attackEntityFrom(entity, src, damage));
            }
            if (entity instanceof Player) {
                markPlayerAffected((Player) entity);
            }

            didEffect = true;
        }

        return didEffect;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    private static class DiscidiaConfig extends Config {

        private final double defaultDamage = 3D;

        public ForgeConfigSpec.DoubleValue damage;

        public DiscidiaConfig() {
            super("discidia", 10D, 2D);
        }

        @Override
        public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.damage = cfgBuilder
                    .comment("Defines the max. possible damage dealt per damage tick.")
                    .translation(translationKey("damage"))
                    .defineInRange("damage", this.defaultDamage, 0.1, 128.0D);
        }
    }
}
