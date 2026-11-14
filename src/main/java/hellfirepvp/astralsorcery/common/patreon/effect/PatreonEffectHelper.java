/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.patreon.effect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/** Publishes a complete immutable snapshot; readers never observe a half-loaded catalog. */
public final class PatreonEffectHelper {
    private static volatile Catalog catalog = new Catalog(Map.of(), Map.of());
    private PatreonEffectHelper() {}

    public static void replaceEffects(Collection<PatreonEffectDefinition> effects) {
        Map<UUID, List<PatreonEffectDefinition>> players = new HashMap<>();
        Map<UUID, PatreonEffectDefinition> byId = new HashMap<>();
        for (PatreonEffectDefinition effect : effects) {
            if (byId.putIfAbsent(effect.getEffectUUID(), effect) != null) {
                throw new IllegalArgumentException("Duplicate patreon effect: " + effect.getEffectUUID());
            }
            players.computeIfAbsent(effect.getOwnerUUID(), id -> new ArrayList<>()).add(effect);
        }
        players.replaceAll((id, entries) -> List.copyOf(entries));
        catalog = new Catalog(Map.copyOf(players), Map.copyOf(byId));
    }

    public static List<PatreonEffectDefinition> getPatreonEffects(LogicalSide side, UUID playerUUID) {
        // Rendering preferences must never filter network state or integrated-server state.
        return catalog.players().getOrDefault(playerUUID, List.of());
    }

    public static Collection<PatreonEffectDefinition> getEffects() { return catalog.effects().values(); }

    @Nullable
    public static PatreonEffectDefinition getEffect(UUID effectUUID) { return catalog.effects().get(effectUUID); }

    public static <T extends Player> Map<UUID, List<PatreonEffectDefinition>> getPatreonEffects(Collection<T> players) {
        Set<UUID> owners = players.stream().map(Player::getUUID).collect(Collectors.toSet());
        Map<UUID, List<PatreonEffectDefinition>> result = new HashMap<>();
        catalog.players().forEach((owner, effects) -> { if (owners.contains(owner)) result.put(owner, effects); });
        return result;
    }

    private record Catalog(Map<UUID, List<PatreonEffectDefinition>> players,
                           Map<UUID, PatreonEffectDefinition> effects) {}
}
