/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.constellation;

import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.constellation.Constellation;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;

import static hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry.register;
import static hellfirepvp.astralsorcery.common.lib.ColorsAS.*;
import static hellfirepvp.astralsorcery.common.lib.ConstellationsAS.*;

/** Built-in weak constellation geometry and behavior, in stable registration order. */
final class WeakConstellations {
    private WeakConstellations() {}

    static void registerEntries() {
        StarLocation sl1, sl2, sl3, sl4, sl5, sl6, sl7, sl8;

        lucerna = new Constellation.Weak("lucerna", CONSTELLATION_LUCERNA);
        sl1 = lucerna.addStar(19, 4);
        sl2 = lucerna.addStar(25, 14);
        sl3 = lucerna.addStar(22, 22);
        sl4 = lucerna.addStar(15, 25);
        sl5 = lucerna.addStar(8, 23);
        sl6 = lucerna.addStar(4, 12);
        lucerna.addConnection(sl1, sl2);
        lucerna.addConnection(sl2, sl3);
        lucerna.addConnection(sl3, sl4);
        lucerna.addConnection(sl4, sl5);
        lucerna.addConnection(sl5, sl6);
        register(lucerna);

        mineralis = new Constellation.Weak("mineralis", CONSTELLATION_MINERALIS);
        sl1 = mineralis.addStar(17, 2);
        sl2 = mineralis.addStar(19, 10);
        sl3 = mineralis.addStar(13, 7);
        sl4 = mineralis.addStar(15, 15);
        sl5 = mineralis.addStar(22, 19);
        sl6 = mineralis.addStar(11, 25);
        sl7 = mineralis.addStar(18, 28);
        mineralis.addConnection(sl1, sl2);
        mineralis.addConnection(sl1, sl3);
        mineralis.addConnection(sl4, sl2);
        mineralis.addConnection(sl4, sl3);
        mineralis.addConnection(sl4, sl5);
        mineralis.addConnection(sl4, sl6);
        mineralis.addConnection(sl7, sl5);
        mineralis.addConnection(sl7, sl6);
        register(mineralis);

        horologium = new Constellation.WeakSpecial("horologium", CONSTELLATION_HOROLOGIUM) {
            @Override
            public boolean doesShowUp(Level level, long day) {
                WorldContext ctx = SkyHandler.getContext(level);
                if (ctx != null) {
                    return ctx.getCelestialEventHandler().getSolarEclipse().isActiveDay();
                }
                return false;
            }

            @Override
            public float getDistribution(Level level, long day, boolean showingUp) {
                return showingUp ? 1F : 0.25F;
            }
        };
        sl1 = horologium.addStar(28, 6);
        sl2 = horologium.addStar(22, 10);
        sl3 = horologium.addStar(16, 6);
        sl4 = horologium.addStar(10, 4);
        sl5 = horologium.addStar(6, 8);
        sl6 = horologium.addStar(3, 27);
        horologium.addConnection(sl1, sl2);
        horologium.addConnection(sl2, sl3);
        horologium.addConnection(sl3, sl4);
        horologium.addConnection(sl4, sl5);
        horologium.addConnection(sl5, sl6);
        register(horologium);

        octans = new Constellation.Weak("octans", CONSTELLATION_OCTANS);
        sl1 = octans.addStar(25, 25);
        sl2 = octans.addStar(17, 5);
        sl3 = octans.addStar(11, 10);
        sl4 = octans.addStar(4, 6);
        octans.addConnection(sl1, sl2);
        octans.addConnection(sl1, sl3);
        octans.addConnection(sl2, sl3);
        octans.addConnection(sl3, sl4);
        register(octans);

        bootes = new Constellation.Weak("bootes", CONSTELLATION_BOOTES);
        sl1 = bootes.addStar(9, 22);
        sl2 = bootes.addStar(3, 14);
        sl3 = bootes.addStar(22, 27);
        sl4 = bootes.addStar(16, 5);
        sl5 = bootes.addStar(26, 3);
        sl6 = bootes.addStar(24, 11);
        bootes.addConnection(sl1, sl2);
        bootes.addConnection(sl1, sl3);
        bootes.addConnection(sl1, sl4);
        bootes.addConnection(sl1, sl6);
        bootes.addConnection(sl4, sl5);
        bootes.addConnection(sl5, sl6);
        register(bootes);

        fornax = new Constellation.Weak("fornax", CONSTELLATION_FORNAX);
        sl1 = fornax.addStar(18, 29);
        sl2 = fornax.addStar(28, 18);
        sl3 = fornax.addStar(21, 13);
        sl4 = fornax.addStar(16, 18);
        sl5 = fornax.addStar(19, 6);
        sl6 = fornax.addStar(13, 2);
        sl7 = fornax.addStar(9, 21);
        sl8 = fornax.addStar(2, 17);
        fornax.addConnection(sl1, sl2);
        fornax.addConnection(sl2, sl3);
        fornax.addConnection(sl3, sl4);
        fornax.addConnection(sl4, sl1);
        fornax.addConnection(sl3, sl5);
        fornax.addConnection(sl5, sl6);
        fornax.addConnection(sl4, sl7);
        fornax.addConnection(sl7, sl8);
        register(fornax);

        pelotrio = new Constellation.WeakSpecial("pelotrio", CONSTELLATION_PELOTRIO) {
            @Override
            public boolean doesShowUp(Level level, long day) {
                MoonPhase phase = MoonPhase.fromWorld(level);
                return phase == MoonPhase.NEW || phase == MoonPhase.FULL;
            }

            @Override
            public float getDistribution(Level level, long day, boolean showingUp) {
                if (showingUp) return 1F;
                MoonPhase current = MoonPhase.fromWorld(level);
                if (current == MoonPhase.WANING_1_2 || current == MoonPhase.WAXING_1_2) {
                    return 0.3F;
                }
                return 0.65F;
            }
        };
        sl1 = pelotrio.addStar(17, 24);
        sl2 = pelotrio.addStar(27, 25);
        sl3 = pelotrio.addStar(22, 8);
        sl4 = pelotrio.addStar(14, 14);
        sl5 = pelotrio.addStar(8, 29);
        sl6 = pelotrio.addStar(3, 8);
        sl7 = pelotrio.addStar(9, 10);
        pelotrio.addConnection(sl1, sl2);
        pelotrio.addConnection(sl2, sl3);
        pelotrio.addConnection(sl3, sl4);
        pelotrio.addConnection(sl4, sl1);
        pelotrio.addConnection(sl1, sl5);
        pelotrio.addConnection(sl5, sl6);
        pelotrio.addConnection(sl6, sl7);
        pelotrio.addConnection(sl7, sl4);
        register(pelotrio);
    }
}
