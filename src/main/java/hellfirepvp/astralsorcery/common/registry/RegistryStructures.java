/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryStructures
 * Created by HellFirePvP
 * Date: 09.07.2019 / 19:36
 */
// Port notes for 1.16.5 common/registry/RegistryStructures.java (53 lines):
//
//   init()                  (1.16.5 ref:30-44) -> the twelve pattern instances it assigned into
//                                                 common/lib/StructuresAS are constructed by StructuresAS itself
//                                                 in the 1.20.1 tree (same classes, same order, same
//                                                 PatternBlockArray(AstralSorcery.key("empty")) for EMPTY), so
//                                                 re-assigning them here would only hand out a second set of
//                                                 patterns - the StructureType suppliers in
//                                                 common/lib/StructureTypesAS point at the StructuresAS fields.
//
//BLOCKED_PORT[AS-REG-16]: register(T) (1.16.5 common/registry/RegistryStructures.java:46-51) cannot be ported.
// Both halves of it are ObserverLib API, and ObserverLib is a source-only reference in this port that must never
// be imported:
//   AstralSorcery.getProxy().getRegistryPrimer().register(struct)   - puts the MatchableStructure into
//       ObserverLib's hellfirepvp.observerlib.common.registry.RegistryStructures (its
//       RegistryEvent.Register<MatchableStructure>, 1.16.5 PrimerEventHandler.java:78/238). The forge registry
//       primer itself is also gone in 1.20.1 (IForgeRegistryEntry deleted in 1.19).
//   new ObserverProviderStructure(struct.getRegistryName()) + register  - registers the ObserverProvider that
//       ObserverLib hands back as a ChangeObserverStructure.
// Nothing in the 1.20.1 tree can consume either registration yet: the two call sites that needed them,
// StructureType#observe (1.16.5 common/structure/types/StructureType.java:52) and TileEntityTick's cached
// ChangeSubscriber (see TODO_PORT[AS-TILE-03] in common/tile/base/TileEntityTick.java), both fall back to running
// MatchableStructure#matches directly, and common/structure/match/structure/MatchableStructure is the embedded
// copy of the ObserverLib interface. Restoring the incremental structure-match cache needs ChangeSubscriber /
// ChangeObserverStructure / ObserverProvider embedded under hellfirepvp.astralsorcery first; that is reported as
// an external change.
//
// This class is therefore intentionally left without members.
public final class RegistryStructures {

    private RegistryStructures() {}
}
