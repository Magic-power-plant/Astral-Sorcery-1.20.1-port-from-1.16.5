/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProvider;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectHelper;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IWeakConstellation
 * Created by HellFirePvP
 * Date: 03.01.2017 / 13:28
 */
public interface IWeakConstellation extends IConstellation {

    // Domain helpers query the same typed handles before and after Forge publication.
    @Nullable
    default public ConstellationEffectProvider getConstellationEffect() {
        return ConstellationEffectProvider.getProvider(this);
    }

    @Nullable
    default public MantleEffect getMantleEffect() {
        return MantleEffectHelper.getEffect(this);
    }

    default public Component getInfoRitualEffect() {
        return Component.translatable(this.getTranslationKey() + ".ritual");
    }

    default public Component getInfoCorruptedRitualEffect() {
        return Component.translatable(this.getTranslationKey() + ".corruption");
    }

    default public Component getInfoMantleEffect() {
        return Component.translatable(this.getTranslationKey() + ".mantle");
    }

}
