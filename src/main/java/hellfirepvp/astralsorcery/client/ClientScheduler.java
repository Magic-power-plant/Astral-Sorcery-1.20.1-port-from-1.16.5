package hellfirepvp.astralsorcery.client;

import net.minecraftforge.event.TickEvent;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class ClientScheduler implements hellfirepvp.astralsorcery.common.ITickHandler {

    private final List<ClientRunnableEntry> pendingRunnables = new LinkedList<>();

    //TickManager#clientTick calls tick(TickEvent.Type.CLIENT) with no varargs, so reading a Phase out of
    //context[0] threw ArrayIndexOutOfBoundsException on the first client tick - which also froze clientTick at 0
    //and with it every VFXColorFunction / RenderOffset* that samples getClientTick(). canFire already filters END.
    @Override
    public void tick(TickEvent.Type type, Object... context) {
        clientTick++;
        List<ClientRunnableEntry> toRemove = new LinkedList<>();
        for (ClientRunnableEntry entry : pendingRunnables) {
            entry.ticks--;
            if (entry.ticks <= 0) {
                entry.runnable.run();
                toRemove.add(entry);
            }
        }
        pendingRunnables.removeAll(toRemove);
    }

    public void addRunnable(Runnable runnable, int tickDelay) {
        pendingRunnables.add(new ClientRunnableEntry(runnable, tickDelay));
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.CLIENT);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "ClientScheduler";
    }

    private static long clientTick = 0;

    public static long getClientTick() {
        return clientTick;
    }

    private static class ClientRunnableEntry {
        final Runnable runnable;
        int ticks;

        ClientRunnableEntry(Runnable runnable, int ticks) {
            this.runnable = runnable;
            this.ticks = ticks;
        }
    }
}