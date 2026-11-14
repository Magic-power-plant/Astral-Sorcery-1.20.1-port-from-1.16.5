/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.effect;
import hellfirepvp.astralsorcery.common.patreon.effect.FlareColor;
import javax.annotation.Nullable;
import java.util.UUID;

public final class TypeFlare extends ClientPatreonEffect {
    public TypeFlare(UUID id, @Nullable FlareColor color) { super(id, color); }
}
