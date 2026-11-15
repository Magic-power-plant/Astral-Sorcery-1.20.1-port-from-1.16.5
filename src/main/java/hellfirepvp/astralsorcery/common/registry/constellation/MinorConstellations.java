/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.constellation;

import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.Constellation;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;

import static hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry.register;
import static hellfirepvp.astralsorcery.common.lib.ColorsAS.*;
import static hellfirepvp.astralsorcery.common.lib.ConstellationsAS.*;

/** Built-in minor constellation geometry and behavior, in stable registration order. */
final class MinorConstellations {
    private MinorConstellations() {}

    static void registerEntries() {
        StarLocation sl1, sl2, sl3, sl4, sl5, sl6, sl7;

        //The four Minor constellations are ritual *traits*: TileRitualPedestal#... and
        //StarlightReceiverRitualPedestal both call getRitualTrait().affectConstellationEffect(properties) on the
        //properties of the ritual they are slotted into, and CEffectArmara does the same for its own trait.
        //IMinorConstellation#affectConstellationEffect defaults to a no-op, so building these as plain
        //Constellation.Minor left all four traits with literally no effect on any ritual.
        //(1.16.5 common/registry/RegistryConstellations.java:380-471)
        gelu = new Constellation.Minor("gelu", CONSTELLATION_GELU, MoonPhase.NEW, MoonPhase.WAXING_1_4, MoonPhase.WAXING_1_2) {
            @Override
            public void affectConstellationEffect(ConstellationEffectProperties properties) {
                properties.multiplyPotency(1.4F);
                properties.multiplySize(2);
            }
        };
        sl1 = gelu.addStar(21, 28);
        sl2 = gelu.addStar(27, 13);
        sl3 = gelu.addStar(29, 4);
        sl4 = gelu.addStar(12, 14);
        sl5 = gelu.addStar(3, 12);
        sl6 = gelu.addStar(9, 21);
        gelu.addConnection(sl1, sl2);
        gelu.addConnection(sl2, sl3);
        gelu.addConnection(sl3, sl4);
        gelu.addConnection(sl4, sl5);
        gelu.addConnection(sl5, sl6);
        gelu.addConnection(sl6, sl1);
        register(gelu);

        ulteria = new Constellation.Minor("ulteria", CONSTELLATION_ULTERIA, MoonPhase.WANING_1_2, MoonPhase.WANING_3_4, MoonPhase.NEW) {
            @Override
            public void affectConstellationEffect(ConstellationEffectProperties properties) {
                properties.multiplyEffectAmplifier(2F);
                properties.multiplyPotency(0.5F);
                properties.multiplySize(0.5F);
            }
        };
        sl1 = ulteria.addStar(4, 28);
        sl2 = ulteria.addStar(7, 22);
        sl3 = ulteria.addStar(9, 14);
        sl4 = ulteria.addStar(21, 15);
        sl5 = ulteria.addStar(15, 8);
        sl6 = ulteria.addStar(24, 3);
        ulteria.addConnection(sl1, sl2);
        ulteria.addConnection(sl2, sl3);
        ulteria.addConnection(sl3, sl4);
        ulteria.addConnection(sl3, sl5);
        ulteria.addConnection(sl4, sl5);
        ulteria.addConnection(sl5, sl6);
        register(ulteria);

        alcara = new Constellation.Minor("alcara", CONSTELLATION_ALCARA, MoonPhase.WANING_1_2, MoonPhase.WAXING_1_2) {
            @Override
            public void affectConstellationEffect(ConstellationEffectProperties properties) {
                properties.setCorrupted(true);
                properties.multiplySize(1.2F);
            }
        };
        sl1 = alcara.addStar(24, 27);
        sl2 = alcara.addStar(17, 16);
        sl3 = alcara.addStar(12, 9);
        sl4 = alcara.addStar(23, 9);
        sl5 = alcara.addStar(21, 2);
        sl6 = alcara.addStar(10, 21);
        sl7 = alcara.addStar(3, 20);
        alcara.addConnection(sl1, sl2);
        alcara.addConnection(sl2, sl3);
        alcara.addConnection(sl2, sl4);
        alcara.addConnection(sl4, sl5);
        alcara.addConnection(sl5, sl3);
        alcara.addConnection(sl2, sl6);
        alcara.addConnection(sl6, sl7);
        alcara.addConnection(sl7, sl3);
        register(alcara);

        vorux = new Constellation.Minor("vorux", CONSTELLATION_VORUX, MoonPhase.FULL, MoonPhase.WAXING_3_4, MoonPhase.WANING_3_4) {
            @Override
            public void affectConstellationEffect(ConstellationEffectProperties properties) {
                properties.multiplyEffectAmplifier(0.8F);
                properties.multiplyPotency(5F);
                properties.multiplySize(1.2F);
            }
        };
        sl1 = vorux.addStar(7, 29);
        sl2 = vorux.addStar(15, 12);
        sl3 = vorux.addStar(23, 7);
        sl4 = vorux.addStar(28, 14);
        sl5 = vorux.addStar(3, 6);
        vorux.addConnection(sl1, sl2);
        vorux.addConnection(sl2, sl3);
        vorux.addConnection(sl3, sl4);
        vorux.addConnection(sl2, sl5);
        register(vorux);
    }
}
