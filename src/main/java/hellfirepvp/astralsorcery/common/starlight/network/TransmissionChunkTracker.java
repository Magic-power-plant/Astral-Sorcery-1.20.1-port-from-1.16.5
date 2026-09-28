package hellfirepvp.astralsorcery.common.starlight.network;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class TransmissionChunkTracker {

    public static final TransmissionChunkTracker INSTANCE = new TransmissionChunkTracker();

    private TransmissionChunkTracker() {}

    public void attachListeners(IEventBus eventBus) {
        eventBus.addListener(this::onChLoad);
        eventBus.addListener(this::onChUnload);
        eventBus.addListener(this::onWorldLoad);
        eventBus.addListener(this::onWorldUnload);
    }

    private void onChLoad(ChunkEvent.Load event) {
        LevelAccessor iWorld = event.getLevel();
        if (iWorld.isClientSide() || !(iWorld instanceof Level)) {
            return;
        }
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler((Level) iWorld);
        if (handle != null) {
            handle.informChunkLoad(event.getChunk().getPos());
        }
    }

    private void onChUnload(ChunkEvent.Unload event) {
        LevelAccessor iWorld = event.getLevel();
        if (iWorld.isClientSide() || !(iWorld instanceof Level)) {
            return;
        }
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler((Level) iWorld);
        if (handle != null) {
            handle.informChunkUnload(event.getChunk().getPos());
        }
    }

    private void onWorldLoad(LevelEvent.Load event) {
        LevelAccessor iWorld = event.getLevel();
        if (iWorld.isClientSide() || !(iWorld instanceof Level)) {
            return;
        }
        StarlightUpdateHandler.getInstance().informWorldLoad((Level) iWorld);
    }

    private void onWorldUnload(LevelEvent.Unload event) {
        LevelAccessor iWorld = event.getLevel();
        if (iWorld.isClientSide() || !(iWorld instanceof Level)) {
            return;
        }
        StarlightTransmissionHandler.getInstance().informWorldUnload((Level) iWorld);
    }

}
