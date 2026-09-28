package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;

public class StructureTypesAS {

    private StructureTypesAS() {}

    //1.16.5 common/registry/RegistryStructureTypes.java:33-45 builds every structure type from the shared
    //PatternBlockArray instances held by common/lib/StructuresAS
    //(registerAS("pattern_altar_attunement", () -> StructuresAS.STRUCT_ALTAR_ATTUNEMENT) and so on).
    //The suppliers below therefore hand out those very instances instead of constructing a fresh pattern on
    //every StructureType#getStructure()/#matches call; StructuresAS is only class-loaded when one of these
    //lambdas first runs, so pattern construction stays as lazy as it was before.
    public static final StructureType EMPTY = new StructureType(AstralSorcery.locate("empty"), () -> StructuresAS.EMPTY);
    public static final StructureType PTYPE_ALTAR_ATTUNEMENT = new StructureType(AstralSorcery.locate("pattern_altar_attunement"), () -> StructuresAS.STRUCT_ALTAR_ATTUNEMENT);
    public static final StructureType PTYPE_ALTAR_CONSTELLATION = new StructureType(AstralSorcery.locate("pattern_altar_constellation"), () -> StructuresAS.STRUCT_ALTAR_CONSTELLATION);
    public static final StructureType PTYPE_ALTAR_TRAIT = new StructureType(AstralSorcery.locate("pattern_altar_trait"), () -> StructuresAS.STRUCT_ALTAR_TRAIT);
    public static final StructureType PTYPE_RITUAL_PEDESTAL = new StructureType(AstralSorcery.locate("pattern_ritual_pedestal"), () -> StructuresAS.STRUCT_RITUAL_PEDESTAL);
    public static final StructureType PTYPE_INFUSER = new StructureType(AstralSorcery.locate("pattern_infuser"), () -> StructuresAS.STRUCT_INFUSER);
    public static final StructureType PTYPE_ENHANCED_COLLECTOR_CRYSTAL = new StructureType(AstralSorcery.locate("pattern_enhanced_collector_crystal"), () -> StructuresAS.STRUCT_ENHANCED_COLLECTOR_CRYSTAL);
    public static final StructureType PTYPE_SPECTRAL_RELAY = new StructureType(AstralSorcery.locate("pattern_spectral_relay"), () -> StructuresAS.STRUCT_SPECTRAL_RELAY);
    public static final StructureType PTYPE_ATTUNEMENT_ALTAR = new StructureType(AstralSorcery.locate("pattern_attunement_altar"), () -> StructuresAS.STRUCT_ATTUNEMENT_ALTAR);
    public static final StructureType PTYPE_CELESTIAL_GATEWAY = new StructureType(AstralSorcery.locate("pattern_celestial_gateway"), () -> StructuresAS.STRUCT_CELESTIAL_GATEWAY);
    public static final StructureType PTYPE_SINGULARITY = new StructureType(AstralSorcery.locate("pattern_singularity"), () -> StructuresAS.STRUCT_SINGULARITY);
    public static final StructureType PTYPE_FOUNTAIN = new StructureType(AstralSorcery.locate("pattern_fountain"), () -> StructuresAS.STRUCT_FOUNTAIN);

}
