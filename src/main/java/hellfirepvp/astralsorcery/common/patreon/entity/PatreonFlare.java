/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.patreon.entity;
import java.util.UUID;

/** Shared companion movement/sync state. Rendering is composed around it on the client. */
public final class PatreonFlare extends PatreonPartialEntity {
    public PatreonFlare(UUID effectUUID, UUID ownerUUID) { super(effectUUID, ownerUUID); }
}
