package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.effect.*;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;

import static hellfirepvp.astralsorcery.common.lib.MantleEffectsAS.*;

public class RegistryMantleEffects {

    private static final List<MantleEffect> REGISTERED_EFFECTS = new ArrayList<>();

    private RegistryMantleEffects() {}

    public static void init() {
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
        for (MantleEffect effect : REGISTERED_EFFECTS) {
            effect.attachEventListeners(bus);
        }
    }

    private static <T extends MantleEffect> T register(T effect) {
        REGISTERED_EFFECTS.add(effect);
        return effect;
    }
}
