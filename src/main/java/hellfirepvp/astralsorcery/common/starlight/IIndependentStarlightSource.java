package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public interface IIndependentStarlightSource {

    Random rand = new Random();

    float produceStarlightTick(ServerLevel world, BlockPos pos);

    @Nullable
    IWeakConstellation getStarlightType();

    default boolean providesAutoLink() {
        return false;
    }

    default <T extends BlockEntity> boolean updateFromTileEntity(T tile) {
        return true;
    }

    void threadedUpdateProximity(BlockPos thisPos, Map<BlockPos, IIndependentStarlightSource> otherSources);

    SourceClassRegistry.SourceProvider getProvider();

    void readFromNBT(CompoundTag compound);

    void writeToNBT(CompoundTag compound);
}
