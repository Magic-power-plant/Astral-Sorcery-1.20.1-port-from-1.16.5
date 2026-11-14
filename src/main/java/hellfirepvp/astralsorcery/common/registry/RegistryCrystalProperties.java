package hellfirepvp.astralsorcery.common.registry;

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
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.*;

// Restores the registration half of 1.16.5 common/registry/RegistryCrystalProperties.java: every line of its
// init() (1.16.5 ref:40-61) went through registerProperty (ref:127-130), which handed the instance to the
// registry primer for common/registry/internal/PrimerEventHandler#registerCrystalProperties (1.16.5
// common/registry/internal/PrimerEventHandler.java:89, 216-218) to flush into
// RegistriesAS.REGISTRY_CRYSTAL_PROPERTIES. This tree still creates that registry
// (RegistryRegistries#buildRegistries) and still reads it - common/crystal/CrystalAttributes.Attribute#deserialize
// resolves every stored attribute through REGISTRY_CRYSTAL_PROPERTIES#getValue and drops the attribute when the
// lookup misses - but nothing filled it, so every crystal would have lost all of its attributes the moment its
// NBT was read back. The primer and RegistryEvent.Register<CrystalProperty> are gone with IForgeRegistryEntry
// (1.19), so the instances are collected here and RegistryRegistries#registerCrystalProperties flushes them from
// the key-dispatched RegisterEvent, the same way this package already handles StructureType, AltarRecipeEffect
// and the perk registries.
public class RegistryCrystalProperties {

    private static final Map<ResourceLocation, CrystalProperty> PROPERTIES = new LinkedHashMap<>();

    private RegistryCrystalProperties() {}

    public static void init() {
        PROPERTIES.clear();

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

    public static synchronized List<CrystalProperty> getProperties() {
        return Collections.unmodifiableList(new ArrayList<>(PROPERTIES.values()));
    }

    @Nullable
    public static synchronized CrystalProperty get(ResourceLocation key) {
        return PROPERTIES.get(key);
    }

    private static synchronized <T extends CrystalProperty> T registerProperty(T property) {
        PROPERTIES.put(property.getRegistryName(), property);
        return property;
    }

    public static void initDefaultAttributes() {
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
