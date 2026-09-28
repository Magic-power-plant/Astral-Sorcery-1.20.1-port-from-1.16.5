package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crystal.property.PropertyConstellation;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;

import javax.annotation.Nullable;

import java.util.Arrays;

public class CrystalPropertyRegistry {

    public static final CrystalPropertyRegistry INSTANCE = new CrystalPropertyRegistry();

    private CrystalPropertyRegistry() {}

    @Nullable
    public CrystalProperty getConstellationProperty(IConstellation cst) {
        return Arrays.stream(CrystalPropertiesAS.Properties.class.getFields())
                .filter(field -> CrystalProperty.class.isAssignableFrom(field.getType()))
                .map(field -> {
                    try {
                        return (CrystalProperty) field.get(null);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Unable to read crystal property " + field.getName(), e);
                    }
                })
                .filter(prop -> prop instanceof PropertyConstellation constellationProperty
                        && constellationProperty.getConstellation().equals(cst))
                .findFirst()
                .orElse(null);
    }

}
