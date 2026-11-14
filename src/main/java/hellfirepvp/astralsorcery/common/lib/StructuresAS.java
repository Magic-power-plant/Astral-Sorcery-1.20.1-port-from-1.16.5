/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.structure.*;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructuresAS
 * Created by HellFirePvP
 * Date: 09.07.2019 / 19:35
 */
//1.16.5 imports hellfirepvp.observerlib.api.util.PatternBlockArray; the 1.20.1 tree uses the embedded copy
//at common/structure/match/util/PatternBlockArray (ObserverLib is a source-only reference here).
// Shared embedded structure patterns. StructureTypesAS supplies these instances lazily.
//Class initialization stays lazy: StructureTypesAS only touches these fields from inside its
//Supplier<PatternBlockArray> lambdas, so no pattern is built before it is first asked for.
public class StructuresAS {

    private StructuresAS() {}

    public static PatternBlockArray EMPTY = new PatternBlockArray(AstralSorcery.key("empty"));

    public static PatternBlockArray STRUCT_ALTAR_ATTUNEMENT = new PatternAltarAttunement();
    public static PatternBlockArray STRUCT_ALTAR_CONSTELLATION = new PatternAltarConstellation();
    public static PatternBlockArray STRUCT_ALTAR_TRAIT = new PatternAltarTrait();
    public static PatternBlockArray STRUCT_RITUAL_PEDESTAL = new PatternRitualPedestal();
    public static PatternBlockArray STRUCT_INFUSER = new PatternInfuser();
    public static PatternBlockArray STRUCT_ENHANCED_COLLECTOR_CRYSTAL = new PatternEnhancedCollectorCrystal();
    public static PatternBlockArray STRUCT_SPECTRAL_RELAY = new PatternSpectralRelay();
    public static PatternBlockArray STRUCT_ATTUNEMENT_ALTAR = new PatternAttunementAltar();
    public static PatternBlockArray STRUCT_CELESTIAL_GATEWAY = new PatternCelestialGateway();
    public static PatternBlockArray STRUCT_SINGULARITY = new PatternSingularity();
    public static PatternBlockArray STRUCT_FOUNTAIN = new PatternFountain();

}
