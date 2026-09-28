package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IMinorConstellation extends IConstellation {
    List<MoonPhase> getShowupMoonPhases(long rSeed);

    default Component getInfoTraitEffect() {
        return Component.translatable(this.getTranslationKey() + ".trait");
    }

    default void affectConstellationEffect(ConstellationEffectProperties properties) {}
}
