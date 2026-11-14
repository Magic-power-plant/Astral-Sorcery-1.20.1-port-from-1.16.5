/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.damage;

import hellfirepvp.astralsorcery.common.util.damage.DamageSourceUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DamageUtil
 * Created by HellFirePvP
 * Date: 25.08.2019 / 00:47
 */
public class DamageUtil {

    public static boolean attackEntityFrom(@Nonnull Entity attacked, @Nonnull DamageSource type, float amount) {
        // 1.16.5 Entity#attackEntityFrom -> 1.20.1 Entity#hurt
        return attacked.hurt(type, amount);
    }

    public static boolean attackEntityFrom(@Nonnull Entity attacked, @Nonnull DamageSource type, float amount, @Nullable Entity newSource) {
        DamageSource newType = DamageSourceUtil.withEntityDirect(type, newSource);
        return attackEntityFrom(attacked, newType != null ? newType : type, amount);
    }

    public static <T extends LivingEntity> void shotgunAttack(T targeted, Consumer<T> fn) {
        // 1.16.5 LivingEntity#hurtResistantTime -> 1.20.1 Entity#invulnerableTime
        int hurtTime = targeted.invulnerableTime;
        targeted.invulnerableTime = 0;
        try {
            fn.accept(targeted);
        } finally {
            targeted.invulnerableTime = hurtTime;
        }
    }
}
