/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.structure.types.StructureType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static hellfirepvp.astralsorcery.common.lib.StructureTypesAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryStructureTypes
 * Created by HellFirePvP
 * Date: 30.05.2019 / 15:12
 */
// Port notes for 1.16.5 common/registry/RegistryStructureTypes.java (58 lines):
//
// 1.16.5 did two things per line: it *created* the StructureType (registerAS(name, supplier), ref:48-50) and it
// *registered* it into RegistriesAS.REGISTRY_STRUCTURE_TYPES through the registry primer (ref:52-56).
// In the 1.20.1 tree the creation half already lives in common/lib/StructureTypesAS, with the same twelve
// registry names and the same Supplier<PatternBlockArray> targets (() -> StructuresAS.STRUCT_*), so constructing
// them again here would hand out a second set of StructureType instances - the tiles compare against the
// StructureTypesAS constants by identity (e.g. common/tile/TileRitualPedestal#getStructureType), so the two sets
// must not diverge. Only the registration half is kept here, in the reference's order, over the constants.
//
//   AstralSorcery.getProxy().getRegistryPrimer().register(type) (ref:54)
//                          -> the registry primer and IForgeRegistryEntry are gone in 1.19+. A custom Forge
//                             registry is filled from RegisterEvent, so init() collects the types and
//                             RegistryRegistries#registerStructureTypes flushes them into
//                             RegistriesAS.REGISTRY_STRUCTURE_TYPES when that registry's key comes up - the same
//                             shape 1.16.5 PrimerEventHandler used for its
//                             RegistryEvent.Register<StructureType> listener.
//   StructureType(ResourceLocation, Supplier<BlockArray>) (ref:53)
//                          -> common/structure/types/StructureType takes a Supplier<PatternBlockArray>; the
//                             ObserverLib BlockArray/PatternBlockArray pair is embedded at
//                             common/structure/match/util/PatternBlockArray (ObserverLib is source-only here).
//
// Reading the StructureTypesAS constants does not class-load StructuresAS: the patterns sit behind the
// suppliers, so registration stays as lazy as 1.16.5's was.
public class RegistryStructureTypes {

    private static final List<StructureType> STRUCTURE_TYPES = new ArrayList<>();

    private RegistryStructureTypes() {}

    public static void init() {
        STRUCTURE_TYPES.clear();

        register(EMPTY);

        register(PTYPE_ALTAR_ATTUNEMENT);
        register(PTYPE_ALTAR_CONSTELLATION);
        register(PTYPE_ALTAR_TRAIT);
        register(PTYPE_RITUAL_PEDESTAL);
        register(PTYPE_INFUSER);
        register(PTYPE_ENHANCED_COLLECTOR_CRYSTAL);
        register(PTYPE_SPECTRAL_RELAY);
        register(PTYPE_ATTUNEMENT_ALTAR);
        register(PTYPE_CELESTIAL_GATEWAY);
        register(PTYPE_SINGULARITY);
        register(PTYPE_FOUNTAIN);
    }

    public static Collection<StructureType> getStructureTypes() {
        return Collections.unmodifiableList(STRUCTURE_TYPES);
    }

    private static StructureType register(StructureType type) {
        STRUCTURE_TYPES.add(type);
        return type;
    }
}
