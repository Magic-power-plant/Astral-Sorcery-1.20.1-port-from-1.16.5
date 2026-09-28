package hellfirepvp.astralsorcery.common.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class CooldownSetEvent extends Event {

    private final Player player;
    private final int originalCooldown;
    private int cooldown;

    public CooldownSetEvent(Player player, int originalCooldown) {
        this.player = player;
        this.originalCooldown = originalCooldown;
        this.setCooldown(this.getOriginalCooldown());
    }

    public Player getPlayer() {
        return player;
    }

    public int getOriginalCooldown() {
        return originalCooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getResultCooldown() {
        return cooldown;
    }
}