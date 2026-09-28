/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertyUsage;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryCrystalPropertyUsages
 * Created by HellFirePvP
 * Date: 20.08.2019 / 21:51
 */
// Port notes for 1.16.5 common/registry/RegistryCrystalPropertyUsages.java (46 lines):
//
// The seven usages and their ids are unchanged (1.16.5 ref:28-34). What was missing from the 1.20.1 tree is the
// second half of every line: 1.16.5 went through createUsage -> registerUsage, and registerUsage handed the
// instance to the registry primer (ref:41-44), which
// common/registry/internal/PrimerEventHandler#registerCrystalUsages (1.16.5
// common/registry/internal/PrimerEventHandler.java:90, 220-222) later flushed into
// RegistriesAS.REGISTRY_CRYSTAL_USAGES. The 1.20.1 version only assigned the static fields, so the registry this
// tree still creates (RegistryRegistries#buildRegistries, REGISTRY_CRYSTAL_USAGES) stayed permanently empty and
// no addon or /dump-style consumer could enumerate the usages.
//
//   AstralSorcery.getProxy().getRegistryPrimer().register(usage)   (1.16.5 ref:42)
//                    -> the registry primer is gone with IForgeRegistryEntry (1.19) and so is
//                       RegistryEvent.Register<PropertyUsage>. The instances are collected here instead and
//                       RegistryRegistries#registerCrystalUsages flushes them from the key-dispatched
//                       RegisterEvent, which is the pattern this slice already uses for StructureType,
//                       AltarRecipeEffect, PerkAttributeType and friends. USAGES is keyed by registry name, like
//                       RegistryPerkAttributeTypes#TYPES, so a re-run of init() cannot produce duplicate keys.
//   PropertyUsage#setRegistryName / getRegistryName
//                    -> PropertyUsage never was an IForgeRegistryEntry subclass in this mod; it takes the
//                       ResourceLocation in its constructor and keeps its own getRegistryName(), which is what
//                       both the registry key and the "crystal.usage.<namespace>.<path>.name" translation key are
//                       built from. Nothing had to change about it.
//
// init() must keep running before the RegisterEvent for REGISTRY_CRYSTAL_USAGES: it does, because
// common/CommonProxy#initialize (common/CommonProxy.java:145) is called from the AstralSorcery constructor
// (AstralSorcery.java:28), i.e. during mod construction, well before any registry event.
public class RegistryCrystalPropertyUsages {

    private static final Map<ResourceLocation, PropertyUsage> USAGES = new LinkedHashMap<>();

    private RegistryCrystalPropertyUsages() {}

    public static void init() {
        USAGES.clear();

        USE_RITUAL_EFFECT = createUsage("ritual.effect");
        USE_RITUAL_RANGE = createUsage("ritual.range");
        USE_COLLECTOR_CRYSTAL = createUsage("collector");
        USE_LENS_TRANSFER = createUsage("lens.transfer");
        USE_LENS_EFFECT = createUsage("lens.effect");
        USE_TOOL_DURABILITY = createUsage("tool.durability");
        USE_TOOL_EFFECTIVENESS = createUsage("tool.effectiveness");
    }

    public static synchronized List<PropertyUsage> getUsages() {
        return Collections.unmodifiableList(new ArrayList<>(USAGES.values()));
    }

    @Nullable
    public static synchronized PropertyUsage get(ResourceLocation key) {
        return USAGES.get(key);
    }

    @SuppressWarnings("unchecked")
    private static <T extends PropertyUsage> T createUsage(String plainName) {
        return (T) registerUsage(new PropertyUsage(AstralSorcery.key(plainName)));
    }

    private static synchronized <T extends PropertyUsage> T registerUsage(T usage) {
        USAGES.put(usage.getRegistryName(), usage);
        return usage;
    }
}
