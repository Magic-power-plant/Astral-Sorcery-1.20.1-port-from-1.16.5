/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.patreon.effect;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonFlare;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonPartialEntity;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

/** Shared identity and companion lifecycle; visual behaviour belongs to the client adapter. */
public abstract class PatreonEffect {
    private final FlareColor flareColor;
    private final UUID effectUUID;

    protected PatreonEffect(UUID effectUUID, @Nullable FlareColor flareColor) {
        this.effectUUID = Objects.requireNonNull(effectUUID);
        this.flareColor = flareColor;
    }

    @Nullable
    public FlareColor getFlareColor() { return flareColor; }

    public UUID getEffectUUID() { return effectUUID; }

    public boolean hasPartialEntity() { return flareColor != null; }

    @Nullable
    public PatreonPartialEntity createEntity(UUID ownerUUID) {
        return hasPartialEntity() ? new PatreonFlare(effectUUID, ownerUUID) : null;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other != null && getClass() == other.getClass()
                && effectUUID.equals(((PatreonEffect) other).effectUUID);
    }

    @Override
    public int hashCode() { return effectUUID.hashCode(); }
}
