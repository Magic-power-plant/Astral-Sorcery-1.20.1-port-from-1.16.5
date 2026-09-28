/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this class at common/util/DamageSourceUtil.java. It is kept in the
// target's common/util/damage/ package because the already-ported tree imports it from here.
package hellfirepvp.astralsorcery.common.util.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DamageSourceUtil
 * Created by HellFirePvP
 * Date: 17.11.2018 / 08:29
 */
public class DamageSourceUtil {

    //BLOCKED_PORT[AS-DMG-01]: 1.16.5 built damage sources from a free-form String id
    // (new DamageSource(String) / new EntityDamageSource(String, Entity) /
    // new IndirectEntityDamageSource(String, Entity, Entity)). In 1.20.1 a DamageSource always carries a
    // Holder<DamageType> from the DAMAGE_TYPE registry, and there is no way to synthesize one from a bare
    // string without a registered (data-driven) damage type. The mod's own types live in
    // common/CommonProxy.java (DAMAGE_SOURCE_BLEED/STELLAR/REFLECT), which is outside this slice, so these
    // three factories cannot be expressed. (1.16.5 common/util/DamageSourceUtil.java:29-39)
    @Nullable
    public static DamageSource newType(@Nonnull String damageType) {
        return null;
    }

    @Nullable
    public static DamageSource withEntityDirect(@Nonnull String damageType, @Nullable Entity source) {
        return null;
    }

    @Nullable
    public static DamageSource withEntityIndirect(@Nonnull String damageType, @Nullable Entity actualSource, @Nullable Entity indirectSource) {
        return null;
    }

    @Nullable
    public static DamageSource withEntityDirect(@Nonnull DamageSource damageType, @Nullable Entity source) {
        return override(damageType, source, null);
    }

    @Nullable
    public static DamageSource withEntityIndirect(@Nonnull DamageSource damageType, @Nullable Entity actualSource, @Nullable Entity indirectSource) {
        return override(damageType, indirectSource, actualSource);
    }

    //BLOCKED_PORT[AS-DMG-02]: 1.16.5 mutated the damage source in place through
    // DamageSource#setFireDamage / #setDamageBypassesArmor (and the copy() helper below carried the whole
    // flag set over to a new instance). 1.20.1 DamageSource is immutable and all of those flags moved into
    // the DamageType/DamageTypeTags data, so a source cannot be re-flagged at runtime. Callers have to pick
    // an appropriately tagged damage type instead.
    // (1.16.5 common/util/DamageSourceUtil.java:52-64 and :101-123)
    @Nullable
    public static DamageSource setToFireDamage(@Nonnull DamageSource src) {
        return null;
    }

    @Nullable
    public static DamageSource setToBypassArmor(@Nonnull DamageSource src) {
        return null;
    }

    @Nullable
    public static DamageSource changeAttribute(@Nonnull DamageSource src, Consumer<DamageSource> update) {
        return overrideWithChanges(src, update);
    }

    //1.16.5 refused to rebuild anything but the three vanilla DamageSource classes, because it had to pick
    //the right constructor. 1.20.1 has a single DamageSource class, so every source can be rebuilt.
    private static boolean mayChangeAttributes(DamageSource src) {
        return true;
    }

    @Nullable
    private static DamageSource overrideWithChanges(@Nonnull DamageSource source, Consumer<DamageSource> run) {
        DamageSource dst = override(source, null, null);
        if (dst != null) {
            run.accept(dst);
        }
        return dst;
    }

    @Nullable
    private static DamageSource override(DamageSource src, @Nullable Entity directSource, @Nullable Entity trueSource) {
        if (!mayChangeAttributes(src)) {
            return null;
        }
        //1.16.5 chose between DamageSource / EntityDamageSource / IndirectEntityDamageSource and copied the
        //flag set over. 1.20.1 keeps (Holder<DamageType>, directEntity, causingEntity, position) on one
        //immutable class, so re-using the type holder carries every attribute across automatically and the
        //old copy(DamageSource, DamageSource) helper is no longer needed.
        // 1.16.5 getImmediateSource() -> getDirectEntity(), getTrueSource() -> getEntity()
        Entity direct = directSource != null ? directSource : src.getDirectEntity();
        Entity causing = trueSource != null ? trueSource : (directSource != null ? directSource : src.getEntity());
        return new DamageSource(src.typeHolder(), direct, causing, src.sourcePositionRaw());
    }

    //Target-only helpers: the ported tree routes mob/player/indirect-magic damage through Level#damageSources()
    //instead of 1.16.5's string-keyed DamageSource constructors.
    public static DamageSource playerAttack(@Nonnull DamageSources sources, @Nullable Player player) {
        return player != null ? sources.playerAttack(player) : sources.generic();
    }

    public static DamageSource mobAttack(@Nonnull DamageSources sources, @Nullable LivingEntity attacker) {
        return attacker != null ? sources.mobAttack(attacker) : sources.generic();
    }

    public static DamageSource indirectMagic(@Nonnull DamageSources sources, @Nullable Entity caster, @Nullable Entity projectile) {
        if (caster == null) caster = projectile;
        return sources.indirectMagic(projectile, caster);
    }

}
