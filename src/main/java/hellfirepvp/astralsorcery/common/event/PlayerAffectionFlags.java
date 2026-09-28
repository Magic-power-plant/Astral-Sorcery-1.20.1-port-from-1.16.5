package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.util.tick.TimeoutList;
import hellfirepvp.astralsorcery.common.util.tick.TimeoutListContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Tracks, for a few ticks at a time, which players are currently standing inside which constellation effect.
 * <p>
 * Ported from 1.16.5 common/event/PlayerAffectionFlags.java. The 1.20.1 version of this class had been reduced
 * to a stub - {@code markPlayerAffected} was empty and {@code isPlayerAffected} returned false unconditionally -
 * behind a claim that {@code TimeoutListContainer} lacked the members needed. It does not: {@code hasList},
 * {@code getOrCreateList}, {@code clear} and {@code ForwardingTimeoutDelegate} are all present, and
 * {@code TimeoutList#setOrAddTimeout} is used the same way by {@code PerkCooldownHelper}.
 * <p>
 * The writers were always live - {@code ConstellationEffect#playEffect} marks every player in a ritual's range
 * and {@code MantleEffect} does the same for its wearer - so only the reader was broken. With
 * {@code isPlayerAffected} hardcoded to false, {@code MantleEffectVicio} always took its {@code drainCharge}
 * branch, charging 100 alignment charge per second for flight that a Vicio ritual is supposed to make free.
 * <p>
 * Entries expire on their own after {@value #DEFAULT_TICK_TIMEOUT} ticks, which is why this needs a tick
 * listener ({@link #attachTickListeners}) and a server-stop hook ({@link #clearServerCache}).
 */
public class PlayerAffectionFlags {

    private static final int DEFAULT_TICK_TIMEOUT = 10;
    private static final TimeoutListContainer<UUID, AffectionFlag> affectMap =
            new TimeoutListContainer<>(new TimeoutListContainer.ForwardingTimeoutDelegate<>(), TickEvent.Type.SERVER);

    private PlayerAffectionFlags() {}

    public static void attachTickListeners(Consumer<ITickHandler> registrar) {
        registrar.accept(affectMap);
    }

    public static void clearServerCache() {
        affectMap.clear();
    }

    public static void markPlayerAffected(Player player, AffectionFlag flag) {
        affectMap.getOrCreateList(player.getUUID()).setOrAddTimeout(DEFAULT_TICK_TIMEOUT, flag);
    }

    public static boolean isPlayerAffected(Player player, AffectionFlag flag) {
        UUID playerUUID = player.getUUID();
        return affectMap.hasList(playerUUID) && affectMap.getOrCreateList(playerUUID).contains(flag);
    }

    //Must implement TimeoutList.TimeoutDelegate<UUID>: TimeoutListContainer.ForwardingTimeoutDelegate is declared
    //as <K, V extends TimeoutList.TimeoutDelegate<K>>, so the value type carries the per-entry expiry callback.
    public static abstract class AffectionFlag implements TimeoutList.TimeoutDelegate<UUID> {

        private final ResourceLocation key;

        public AffectionFlag(ResourceLocation key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AffectionFlag that = (AffectionFlag) o;
            return Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }

    public static class NoOpAffectionFlag extends AffectionFlag {

        public NoOpAffectionFlag() {
            super(ResourceLocation.parse("astralsorcery:noop"));
        }

        public NoOpAffectionFlag(ResourceLocation key) {
            super(key);
        }

        @Override
        public void onTimeout(UUID object) {}
    }
}
