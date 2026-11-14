package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.registry.RegistryConstellations;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffect;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.EngravingEffectsAS;
import hellfirepvp.astralsorcery.common.registry.RegistryEngravingEffects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code IConstellation#getEngravingEffect()} used to {@code return null} unconditionally, while
 * {@link RegistryEngravingEffects#init()} happily built every {@link EngravingEffect} into a private list that
 * nothing read. The only consumer, {@code EngravedStarMap}, therefore got null for every constellation and an
 * engraved star map granted no potion, enchantment or modifier - a whole feature that compiled, registered and
 * did nothing.
 *
 * <p>1.16.5 resolved the effect through {@code ENGRAVING_EFFECTS.getValue(getRegistryName())}; this tree
 * resolves it through the canonical typed registry under the same key. These tests pin that the lookup closes.
 */
class EngravingEffectLookupTest {

    @BeforeAll
    static void init() {
        //Both are idempotent; neither touches a vanilla registry.
        RegistryConstellations.init();
        RegistryEngravingEffects.init();
    }

    @Test
    void constellationsResolveTheirOwnEngravingEffect() {
        assertNotNull(ConstellationsAS.aevitas.getEngravingEffect(),
                "aevitas has an engraving effect registered by RegistryEngravingEffects#init");
        assertNotNull(ConstellationsAS.armara.getEngravingEffect(), "armara");
        assertNotNull(ConstellationsAS.discidia.getEngravingEffect(), "discidia");
        assertNotNull(ConstellationsAS.evorsio.getEngravingEffect(), "evorsio");
        assertNotNull(ConstellationsAS.vicio.getEngravingEffect(), "vicio");
    }

    @Test
    void theResolvedEffectIsTheSameInstanceRegistryEngravingEffectsBuilt() {
        assertSame(EngravingEffectsAS.AEVITAS, ConstellationsAS.aevitas.getEngravingEffect(),
                "the lookup must return the very instance init() built, not a copy");
        assertSame(EngravingEffectsAS.ARMARA, ConstellationsAS.armara.getEngravingEffect());
        assertSame(EngravingEffectsAS.DISCIDIA, ConstellationsAS.discidia.getEngravingEffect());
    }

    @Test
    void theEffectIsKeyedByTheConstellationRegistryName() {
        EngravingEffect aevitas = ConstellationsAS.aevitas.getEngravingEffect();
        assertEquals(ConstellationsAS.aevitas.getRegistryName(), aevitas.getRegistryName(),
                "the key is the constellation's registry name, exactly as the 1.16.5 forge registry lookup was");
        assertSame(aevitas, EngravingEffect.getEffect(ConstellationsAS.aevitas.getRegistryName()));
    }

    @Test
    void everyRegisteredConstellationResolvesToItsOwnDistinctEffect() {
        //RegistryEngravingEffects#init registers an effect for all 16 constellations, Minor ones included.
        java.util.Map<EngravingEffect, IConstellation> seen = new java.util.IdentityHashMap<>();
        for (IConstellation cst : ConstellationRegistry.getAllConstellations()) {
            EngravingEffect effect = cst.getEngravingEffect();
            assertNotNull(effect, cst.getRegistryName() + " must resolve an engraving effect");
            assertEquals(cst.getRegistryName(), effect.getRegistryName(),
                    cst.getRegistryName() + " must not resolve another constellation's effect");
            IConstellation previous = seen.put(effect, cst);
            assertNull(previous, "each constellation must get its own effect instance, but "
                    + cst.getRegistryName() + " shares one with " + (previous == null ? "" : previous.getRegistryName()));
        }
        assertEquals(16, seen.size(), "all 16 constellations carry an engraving effect");
    }
}
