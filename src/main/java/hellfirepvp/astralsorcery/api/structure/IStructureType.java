package hellfirepvp.astralsorcery.api.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;

/** Identified structure matcher. */
public interface IStructureType {
    ResourceLocation getRegistryName();
    boolean matches(BlockGetter reader, BlockPos center);
}
