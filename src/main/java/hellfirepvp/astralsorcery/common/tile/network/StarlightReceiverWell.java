package hellfirepvp.astralsorcery.common.tile.network;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import hellfirepvp.astralsorcery.common.tile.TileWell;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class StarlightReceiverWell extends SimpleTransmissionReceiver<TileWell> {

    public StarlightReceiverWell(BlockPos thisPos) {
        super(thisPos);
    }

    @Override
    public void onStarlightReceive(Level world, IWeakConstellation type, double amount) {
        TileWell well = getTileAtPos(world);
        if (well != null) {
            well.receiveStarlight(amount);
        }
    }

    @Override
    public boolean syncTileData(Level world, TileWell tile) {
        return true;
    }

    @Override
    public Class<TileWell> getTileClass() {
        return TileWell.class;
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
