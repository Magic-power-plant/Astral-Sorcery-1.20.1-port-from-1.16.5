package hellfirepvp.astralsorcery.common.blockentity.network;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import hellfirepvp.astralsorcery.common.blockentity.treebeacon.TreeBeaconBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class StarlightReceiverTreeBeacon extends SimpleTransmissionReceiver<TreeBeaconBlockEntity> {

    public StarlightReceiverTreeBeacon(BlockPos thisPos) {
        super(thisPos);
    }

    @Override
    public void onStarlightReceive(Level world, IWeakConstellation type, double amount) {
        TreeBeaconBlockEntity well = getBlockEntityAtPos(world);
        if (well != null) {
            well.receiveStarlight(amount, type);
        }
    }

    @Override
    public boolean syncBlockEntityData(Level world, TreeBeaconBlockEntity blockEntity) {
        return true;
    }

    @Override
    public Class<TreeBeaconBlockEntity> getBlockEntityClass() {
        return TreeBeaconBlockEntity.class;
    }

    @Override
    public TransmissionProvider getProvider() {
        return new Provider();
    }

    public static class Provider extends TransmissionProvider {

        @Override
        public IPrismTransmissionNode get() {
            return new StarlightReceiverTreeBeacon(null);
        }

    }
}
