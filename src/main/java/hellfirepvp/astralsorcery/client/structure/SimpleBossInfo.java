/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.structure;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleBossInfo
 * Created by HellFirePvP
 * Date: 12.02.2020 / 21:06
 *
 * Embedded VERBATIM from ObserverLib (hellfirepvp.observerlib.client.util.SimpleBossInfo - whose own header
 * already credits Astral Sorcery); ObserverLib is a source-only reference for this port, not a runtime
 * dependency. Only the package declaration, the license header and the {@link OnlyIn} annotation differ.
 *
 * A boss bar with no boss behind it, pushed straight into {@link BossHealthOverlay}'s event map so the
 * Resonating Wand can show multiblock completion progress. That map is private in vanilla; the
 * {@code BossHealthOverlay f_93699_} entry added to accesstransformer.cfg exposes it, exactly as ObserverLib's
 * own access transformer does.
 */
@OnlyIn(Dist.CLIENT)
public class SimpleBossInfo extends LerpingBossEvent {

    private SimpleBossInfo(UUID id, Component name,
                           BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay,
                           boolean darkenScreen, boolean playBossMusic, boolean worldFog) {
        super(id, name, 1F, color, overlay, darkenScreen, playBossMusic, worldFog);
    }

    public static SimpleBossInfo create(Component text, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
        return create(UUID.randomUUID(), text, color, overlay);
    }

    public static SimpleBossInfo create(Component text, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay,
                                        boolean darkenScreen, boolean playBossMusic, boolean worldFog) {
        return create(UUID.randomUUID(), text, color, overlay, darkenScreen, playBossMusic, worldFog);
    }

    public static SimpleBossInfo create(UUID id, Component text, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
        return new SimpleBossInfo(id, text, color, overlay, false, false, false);
    }

    public static SimpleBossInfo create(UUID id, Component text, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay,
                                        boolean darkenScreen, boolean playBossMusic, boolean worldFog) {
        return new SimpleBossInfo(id, text, color, overlay, darkenScreen, playBossMusic, worldFog);
    }

    public boolean displayInfo() {
        if (Minecraft.getInstance().level == null) {
            return false;
        }
        BossHealthOverlay gui = Minecraft.getInstance().gui.getBossOverlay();
        if (!gui.events.containsKey(this.getId())) {
            return gui.events.put(this.getId(), this) == null;
        }
        return false;
    }

    public boolean removeInfo() {
        return Minecraft.getInstance().gui.getBossOverlay().events.remove(this.getId()) != null;
    }
}
