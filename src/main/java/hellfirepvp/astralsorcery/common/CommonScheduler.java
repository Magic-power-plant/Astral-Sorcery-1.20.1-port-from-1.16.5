package hellfirepvp.astralsorcery.common;

import hellfirepvp.astralsorcery.common.util.Counter;
import net.minecraftforge.event.TickEvent;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;

public class CommonScheduler implements ITickHandler {

    private static final Object lock = new Object();

    private boolean inTick = false;
    private final LinkedList<Entry> queue = new LinkedList<>();
    private final LinkedList<Entry> waiting = new LinkedList<>();

    //TickManager#serverTick calls tick(TickEvent.Type.SERVER) with no varargs (it only passes a context for
    //LEVEL/RENDER/PLAYER), so reading a Phase out of context[0] threw ArrayIndexOutOfBoundsException on the very
    //first server tick. The phase is already filtered by canFire below, exactly as in 1.16.5.
    @Override
    public void tick(TickEvent.Type type, Object... context) {
        inTick = true;
        synchronized (lock) {
            Iterator<Entry> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Entry r = iterator.next();
                r.counter.decrement();
                if (r.counter.getValue() <= 0) {
                    r.runnable.run();
                    iterator.remove();
                }
            }
            inTick = false;
            for (Entry w : waiting) {
                queue.addLast(new Entry(w.runnable, w.counter.getValue()));
            }
        }
        waiting.clear();
    }

    public void addRunnable(Runnable runnable, int tickDelay) {
        synchronized (lock) {
            if (inTick) {
                waiting.addLast(new Entry(runnable, tickDelay));
            } else {
                queue.addLast(new Entry(runnable, tickDelay));
            }
        }
    }

    //SERVER only, as in 1.16.5: the client side has its own ClientScheduler (registered by ClientProxy), and
    //listening to CLIENT as well made every scheduleDelayed runnable decrement twice per tick in singleplayer,
    //where both tick types fire.
    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.SERVER);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "Common Scheduler";
    }

    private static class Entry {
        final Runnable runnable;
        final Counter counter;

        Entry(Runnable runnable, int ticks) {
            this.runnable = runnable;
            this.counter = new Counter(ticks);
        }
    }
}