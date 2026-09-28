package hellfirepvp.astralsorcery.common.event.helper;

import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.data.sync.server.DataTimeFreezeEntities;
import hellfirepvp.astralsorcery.common.util.tick.TimeoutList;
import hellfirepvp.astralsorcery.common.util.time.TimeStopController;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.Consumer;

public class EventHelperEntityFreeze {

    private static final TimeoutList<Entity> timeFreezeTimeout = new TimeoutList<>(entity -> {
        SyncDataHolder.executeServer(SyncDataHolder.DATA_TIME_FREEZE_ENTITIES, DataTimeFreezeEntities.class, data -> {
            data.unfreeze(entity);
        });
    }, TickEvent.Type.SERVER);

    private EventHelperEntityFreeze() {}

    public static void attachTickListener(Consumer<ITickHandler> registrar) {
        registrar.accept(timeFreezeTimeout);
    }

    public static void attachListeners(IEventBus bus) {
        bus.addListener(EventPriority.HIGHEST, EventHelperEntityFreeze::onLivingTick);
        bus.addListener(EventHelperEntityFreeze::onLivingKnockBack);
        bus.addListener(EventHelperEntityFreeze::onDestroy);
    }

    private static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (TimeStopController.skipLivingTick(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (TimeStopController.skipLivingTick(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onDestroy(LivingDestroyBlockEvent event) {
        if (TimeStopController.isFrozenDirectly(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    public static boolean freeze(Entity e) {
        if (timeFreezeTimeout.setOrAddTimeout(20, e)) {
            SyncDataHolder.executeServer(SyncDataHolder.DATA_TIME_FREEZE_ENTITIES, DataTimeFreezeEntities.class, data -> {
                data.freeze(e);
            });
            return true;
        }
        return false;
    }
}
