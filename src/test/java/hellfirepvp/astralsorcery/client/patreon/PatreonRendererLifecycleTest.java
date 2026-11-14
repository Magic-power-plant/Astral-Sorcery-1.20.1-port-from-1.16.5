package hellfirepvp.astralsorcery.client.patreon;

import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingSprite;
import hellfirepvp.astralsorcery.client.patreon.effect.TypeFlare;
import hellfirepvp.astralsorcery.client.patreon.entity.PatreonFlareRenderer;
import hellfirepvp.astralsorcery.common.patreon.effect.FlareColor;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonFlare;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PatreonRendererLifecycleTest {
    @Test void closingAVisualRequestsRemovalWithoutRemovingSharedEntityState() {
        PatreonFlare entity = new PatreonFlare(UUID.randomUUID(), UUID.randomUUID());
        InspectableRenderer renderer = new InspectableRenderer(entity);
        FXFacingSprite sprite = new FXFacingSprite(new Vector3());
        sprite.setActive();
        renderer.attach(sprite);
        assertFalse(sprite.canRemove());
        renderer.close();
        assertTrue(sprite.canRemove());
        assertTrue(renderer.closed());
        assertFalse(entity.isRemoved(), "Client render distance and config must not remove sync state");
        assertDoesNotThrow(renderer::close);
    }

    @Test void sharedEntityRetirementStopsRendererRefreshAndPositionTracksSharedState() {
        PatreonFlare entity = new PatreonFlare(UUID.randomUUID(), UUID.randomUUID());
        InspectableRenderer renderer = new InspectableRenderer(entity);
        entity.getPos().add(4, 5, 6);
        assertEquals(entity.getPos(), renderer.position());
        assertFalse(renderer.closed());
        entity.setRemoved(true);
        assertTrue(renderer.closed());
    }

    private static final class InspectableRenderer extends PatreonFlareRenderer {
        InspectableRenderer(PatreonFlare entity) { super(entity, new TypeFlare(entity.getEffectUUID(), FlareColor.BLUE)); }
        void attach(FXFacingSprite sprite) { clientSprite = sprite; }
        boolean closed() { return isRemoved(); }
        Vector3 position() { return getPos(); }
    }
}
