package hellfirepvp.astralsorcery.common.tick;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.common.CommonScheduler;
import hellfirepvp.astralsorcery.common.ITickHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the contract between {@link TickManager} and {@link ITickHandler}, which is the contract a handler broke
 * when it read a {@code TickEvent.Phase} out of {@code context[0]}: TickManager only passes a context for
 * LEVEL / RENDER / PLAYER ticks, so SERVER and CLIENT handlers receive an empty varargs array. Reading
 * {@code context[0]} there threw ArrayIndexOutOfBoundsException out of the tick event and crashed the dedicated
 * server on its very first tick (observed at boot, MinecraftServer#tickServer -> ForgeEventFactory#onPostServerTick).
 */
class TickHandlerContractTest {

    private static final class Recorder implements ITickHandler {

        private final EnumSet<TickEvent.Type> types;
        private final List<Object[]> calls = new ArrayList<>();

        private Recorder(TickEvent.Type... types) {
            this.types = EnumSet.copyOf(List.of(types));
        }

        @Override
        public void tick(TickEvent.Type type, Object... context) {
            this.calls.add(context);
        }

        @Override
        public EnumSet<TickEvent.Type> getHandledTypes() {
            return this.types;
        }

        @Override
        public boolean canFire(TickEvent.Phase phase) {
            return phase == TickEvent.Phase.END;
        }

        @Override
        public String getName() {
            return "Recorder";
        }
    }

    private static void dispatch(TickManager manager, String method, Class<?> eventType, Object event) {
        try {
            Method m = TickManager.class.getDeclaredMethod(method, eventType);
            m.setAccessible(true);
            m.invoke(manager, event);
        } catch (ReflectiveOperationException e) {
            //An InvocationTargetException here is a real failure of the code under test, not a test-harness problem.
            throw new AssertionError("dispatching " + method + " failed", e.getCause() != null ? e.getCause() : e);
        }
    }

    private static void dispatchServerTick(TickManager manager, TickEvent.Phase phase) {
        dispatch(manager, "serverTick", TickEvent.ServerTickEvent.class,
                new TickEvent.ServerTickEvent(phase, () -> true, null));
    }

    private static void dispatchClientTick(TickManager manager, TickEvent.Phase phase) {
        dispatch(manager, "clientTick", TickEvent.ClientTickEvent.class,
                new TickEvent.ClientTickEvent(phase));
    }

    @Test
    void serverAndClientTicksCarryNoContextWhileLevelTicksCarryTheLevel() {
        TickManager manager = new TickManager();
        Recorder server = new Recorder(TickEvent.Type.SERVER);
        Recorder client = new Recorder(TickEvent.Type.CLIENT);
        Recorder level = new Recorder(TickEvent.Type.LEVEL);
        manager.register(server);
        manager.register(client);
        manager.register(level);

        dispatchServerTick(manager, TickEvent.Phase.END);
        dispatchClientTick(manager, TickEvent.Phase.END);
        dispatch(manager, "worldTick", TickEvent.LevelTickEvent.class,
                new TickEvent.LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.END, null, () -> true));

        assertEquals(1, server.calls.size(), "the SERVER handler should have been ticked once");
        assertEquals(0, server.calls.get(0).length,
                "TickManager#serverTick passes no context, so a SERVER handler must not read context[0]");

        assertEquals(1, client.calls.size(), "the CLIENT handler should have been ticked once");
        assertEquals(0, client.calls.get(0).length,
                "TickManager#clientTick passes no context, so a CLIENT handler must not read context[0]");

        assertEquals(1, level.calls.size(), "the LEVEL handler should have been ticked once");
        assertEquals(1, level.calls.get(0).length, "TickManager#worldTick passes the level as context[0]");
    }

    @Test
    void canFireFiltersTheStartPhaseBeforeTheHandlerIsCalled() {
        TickManager manager = new TickManager();
        Recorder server = new Recorder(TickEvent.Type.SERVER);
        manager.register(server);

        dispatchServerTick(manager, TickEvent.Phase.START);
        assertEquals(0, server.calls.size(),
                "canFire(START) == false, so TickManager must not call tick at all - a handler therefore does not "
                        + "need to re-check the phase itself");

        dispatchServerTick(manager, TickEvent.Phase.END);
        assertEquals(1, server.calls.size());
    }

    @Test
    void commonSchedulerSurvivesARealServerTick() {
        TickManager manager = new TickManager();
        CommonScheduler scheduler = new CommonScheduler();
        manager.register(scheduler);

        AtomicInteger ran = new AtomicInteger();
        scheduler.addRunnable(ran::incrementAndGet, 1);

        //Before the fix this threw ArrayIndexOutOfBoundsException: Index 0 out of bounds for length 0.
        assertDoesNotThrow(() -> dispatchServerTick(manager, TickEvent.Phase.END));
        assertEquals(1, ran.get(), "a runnable scheduled with a one tick delay runs on the next tick");
    }

    @Test
    void clientSchedulerSurvivesARealClientTickAndAdvancesTheClientTickCounter() {
        TickManager manager = new TickManager();
        ClientScheduler scheduler = new ClientScheduler();
        manager.register(scheduler);

        AtomicInteger ran = new AtomicInteger();
        scheduler.addRunnable(ran::incrementAndGet, 1);

        //clientTick is static and shared across tests, so only the delta is asserted.
        long before = ClientScheduler.getClientTick();
        assertDoesNotThrow(() -> dispatchClientTick(manager, TickEvent.Phase.END));
        assertEquals(1, ClientScheduler.getClientTick() - before,
                "getClientTick must advance once per client tick - every VFXColorFunction / RenderOffset* samples it");
        assertEquals(1, ran.get());
    }

    @Test
    void commonSchedulerOnlyListensToServerTicks() {
        //Listening to CLIENT as well would decrement every pending delay twice per tick in singleplayer, where the
        //integrated server's SERVER tick and the client's CLIENT tick both fire. The client side has its own
        //ClientScheduler, registered separately by ClientProxy.
        assertEquals(EnumSet.of(TickEvent.Type.SERVER), new CommonScheduler().getHandledTypes());
        assertEquals(EnumSet.of(TickEvent.Type.CLIENT), new ClientScheduler().getHandledTypes());
    }

    @Test
    void commonSchedulerCountsDownExactlyTheRequestedNumberOfTicks() {
        TickManager manager = new TickManager();
        CommonScheduler scheduler = new CommonScheduler();
        manager.register(scheduler);

        AtomicInteger ran = new AtomicInteger();
        scheduler.addRunnable(ran::incrementAndGet, 3);

        dispatchServerTick(manager, TickEvent.Phase.END);
        assertEquals(0, ran.get(), "still two ticks to go");
        dispatchServerTick(manager, TickEvent.Phase.END);
        assertEquals(0, ran.get(), "still one tick to go");
        dispatchServerTick(manager, TickEvent.Phase.END);
        assertEquals(1, ran.get(), "runs on the third tick");
        dispatchServerTick(manager, TickEvent.Phase.END);
        assertEquals(1, ran.get(), "and is removed from the queue afterwards");
    }

    @Test
    void aRunnableScheduledFromInsideATickIsDeferredToTheNextTick() {
        TickManager manager = new TickManager();
        CommonScheduler scheduler = new CommonScheduler();
        manager.register(scheduler);

        AtomicInteger nested = new AtomicInteger();
        scheduler.addRunnable(() -> scheduler.addRunnable(nested::incrementAndGet, 0), 0);

        dispatchServerTick(manager, TickEvent.Phase.END);
        assertEquals(0, nested.get(),
                "the inTick guard must park the nested runnable in the waiting list rather than mutating the queue "
                        + "that is being iterated");
        dispatchServerTick(manager, TickEvent.Phase.END);
        assertEquals(1, nested.get());
    }
}
