package hellfirepvp.astralsorcery.api;

import hellfirepvp.astralsorcery.common.blockentity.base.network.SourceBlockEntity;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Stateful base class for block entities that provide starlight network nodes. */
public abstract class StarlightSourceBlockEntity<T extends ITransmissionSource> extends SourceBlockEntity<T> {

    protected StarlightSourceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
