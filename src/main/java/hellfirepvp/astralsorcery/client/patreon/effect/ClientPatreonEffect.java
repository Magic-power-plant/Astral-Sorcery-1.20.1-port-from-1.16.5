/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.effect;
import hellfirepvp.astralsorcery.client.patreon.entity.PatreonEntityRenderer;
import hellfirepvp.astralsorcery.client.patreon.entity.PatreonFlareRenderer;
import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.patreon.effect.FlareColor;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffect;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonPartialEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

/** Client effect lifecycle. Factories are installed exclusively by the client manager. */
public abstract class ClientPatreonEffect extends PatreonEffect implements AutoCloseable {
    protected static final Random rand = new Random();
    protected ClientPatreonEffect(UUID id, @Nullable FlareColor color) { super(id, color); }
    public void initialize() {}
    public void attachEventListeners(IEventBus bus) {}
    public void attachTickListeners(Consumer<ITickHandler> registrar) {}
    public void doClientEffect(Player player) {}
    public PatreonEntityRenderer createRenderer(PatreonPartialEntity entity) {
        return new PatreonFlareRenderer(entity, this);
    }
    @Override
    public void close() {}
}
