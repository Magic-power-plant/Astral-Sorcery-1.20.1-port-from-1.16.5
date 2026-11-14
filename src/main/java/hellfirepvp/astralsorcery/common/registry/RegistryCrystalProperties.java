package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertySource;
import hellfirepvp.astralsorcery.common.crystal.property.*;
import hellfirepvp.astralsorcery.common.crystal.source.Crystal;
import hellfirepvp.astralsorcery.common.crystal.source.Ritual;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.IndependentCrystalSource;
import hellfirepvp.astralsorcery.common.blockentity.CollectorCrystalBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.RitualPedestalBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.network.StarlightReceiverRitualPedestal;


import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.*;

/** Built-in declarations; canonical storage and Forge publication are owned by RegistriesAS. */
public class RegistryCrystalProperties {

    private static boolean initialized;
    private static boolean defaultAttributesInitialized;

    private RegistryCrystalProperties() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        Properties.PROPERTY_SIZE = registerProperty(new PropertySize());
        Properties.PROPERTY_PURITY = registerProperty(new PropertyPurity());
        Properties.PROPERTY_SHAPE = registerProperty(new PropertyShape());

        Properties.PROPERTY_TOOL_DURABILITY = registerProperty(new PropertyToolDurability());
        Properties.PROPERTY_TOOL_EFFICIENCY = registerProperty(new PropertyToolEfficiency());
        Properties.PROPERTY_RITUAL_EFFECT = registerProperty(new PropertyRitualEffect());
        Properties.PROPERTY_RITUAL_RANGE = registerProperty(new PropertyRitualRange());
        Properties.PROPERTY_COLLECTOR_COLLECTION_RATE = registerProperty(new PropertyCollectionRate());

        Properties.PROPERTY_CST_AEVITAS =    registerProperty(new PropertyConstellation(ConstellationsAS.aevitas));
        Properties.PROPERTY_CST_DISCIDIA =   registerProperty(new PropertyConstellation(ConstellationsAS.discidia));
        Properties.PROPERTY_CST_ARMARA =     registerProperty(new PropertyConstellation(ConstellationsAS.armara));
        Properties.PROPERTY_CST_VICIO =      registerProperty(new PropertyConstellation(ConstellationsAS.vicio));
        Properties.PROPERTY_CST_EVORSIO =    registerProperty(new PropertyConstellation(ConstellationsAS.evorsio));
        Properties.PROPERTY_CST_LUCERNA =    registerProperty(new PropertyConstellation(ConstellationsAS.lucerna));
        Properties.PROPERTY_CST_MINERALIS =  registerProperty(new PropertyConstellation(ConstellationsAS.mineralis));
        Properties.PROPERTY_CST_OCTANS =     registerProperty(new PropertyConstellation(ConstellationsAS.octans));
        Properties.PROPERTY_CST_BOOTES =     registerProperty(new PropertyConstellation(ConstellationsAS.bootes));
        Properties.PROPERTY_CST_HOROLOGIUM = registerProperty(new PropertyConstellation(ConstellationsAS.horologium));
        Properties.PROPERTY_CST_FORNAX =     registerProperty(new PropertyConstellation(ConstellationsAS.fornax));
        Properties.PROPERTY_CST_PELOTRIO =   registerProperty(new PropertyConstellation(ConstellationsAS.pelotrio));
    }

    private static synchronized <T extends CrystalProperty> T registerProperty(T property) {
        RegistriesAS.CRYSTAL_PROPERTIES.register(property);
        return property;
    }

    public static synchronized void initDefaultAttributes() {
        if (defaultAttributesInitialized) return;
        defaultAttributesInitialized = true;
        Sources.SOURCE_RITUAL_PEDESTAL = new PropertySource<Object, Ritual>(AstralSorcery.key("ritual_network")) {
            @Override
            public Ritual createInstance(Object obj) {
                StarlightReceiverRitualPedestal srrp = (StarlightReceiverRitualPedestal) obj;
                return new Ritual(this, srrp.getChannelingType(), srrp.getChannelingTrait());
            }
        };
        Sources.SOURCE_RITUAL_PEDESTAL_BLOCK_ENTITY = new PropertySource<Object, Ritual>(AstralSorcery.key("ritual_tile")) {
            @Override
            public Ritual createInstance(Object obj) {
                RitualPedestalBlockEntity trp = (RitualPedestalBlockEntity) obj;
                return new Ritual(this, trp.getRitualConstellation(), trp.getRitualTrait());
            }
        };
        Sources.SOURCE_COLLECTOR_CRYSTAL = new PropertySource<Object, Crystal>(AstralSorcery.key("crystal_network")) {
            @Override
            public Crystal createInstance(Object obj) {
                IndependentCrystalSource ics = (IndependentCrystalSource) obj;
                return new Crystal(this, ics.getStarlightType());
            }
        };
        Sources.SOURCE_COLLECTOR_CRYSTAL_BLOCK_ENTITY = new PropertySource<Object, Crystal>(AstralSorcery.key("crystal_tile")) {
            @Override
            public Crystal createInstance(Object obj) {
                CollectorCrystalBlockEntity tcc = (CollectorCrystalBlockEntity) obj;
                return new Crystal(this, tcc.getAttunedConstellation());
            }
        };

        CREATIVE_CRYSTAL_TOOL_ATTRIBUTES =
                CrystalAttributes.Builder.newBuilder(false)
                        .addProperty(Properties.PROPERTY_SIZE, 3)
                        .addProperty(Properties.PROPERTY_SHAPE, 3)
                        .addProperty(Properties.PROPERTY_TOOL_DURABILITY, 3)
                        .addProperty(Properties.PROPERTY_TOOL_EFFICIENCY, 3)
                        .build();
        WORLDGEN_SHRINE_COLLECTOR_ATTRIBUTES =
                CrystalAttributes.Builder.newBuilder(false)
                        .addProperty(Properties.PROPERTY_SIZE, 2)
                        .addProperty(Properties.PROPERTY_SHAPE, 2)
                        .addProperty(Properties.PROPERTY_PURITY, 2)
                        .addProperty(Properties.PROPERTY_COLLECTOR_COLLECTION_RATE, 2)
                        .build();

        CREATIVE_ROCK_COLLECTOR_ATTRIBUTES =
                CrystalAttributes.Builder.newBuilder(false)
                        .addProperty(Properties.PROPERTY_SIZE, 3)
                        .addProperty(Properties.PROPERTY_SHAPE, 3)
                        .addProperty(Properties.PROPERTY_PURITY, 2)
                        .addProperty(Properties.PROPERTY_COLLECTOR_COLLECTION_RATE, 3)
                        .build();
        CREATIVE_CELESTIAL_COLLECTOR_ATTRIBUTES =
                CrystalAttributes.Builder.newBuilder(false)
                        .addProperty(Properties.PROPERTY_SIZE, 3)
                        .addProperty(Properties.PROPERTY_SHAPE, 3)
                        .addProperty(Properties.PROPERTY_PURITY, 2)
                        .addProperty(Properties.PROPERTY_COLLECTOR_COLLECTION_RATE, 3)
                        .build();

        LENS_PRISM_CREATIVE_ATTRIBUTES =
                CrystalAttributes.Builder.newBuilder(false)
                        .addProperty(Properties.PROPERTY_PURITY, 2)
                        .addProperty(Properties.PROPERTY_SHAPE, 3)
                        .build();
    }
}
