package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.Constellation;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;

import static hellfirepvp.astralsorcery.common.lib.ColorsAS.*;

public class ConstellationsAS {
    private ConstellationsAS() {}

    public static IMajorConstellation discidia;
    public static IMajorConstellation armara;
    public static IMajorConstellation vicio;
    public static IMajorConstellation aevitas;
    public static IMajorConstellation evorsio;

    public static IWeakConstellation mineralis;
    public static IWeakConstellation lucerna;
    public static IWeakConstellation bootes;
    public static IWeakConstellation octans;
    public static IWeakConstellation horologium;
    public static IWeakConstellation fornax;
    public static IWeakConstellation pelotrio;

    public static IMinorConstellation gelu;
    public static IMinorConstellation ulteria;
    public static IMinorConstellation alcara;
    public static IMinorConstellation vorux;

    private static boolean initialized;

    public static void init(IEventBus bus) {
        if (initialized) {
            return;
        }
        initialized = true;
        buildConstellations();
    }

    private static void buildConstellations() {
        StarLocation sl1, sl2, sl3, sl4, sl5, sl6, sl7, sl8;

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

    private static void register(hellfirepvp.astralsorcery.common.constellation.IConstellation constellation) {
        ConstellationRegistry.register(constellation);
    }
}
