package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffectHelper;
import hellfirepvp.astralsorcery.common.constellation.mantle.effect.*;
import net.minecraftforge.eventbus.api.IEventBus;


import static hellfirepvp.astralsorcery.common.lib.MantleEffectsAS.*;

public class RegistryMantleEffects {

    private static boolean initialized;

    private RegistryMantleEffects() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        AEVITAS = register(new MantleEffectAevitas());
        ARMARA = register(new MantleEffectArmara());
        BOOTES = register(new MantleEffectBootes());
        DISCIDIA = register(new MantleEffectDiscidia());
        EVORSIO = register(new MantleEffectEvorsio());
        FORNAX = register(new MantleEffectFornax());
        HOROLOGIUM = register(new MantleEffectHorologium());
        LUCERNA = register(new MantleEffectLucerna());
        MINERALIS = register(new MantleEffectMineralis());
        OCTANS = register(new MantleEffectOctans());
        PELOTRIO = register(new MantleEffectPelotrio());
        VICIO = register(new MantleEffectVicio());
    }

    public static void attachEventListeners(IEventBus bus) {
        for (MantleEffect effect : RegistriesAS.MANTLE_EFFECTS.getValues()) {
            effect.attachEventListeners(bus);
        }
    }

    private static <T extends MantleEffect> T register(T effect) {
        MantleEffectHelper.register(effect.getAssociatedConstellation(), effect);
        return effect;
    }
}
