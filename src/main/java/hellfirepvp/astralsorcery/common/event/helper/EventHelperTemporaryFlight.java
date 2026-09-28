package hellfirepvp.astralsorcery.common.event.helper;

import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.util.tick.TimeoutList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;

import java.util.function.Consumer;

public class EventHelperTemporaryFlight {

    private static final TimeoutList<Player> temporaryFlight = new TimeoutList<>(player -> {
        if (player instanceof ServerPlayer sp && (sp.gameMode.getGameModeForPlayer().isSurvival() || sp.gameMode.getGameModeForPlayer() == GameType.ADVENTURE)) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            sp.onUpdateAbilities();
        }
    }, TickEvent.Type.SERVER);

    private EventHelperTemporaryFlight() {}

    public static void clearServer() {
        temporaryFlight.clear();
    }

    public static void onDisconnect(ServerPlayer player) {
        temporaryFlight.remove(player);
    }

    public static void attachTickListener(Consumer<ITickHandler> registrar) {
        registrar.accept(temporaryFlight);
    }

    public static boolean allowFlight(Player player) {
        return allowFlight(player, 20);
    }

    public static boolean allowFlight(Player player, int timeout) {
        return temporaryFlight.setOrAddTimeout(timeout, player);
    }
}
