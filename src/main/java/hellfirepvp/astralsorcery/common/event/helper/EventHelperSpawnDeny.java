package hellfirepvp.astralsorcery.common.event.helper;

import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.data.config.entry.GeneralConfig;
import hellfirepvp.astralsorcery.common.block.util.WorldBlockPos;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.tick.TickTokenMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Map;
import java.util.function.Consumer;

public class EventHelperSpawnDeny {

    public static TickTokenMap<WorldBlockPos, TickTokenMap.SimpleTickToken<Double>> spawnDenyRegions = new TickTokenMap<>(TickEvent.Type.SERVER);

    private EventHelperSpawnDeny() {}

    public static void clearServer() {
        spawnDenyRegions.clear();
    }

    public static void attachTickListener(Consumer<ITickHandler> registrar) {
        registrar.accept(spawnDenyRegions);
    }

    public static void attachListeners(IEventBus eventBus) {
        eventBus.addListener(EventHelperSpawnDeny::onSpawn);
    }

    //1.16.5 listens to LivingSpawnEvent.CheckSpawn, which is @HasResult. That event is gone in 1.20.1 and this
    //had been retargeted onto MobSpawnEvent.FinalizeSpawn - which Forge 47.4.20 declares @Cancelable and
    //deliberately NOT @HasResult (verified with javap: FinalizeSpawn carries only
    //net.minecraftforge.eventbus.api.Cancelable). EventBus#setResult is a bare field write with no validation and
    //ForgeEventFactory#onFinalizeSpawn never reads getResult(), so the setResult(DENY) below was silently
    //discarded and the deny regions blocked nothing at all. Populating spawnDenyRegions is the only server-side
    //action a Lucerna ritual performs, so that ritual was inert.
    //
    //MobSpawnEvent.PositionCheck is the faithful replacement rather than FinalizeSpawn#setSpawnCancelled:
    //  - it IS @HasResult (javap), so both the DENY early-out below and setResult keep 1.16.5's meaning;
    //    ForgeEventFactory#checkSpawnPosition returns "result == ALLOW" whenever the result is not DEFAULT,
    //    so DENY blocks the spawn;
    //  - it still exposes getSpawner(), so 1.16.5's "never deny a spawner-driven spawn" skip is preserved
    //    exactly (checkSpawnPosition passes null, checkSpawnPositionSpawner passes the BaseSpawner);
    //  - it fires from NaturalSpawner at the same point CheckSpawn did, before the mob is finalized, so no
    //    equipment or variant roll is wasted. setSpawnCancelled would run finalizeSpawn first and cannot
    //    express the DENY early-out.
    private static void onSpawn(MobSpawnEvent.PositionCheck event) {
        if (event.getResult() == Event.Result.DENY ||
                event.getLevel().isClientSide() ||
                event.getSpawner() != null) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity.getTags().contains(ConstellationEffectRegistry.ENTITY_TAG_LUCERNA_SKIP_ENTITY)) {
            return;
        }

        if (GeneralConfig.CONFIG.mobSpawningDenyAllTypes.get() || entity.getClassification(false) == MobCategory.MONSTER) {
            Vector3 entityPos = Vector3.atEntityCorner(entity);
            for (Map.Entry<WorldBlockPos, TickTokenMap.SimpleTickToken<Double>> entry : spawnDenyRegions.entrySet()) {
                if (!entry.getKey().getWorldKey().equals(entity.level().dimension())) {
                    continue;
                }

                if (entityPos.distance(entry.getKey()) <= entry.getValue().getValue()) {
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
        }
    }

}
