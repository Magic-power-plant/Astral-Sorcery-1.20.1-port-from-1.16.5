package hellfirepvp.astralsorcery.common.event.handler;

import hellfirepvp.astralsorcery.common.data.world.cache.WorldCacheManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EventHandlerWorldCacheIO
 * Created by HellFirePvP
 * Date: 03.07.2019 / 15:35
 *
 * Embedded from ObserverLib (hellfirepvp.observerlib.common.event.handler.EventHandlerIO);
 * ObserverLib is a source-only reference for this port, not a runtime dependency.
 * Schedules a save of every dirty {@link hellfirepvp.astralsorcery.common.data.world.cache.CachedWorldData}
 * of a server level whenever that level is saved.
 */
public class EventHandlerWorldCacheIO {

    public static void init(IEventBus eventBus) {
        eventBus.addListener(EventHandlerWorldCacheIO::onSave);
    }

    private static void onSave(LevelEvent.Save event) {
        if (event.getLevel().isClientSide() || !(event.getLevel() instanceof Level)) {
            return;
        }
        WorldCacheManager.getInstance().doSave((Level) event.getLevel());
    }

}
