package hellfirepvp.astralsorcery.common.blockentity.base;

import hellfirepvp.astralsorcery.common.structure.types.StructureType;

import javax.annotation.Nullable;

public interface RequiresMultiblockBlockEntity {
    @Nullable
    StructureType getRequiredStructureType();
}