package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertySource;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertyUsage;
import hellfirepvp.astralsorcery.common.crystal.source.Crystal;
import hellfirepvp.astralsorcery.common.crystal.source.Ritual;

public class CrystalPropertiesAS {

    private CrystalPropertiesAS() {}

    public static CrystalAttributes CREATIVE_CRYSTAL_TOOL_ATTRIBUTES;
    public static CrystalAttributes WORLDGEN_SHRINE_COLLECTOR_ATTRIBUTES;

    public static CrystalAttributes CREATIVE_ROCK_COLLECTOR_ATTRIBUTES;
    public static CrystalAttributes CREATIVE_CELESTIAL_COLLECTOR_ATTRIBUTES;

    public static CrystalAttributes LENS_PRISM_CREATIVE_ATTRIBUTES;

    public static class Properties {

        public static CrystalProperty PROPERTY_SIZE;
        public static CrystalProperty PROPERTY_PURITY;
        public static CrystalProperty PROPERTY_SHAPE;

        public static CrystalProperty PROPERTY_TOOL_DURABILITY;
        public static CrystalProperty PROPERTY_TOOL_EFFICIENCY;

        public static CrystalProperty PROPERTY_RITUAL_RANGE;
        public static CrystalProperty PROPERTY_RITUAL_EFFECT;
        public static CrystalProperty PROPERTY_COLLECTOR_COLLECTION_RATE;

        public static CrystalProperty PROPERTY_CST_DISCIDIA;
        public static CrystalProperty PROPERTY_CST_ARMARA;
        public static CrystalProperty PROPERTY_CST_AEVITAS;
        public static CrystalProperty PROPERTY_CST_VICIO;
        public static CrystalProperty PROPERTY_CST_EVORSIO;
        public static CrystalProperty PROPERTY_CST_MINERALIS;
        public static CrystalProperty PROPERTY_CST_LUCERNA;
        public static CrystalProperty PROPERTY_CST_BOOTES;
        public static CrystalProperty PROPERTY_CST_OCTANS;
        public static CrystalProperty PROPERTY_CST_HOROLOGIUM;
        public static CrystalProperty PROPERTY_CST_FORNAX;
        public static CrystalProperty PROPERTY_CST_PELOTRIO;
    }

    public static class Usages {

        public static PropertyUsage USE_RITUAL_EFFECT;
        public static PropertyUsage USE_RITUAL_RANGE;
        public static PropertyUsage USE_COLLECTOR_CRYSTAL;
        public static PropertyUsage USE_LENS_TRANSFER;
        public static PropertyUsage USE_LENS_EFFECT;
        public static PropertyUsage USE_TOOL_DURABILITY;
        public static PropertyUsage USE_TOOL_EFFECTIVENESS;
    }

    public static class Sources {

        public static PropertySource<Object, Crystal> SOURCE_COLLECTOR_CRYSTAL;
        public static PropertySource<Object, Crystal> SOURCE_COLLECTOR_CRYSTAL_BLOCK_ENTITY;
        public static PropertySource<Object, Ritual> SOURCE_RITUAL_PEDESTAL;
        public static PropertySource<Object, Ritual> SOURCE_RITUAL_PEDESTAL_BLOCK_ENTITY;
    }
}
