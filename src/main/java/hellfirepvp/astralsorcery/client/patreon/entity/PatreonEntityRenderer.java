/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.entity;
import hellfirepvp.astralsorcery.client.patreon.effect.ClientPatreonEffect;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonPartialEntity;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.Level;
import java.util.Random;

/** Owns only visual lifetime; the synchronized companion continues to own movement and NBT. */
public abstract class PatreonEntityRenderer implements AutoCloseable {
    protected static final Random rand = new Random();
    protected final PatreonPartialEntity entity;
    private final ClientPatreonEffect effect;
    private boolean removed;

    protected PatreonEntityRenderer(PatreonPartialEntity entity, ClientPatreonEffect effect) {
        this.entity = entity;
        this.effect = effect;
    }
    protected Vector3 getPos() { return entity.getPos(); }
    protected ClientPatreonEffect getEffect() { return effect; }
    protected boolean isRemoved() { return removed || entity.isRemoved(); }
    public void tickClient() {}
    public void tickEffects(Level world) {}
    @Override
    public void close() { removed = true; }
}
