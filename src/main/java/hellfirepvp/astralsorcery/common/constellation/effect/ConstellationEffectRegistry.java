package hellfirepvp.astralsorcery.common.constellation.effect;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.aoe.*;
import hellfirepvp.astralsorcery.common.data.config.ServerConfig;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;

import javax.annotation.Nullable;

public class ConstellationEffectRegistry {

    public static final String ENTITY_TAG_LUCERNA_SKIP_ENTITY = "skip.spawn.deny";

    public static void addConfigEntries(ServerConfig config) {
        config.addConfigEntry(CEffectAevitas.CONFIG);
        config.addConfigEntry(CEffectArmara.CONFIG);
        config.addConfigEntry(CEffectBootes.CONFIG);
        config.addConfigEntry(CEffectDiscidia.CONFIG);
        config.addConfigEntry(CEffectEvorsio.CONFIG);
        config.addConfigEntry(CEffectFornax.CONFIG);
        config.addConfigEntry(CEffectHorologium.CONFIG);
        config.addConfigEntry(CEffectLucerna.CONFIG);
        config.addConfigEntry(CEffectMineralis.CONFIG);
        config.addConfigEntry(CEffectOctans.CONFIG);
        config.addConfigEntry(CEffectPelotrio.CONFIG);
        config.addConfigEntry(CEffectVicio.CONFIG);
    }

    @Nullable
    public static ConstellationEffect createInstance(@Nullable ILocatable origin, IWeakConstellation constellation) {
        ConstellationEffectProvider effect = constellation.getConstellationEffect();
        if (effect != null) {
            return effect.createEffect(origin);
        }
        return null;
    }
}
