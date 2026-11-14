package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crystal.property.PropertyConstellation;

import javax.annotation.Nullable;

public class CrystalPropertyRegistry {

    public static final CrystalPropertyRegistry INSTANCE = new CrystalPropertyRegistry();

    private CrystalPropertyRegistry() {}

    @Nullable
    public CrystalProperty getConstellationProperty(IConstellation cst) {
        return RegistriesAS.CRYSTAL_PROPERTIES.getValues().stream()
                .filter(prop -> prop instanceof PropertyConstellation constellationProperty
                        && constellationProperty.getConstellation().equals(cst))
                .findFirst()
                .orElse(null);
    }

}
