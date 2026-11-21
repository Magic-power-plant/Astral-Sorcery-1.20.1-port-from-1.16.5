package hellfirepvp.astralsorcery.api;

import net.minecraft.world.entity.player.Player;

/** Client-side tick behavior contract for a mantle effect. */
public interface IMantleEffect {

    default void tickClient(Player player) {}
}
