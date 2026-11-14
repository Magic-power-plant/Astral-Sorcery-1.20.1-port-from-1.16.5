/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.auxiliary.combat;
import hellfirepvp.astralsorcery.common.util.damage.DamageUtil;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.world.SkyCollectionHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CelestialStrike
 * Created by HellFirePvP
 * Date: 04.04.2020 / 13:05
 */
public class CelestialStrike {

    private static final AABB EMPTY = new AABB(0, 0, 0, 0, 0, 0);

    private CelestialStrike() {}

    public static void play(@Nullable LivingEntity attacker, ServerLevel world, Vector3 at, Vector3 displayPosition) {
        double radius = 16D;
        // 1.16.5: getEntitiesWithinAABB + AxisAlignedBB#grow/offset + EntityPredicates.IS_ALIVE
        List<LivingEntity> livingEntities = world.getEntitiesOfClass(LivingEntity.class,
                EMPTY.inflate(radius, radius / 2, radius)
                        .move(at.toBlockPos()), EntitySelector.ENTITY_STILL_ALIVE);
        if (attacker != null) {
            livingEntities.remove(attacker);
        }

        DamageSource ds = CommonProxy.DAMAGE_SOURCE_STELLAR;
        if (attacker != null) {
            // 1.16.5 DamageSource.causeMobDamage/causePlayerDamage; 1.20.1 routes those through Level#damageSources().
            ds = world.damageSources().mobAttack(attacker);
            if (attacker instanceof Player) {
                ds = world.damageSources().playerAttack((Player) attacker);
            }
        }
        float dmg = 25F;
        dmg += SkyCollectionHelper.getSkyNoiseDistribution(world, at.toBlockPos()) * 10F;
        for (LivingEntity living : livingEntities) {
            if ((living instanceof Player) &&
                    (living.isSpectator() || ((Player) living).isCreative() ||
                            (attacker != null && living.isAlliedTo(attacker)))) {
                continue;
            }
            float dstPerc = (float) (Vector3.atEntityCenter(living).distance(at) / radius);
            dstPerc = 1F - Mth.clamp(dstPerc, 0F, 1F);
            float dmgDealt = dstPerc * dmg;
            if (dmgDealt > 0.5) {
                DamageUtil.attackEntityFrom(living, ds, dmgDealt);

                if (attacker != null) {
                    // 1.16.5 EnchantmentHelper.getMaxEnchantmentLevel(Enchantment, Entity)
                    int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, attacker);
                    if (fireAspectLevel > 0 && !living.isOnFire()) {
                        living.setSecondsOnFire(fireAspectLevel * 4);
                    }
                }
            }
        }
        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.CELESTIAL_STRIKE)
                .addData(buf -> {
                    ByteBufUtils.writeVector(buf, displayPosition);
                });
        // 1.16.5 built a TargetPoint from a BlockPos; the ported PacketChannel helper takes a Vec3, so the
        // block corner is used to keep the same integer coordinates.
        PacketChannel.sendToAllAround(pkt, world, Vec3.atLowerCornerOf(at.toBlockPos()), 96);
    }

}
