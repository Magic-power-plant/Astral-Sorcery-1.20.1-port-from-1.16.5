/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.constellation;

import hellfirepvp.astralsorcery.common.constellation.Constellation;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;

import static hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry.register;
import static hellfirepvp.astralsorcery.common.lib.ColorsAS.*;
import static hellfirepvp.astralsorcery.common.lib.ConstellationsAS.*;

/** Built-in major constellation geometry and behavior, in stable registration order. */
final class MajorConstellations {
    private MajorConstellations() {}

    static void registerEntries() {
        StarLocation sl1, sl2, sl3, sl4, sl5, sl6, sl7;

        discidia = new Constellation.Major("discidia", CONSTELLATION_DISCIDIA);
        sl1 = discidia.addStar(9, 25);
        sl2 = discidia.addStar(6, 18);
        sl3 = discidia.addStar(13, 16);
        sl4 = discidia.addStar(15, 9);
        sl5 = discidia.addStar(22, 8);
        sl6 = discidia.addStar(27, 2);
        discidia.addConnection(sl1, sl2);
        discidia.addConnection(sl2, sl3);
        discidia.addConnection(sl1, sl3);
        discidia.addConnection(sl3, sl4);
        discidia.addConnection(sl3, sl5);
        discidia.addConnection(sl4, sl5);
        discidia.addConnection(sl5, sl6);
        register(discidia);

        armara = new Constellation.Major("armara", CONSTELLATION_ARMARA);
        sl1 = armara.addStar(26, 26);
        sl2 = armara.addStar(24, 15);
        sl3 = armara.addStar(16, 22);
        sl4 = armara.addStar(20, 5);
        sl5 = armara.addStar(15, 4);
        sl6 = armara.addStar(8, 12);
        sl7 = armara.addStar(5, 18);
        armara.addConnection(sl1, sl2);
        armara.addConnection(sl2, sl3);
        armara.addConnection(sl1, sl3);
        armara.addConnection(sl2, sl4);
        armara.addConnection(sl4, sl5);
        armara.addConnection(sl5, sl6);
        armara.addConnection(sl3, sl6);
        armara.addConnection(sl6, sl7);
        register(armara);

        vicio = new Constellation.Major("vicio", CONSTELLATION_VICIO);
        sl1 = vicio.addStar(13, 11);
        sl2 = vicio.addStar(26, 10);
        sl3 = vicio.addStar(23, 4);
        sl4 = vicio.addStar(4, 6);
        sl5 = vicio.addStar(5, 20);
        sl6 = vicio.addStar(12, 25);
        vicio.addConnection(sl1, sl2);
        vicio.addConnection(sl2, sl3);
        vicio.addConnection(sl1, sl3);
        vicio.addConnection(sl1, sl4);
        vicio.addConnection(sl4, sl5);
        vicio.addConnection(sl5, sl6);
        vicio.addConnection(sl6, sl1);
        register(vicio);

        aevitas = new Constellation.Major("aevitas", CONSTELLATION_AEVITAS);
        sl1 = aevitas.addStar(13, 4);
        sl2 = aevitas.addStar(15, 18);
        sl3 = aevitas.addStar(10, 27);
        sl4 = aevitas.addStar(27, 24);
        sl5 = aevitas.addStar(24, 19);
        sl6 = aevitas.addStar(25, 10);
        sl7 = aevitas.addStar(3, 6);
        aevitas.addConnection(sl1, sl2);
        aevitas.addConnection(sl2, sl3);
        aevitas.addConnection(sl3, sl4);
        aevitas.addConnection(sl4, sl5);
        aevitas.addConnection(sl5, sl6);
        aevitas.addConnection(sl6, sl1);
        aevitas.addConnection(sl1, sl7);
        register(aevitas);

        evorsio = new Constellation.Major("evorsio", CONSTELLATION_EVORSIO);
        sl1 = evorsio.addStar(27, 17);
        sl2 = evorsio.addStar(19, 23);
        sl3 = evorsio.addStar(25, 27);
        sl4 = evorsio.addStar(22, 12);
        sl5 = evorsio.addStar(13, 3);
        sl6 = evorsio.addStar(16, 11);
        sl7 = evorsio.addStar(6, 5);
        evorsio.addConnection(sl1, sl2);
        evorsio.addConnection(sl1, sl3);
        evorsio.addConnection(sl1, sl4);
        evorsio.addConnection(sl4, sl5);
        evorsio.addConnection(sl4, sl6);
        evorsio.addConnection(sl6, sl7);
        register(evorsio);
    }
}
