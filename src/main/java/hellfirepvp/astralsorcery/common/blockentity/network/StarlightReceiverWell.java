package hellfirepvp.astralsorcery.common.blockentity.network;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import hellfirepvp.astralsorcery.common.blockentity.WellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class StarlightReceiverWell extends SimpleTransmissionReceiver<WellBlockEntity> {

    public StarlightReceiverWell(BlockPos thisPos) {
        super(thisPos);
    }

    @Override
    public void onStarlightReceive(Level world, IWeakConstellation type, double amount) {
        WellBlockEntity well = getBlockEntityAtPos(world);
        if (well != null) {
            well.receiveStarlight(amount);
        }
    }

    @Override
    public boolean syncBlockEntityData(Level world, WellBlockEntity blockEntity) {
        return true;
    }

    @Override
    public Class<WellBlockEntity> getBlockEntityClass() {
        return WellBlockEntity.class;
    }

    @Override
    public TransmissionProvider getProvider() {
        return new Provider();
    }

    public static class Provider extends TransmissionProvider {

        @Override
        public IPrismTransmissionNode get() {
            return new StarlightReceiverWell(null);
        }

    }

}
