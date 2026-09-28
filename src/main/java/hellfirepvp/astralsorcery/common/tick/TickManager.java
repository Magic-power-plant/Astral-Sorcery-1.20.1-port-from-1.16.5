package hellfirepvp.astralsorcery.common.tick;

import hellfirepvp.astralsorcery.common.ITickHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TickManager
 * Created by HellFirePvP
 * Date: 04.08.2016 / 11:20
 *
 * Embedded from ObserverLib (hellfirepvp.observerlib.common.util.tick.TickManager);
 * ObserverLib is a source-only reference for this port, not a runtime dependency.
 * The 1.16.5 mod consumed this class (and {@link ITickHandler}) straight from ObserverLib
 * through CommonProxy#tickManager / CommonProxy#attachTickListeners.
 */
public class TickManager {

    private final Map<TickEvent.Type, List<ITickHandler>> registeredTickHandlers = new HashMap<>();

    public TickManager() {
        for (TickEvent.Type type : TickEvent.Type.values()) {
            registeredTickHandlers.put(type, new ArrayList<>());
        }
    }

    public void attachListeners(IEventBus eventBus) {
        eventBus.addListener(this::worldTick);
        eventBus.addListener(this::serverTick);
        eventBus.addListener(this::playerTick);
        eventBus.addListener(this::renderTick);
        eventBus.addListener(this::clientTick);
    }

    /**
     * Registers a TickHandler.
     *
     * @param handler the handler to register
     */
    public void register(ITickHandler handler) {
        for (TickEvent.Type type : handler.getHandledTypes()) {
            registeredTickHandlers.get(type).add(handler);
        }
    }

    /**
     * Unregisters a TickHandler.
     *
     * @param handler the handler to remove
     * @return true if it has been successfully found in all lists it should've been registered to originally
     */
    public boolean unregister(ITickHandler handler) {
        boolean removed = true;
        for (TickEvent.Type type : handler.getHandledTypes()) {
            if (!registeredTickHandlers.get(type).remove(handler)) {
                removed = false;
            }
        }
        return removed;
    }

    private void worldTick(TickEvent.LevelTickEvent event) {
        TickEvent.Phase ph = event.phase;
        for (ITickHandler handler : registeredTickHandlers.get(TickEvent.Type.LEVEL)) {
            if (handler.canFire(ph)) handler.tick(TickEvent.Type.LEVEL, event.level);
        }
    }

    private void serverTick(TickEvent.ServerTickEvent event) {
        TickEvent.Phase ph = event.phase;
        for (ITickHandler handler : registeredTickHandlers.get(TickEvent.Type.SERVER)) {
            if (handler.canFire(ph)) handler.tick(TickEvent.Type.SERVER);
        }
    }

    private void clientTick(TickEvent.ClientTickEvent event) {
        TickEvent.Phase ph = event.phase;
        for (ITickHandler handler : registeredTickHandlers.get(TickEvent.Type.CLIENT)) {
            if (handler.canFire(ph)) handler.tick(TickEvent.Type.CLIENT);
        }
    }

    private void renderTick(TickEvent.RenderTickEvent event) {
        TickEvent.Phase ph = event.phase;
        for (ITickHandler handler : registeredTickHandlers.get(TickEvent.Type.RENDER)) {
            if (handler.canFire(ph)) handler.tick(TickEvent.Type.RENDER, event.renderTickTime);
        }
    }

    private void playerTick(TickEvent.PlayerTickEvent event) {
        TickEvent.Phase ph = event.phase;
        for (ITickHandler handler : registeredTickHandlers.get(TickEvent.Type.PLAYER)) {
            if (handler.canFire(ph)) handler.tick(TickEvent.Type.PLAYER, event.player, event.side);
        }
    }

}
