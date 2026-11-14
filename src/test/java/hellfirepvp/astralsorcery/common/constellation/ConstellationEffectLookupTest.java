package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.registry.RegistryConstellations;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProvider;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.registry.RegistryConstellationEffects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The ritual chain: StarlightReceiverRitualPedestal calls
 * {@code ConstellationEffectRegistry.createInstance(origin, channelingType)}, which resolves the effect through
 * {@code IWeakConstellation#getConstellationEffect()}. If that lookup returns null, every ritual is inert -
 * the pedestal builds, accepts a crystal, shows a constellation and does nothing.
 *
 * <p>1.16.5 resolved it through {@code CONSTELLATION_EFFECTS.getValue(getRegistryName())}; this tree
 * resolves it through the canonical typed registry, populated by
 * {@link RegistryConstellationEffects#init()}. These tests pin that the chain closes for all twelve
 * ritual-capable constellations.
 */
class ConstellationEffectLookupTest {

    @BeforeAll
    static void init() {
        //Both idempotent and free of vanilla-registry access: init() only stores `CEffectX::new` method
        //references, it never constructs a CEffect (that happens in createEffect, which needs a world).
        RegistryConstellations.init();
        RegistryConstellationEffects.init();
    }

    @Test
    void everyRitualCapableConstellationResolvesAnEffectProvider() {
        IWeakConstellation[] ritualCapable = {
                ConstellationsAS.aevitas, ConstellationsAS.armara, ConstellationsAS.bootes,
                ConstellationsAS.discidia, ConstellationsAS.evorsio, ConstellationsAS.fornax,
                ConstellationsAS.horologium, ConstellationsAS.lucerna, ConstellationsAS.mineralis,
                ConstellationsAS.octans, ConstellationsAS.pelotrio, ConstellationsAS.vicio
        };
        for (IWeakConstellation cst : ritualCapable) {
            ConstellationEffectProvider provider = cst.getConstellationEffect();
            assertNotNull(provider, cst.getRegistryName()
                    + " must resolve a ConstellationEffectProvider, or its ritual does nothing");
            assertSame(cst, provider.getConstellation(),
                    cst.getRegistryName() + " must not resolve another constellation's effect provider");
        }
    }

    @Test
    void theProviderIsKeyedByTheConstellationRegistryName() {
        ConstellationEffectProvider aevitas = ConstellationsAS.aevitas.getConstellationEffect();
        assertEquals(ConstellationsAS.aevitas.getRegistryName(), aevitas.getRegistryName(),
                "the key is the constellation registry name, exactly as the 1.16.5 forge registry lookup was");
        assertSame(aevitas, ConstellationEffectProvider.getProvider(ConstellationsAS.aevitas));
    }
}
