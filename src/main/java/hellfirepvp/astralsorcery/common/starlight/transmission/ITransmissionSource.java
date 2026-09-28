package hellfirepvp.astralsorcery.common.starlight.transmission;

import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.IStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ITransmissionSource extends IPrismTransmissionNode {

    IIndependentStarlightSource provideNewIndependentSource(IStarlightSource<?> source);

    @Override
    default <T extends BlockEntity> boolean updateFromTileEntity(T tile) {
        Level world = tile.getLevel();
        if (world == null) {
            return true;
        }
        IIndependentStarlightSource source = WorldNetworkHandler.getNetworkHandler(world).getSourceAt(getLocationPos());
        return source == null || source.updateFromTileEntity(tile);
    }
}
