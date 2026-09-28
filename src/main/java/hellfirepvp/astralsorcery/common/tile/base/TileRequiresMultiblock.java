package hellfirepvp.astralsorcery.common.tile.base;

import hellfirepvp.astralsorcery.common.structure.types.StructureType;

import javax.annotation.Nullable;

public interface TileRequiresMultiblock {
    @Nullable
    StructureType getRequiredStructureType();
}