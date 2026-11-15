package hellfirepvp.astralsorcery.client.screen.journal.perk;

import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.common.perk.AllocationStatus;

/** Resolve current resources on each draw; never retain sprite instances across resource reloads. */
final class PerkSprites {
    private PerkSprites() {}

    static SpriteSheetResource point(AllocationStatus status) {
        return switch (status) {
            case GRANTED, ALLOCATED -> SpritesAS.SPR_PERK_ACTIVE;
            case UNLOCKABLE -> SpritesAS.SPR_PERK_ACTIVATEABLE;
            case UNALLOCATED -> SpritesAS.SPR_PERK_INACTIVE;
        };
    }

    static SpriteSheetResource halo(AllocationStatus status) {
        return switch (status) {
            case GRANTED, ALLOCATED -> SpritesAS.SPR_PERK_HALO_ACTIVE;
            case UNLOCKABLE -> SpritesAS.SPR_PERK_HALO_ACTIVATEABLE;
            case UNALLOCATED -> SpritesAS.SPR_PERK_HALO_INACTIVE;
        };
    }
}
