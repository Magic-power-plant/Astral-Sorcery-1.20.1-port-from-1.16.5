package hellfirepvp.astralsorcery.common.starlight.network;

import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;

import java.util.*;

public class StarlightUpdateHandler implements ITickHandler {

    private static final StarlightUpdateHandler instance = new StarlightUpdateHandler();
    private static final Map<ResourceKey<Level>, List<IPrismTransmissionNode>> updateRequired = new HashMap<>();
    private static final Object accessLock = new Object();

    private StarlightUpdateHandler() {}

    public static StarlightUpdateHandler getInstance() {
        return instance;
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Level world = (Level) context[0];
        if (world.isClientSide()) {
            return;
        }

        List<IPrismTransmissionNode> nodes = getNodes(world);
        synchronized (accessLock) {
            for (IPrismTransmissionNode node : nodes) {
                node.update(world);
            }
        }
    }

    private List<IPrismTransmissionNode> getNodes(Level world) {
        return updateRequired.computeIfAbsent(world.dimension(), k -> new LinkedList<>());
    }

    public void removeNode(Level world, IPrismTransmissionNode node) {
        synchronized (accessLock) {
            getNodes(world).remove(node);
        }
    }

    public void addNode(Level world, IPrismTransmissionNode node) {
        synchronized (accessLock) {
            getNodes(world).add(node);
        }
    }

    public void informWorldLoad(Level world) {
        synchronized (accessLock) {
            updateRequired.remove(world.dimension());
        }
    }

    public void clearServer() {
        synchronized (accessLock) {
            updateRequired.clear();
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.LEVEL);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "Starlight Update Handler";
    }

}
