package hellfirepvp.astralsorcery.client.util.camera;

import hellfirepvp.astralsorcery.common.ITickHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.TreeSet;

/**
 * Port of 1.16.5 {@code ClientCameraManager}. Registered as a client tick handler; the RENDER tick context
 * carries the partial tick as {@code context[0]} (see {@code TickManager#onRenderTick}).
 */
public class ClientCameraManager implements ITickHandler {

    public static final ClientCameraManager INSTANCE = new ClientCameraManager();

    private final TreeSet<ICameraTransformer> transformers = new TreeSet<>(Comparator.comparingInt(ICameraTransformer::getPriority));
    private ICameraTransformer lastTransformer = null;

    private ClientCameraManager() {}

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        if (type == TickEvent.Type.RENDER) {
            //Render Tick
            float pTicks = context.length > 0 && context[0] instanceof Float ? (Float) context[0] : 0F;
            if (this.hasActiveTransformer()) {
                ICameraTransformer prio = this.getActiveTransformer();
                if (!prio.equals(lastTransformer)) {
                    if (lastTransformer != null) {
                        lastTransformer.onStopTransforming(pTicks);
                    }
                    prio.onStartTransforming(pTicks);
                    lastTransformer = prio;
                }
                prio.transformRenderView(Minecraft.getInstance().isPaused() ? 0F : pTicks);
                if (prio.getPersistencyFunction().isExpired()) {
                    prio.onStopTransforming(pTicks);
                    transformers.remove(prio);
                }
            } else {
                //Clean up remaining transformer
                if (lastTransformer != null) {
                    lastTransformer.onStopTransforming(pTicks);
                    lastTransformer = null;
                }
            }
        } else if (!Minecraft.getInstance().isPaused()) {
            //Client Tick
            if (this.hasActiveTransformer()) {
                this.getActiveTransformer().onClientTick();
            }
        }
    }

    public void removeAllAndCleanup() {
        if (this.hasActiveTransformer()) {
            transformers.last().onStopTransforming(0);
        }
        this.transformers.clear();
        this.lastTransformer = null;
    }

    public void addTransformer(ICameraTransformer transformer) {
        this.transformers.add(transformer);
    }

    public void removeTransformer(ICameraTransformer transformer) {
        this.transformers.remove(transformer);
    }

    @Nullable
    public ICameraTransformer getActiveTransformer() {
        if (this.hasActiveTransformer()) {
            return this.transformers.last();
        }
        return null;
    }

    public boolean hasActiveTransformer() {
        return !this.transformers.isEmpty();
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.RENDER, TickEvent.Type.CLIENT);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.START;
    }

    @Override
    public String getName() {
        return "Client Camera Manager";
    }
}
