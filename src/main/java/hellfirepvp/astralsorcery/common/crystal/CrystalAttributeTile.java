package hellfirepvp.astralsorcery.common.crystal;

import javax.annotation.Nullable;

public interface CrystalAttributeTile {

    @Nullable
    CrystalAttributes getAttributes();

    void setAttributes(@Nullable CrystalAttributes attributes);

    default CrystalAttributes getMissingAttributes() {
        return CrystalAttributes.Builder.newBuilder(false).build();
    }
}
