package hellfirepvp.astralsorcery.api;

import hellfirepvp.astralsorcery.common.blockentity.base.network.ReceiverBlockEntity;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Stateful base class for block entities that receive starlight network nodes. */
public abstract class StarlightReceiverBlockEntity<T extends ITransmissionReceiver> extends ReceiverBlockEntity<T> {

    protected StarlightReceiverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
