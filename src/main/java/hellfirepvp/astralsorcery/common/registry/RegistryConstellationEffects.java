package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffect;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProvider;
import hellfirepvp.astralsorcery.common.constellation.effect.aoe.*;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.block.util.ILocatable;

import java.util.function.Function;

import static hellfirepvp.astralsorcery.common.lib.ConstellationEffectsAS.*;

public class RegistryConstellationEffects {

    private static boolean initialized;

    private RegistryConstellationEffects() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        AEVITAS    = registerProvider(ConstellationsAS.aevitas,    CEffectAevitas::new);
        ARMARA     = registerProvider(ConstellationsAS.armara,     CEffectArmara::new);
        BOOTES     = registerProvider(ConstellationsAS.bootes,     CEffectBootes::new);
        DISCIDIA   = registerProvider(ConstellationsAS.discidia,   CEffectDiscidia::new);
        EVORSIO    = registerProvider(ConstellationsAS.evorsio,    CEffectEvorsio::new);
        FORNAX     = registerProvider(ConstellationsAS.fornax,     CEffectFornax::new);
        HOROLOGIUM = registerProvider(ConstellationsAS.horologium, CEffectHorologium::new);
        LUCERNA    = registerProvider(ConstellationsAS.lucerna,    CEffectLucerna::new);
        MINERALIS  = registerProvider(ConstellationsAS.mineralis,  CEffectMineralis::new);
        OCTANS     = registerProvider(ConstellationsAS.octans,     CEffectOctans::new);
        PELOTRIO   = registerProvider(ConstellationsAS.pelotrio,   CEffectPelotrio::new);
        VICIO      = registerProvider(ConstellationsAS.vicio,      CEffectVicio::new);
    }

    private static ConstellationEffectProvider registerProvider(IWeakConstellation cst, Function<ILocatable, ? extends ConstellationEffect> effectProvider) {
        return ConstellationEffectProvider.register(cst, effectProvider);
    }
}
