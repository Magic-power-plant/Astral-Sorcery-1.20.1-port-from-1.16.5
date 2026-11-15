package hellfirepvp.astralsorcery.client.screen.journal.perk;

import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.AllocationStatus;
import hellfirepvp.astralsorcery.common.perk.tree.*;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.*;

class PerkRenderRegistryTest {
    @Test
    void builtinsResolveToSharedRenderersAndOnlyGemAndConstellationHaveDynamicPasses() {
        var registry = new PerkRenderRegistry();
        PerkRender ordinary = registry.get(new PerkTreePoint<>(null, new Point.Float()));
        PerkRender major = registry.get(new PerkTreeMajor<>(null, new Point.Float()));
        PerkRender gem = registry.get(new PerkTreeGem<>(null, new Point.Float()));
        PerkRender constellation = registry.get(new PerkTreeConstellation<>(null, new Point.Float(), null, 50));
        assertEquals(PerkPointRenderer.class, ordinary.getClass());
        assertEquals(PerkHaloRenderer.class, major.getClass());
        assertEquals(PerkGemRenderer.class, gem.getClass());
        assertEquals(PerkConstellationRenderer.class, constellation.getClass());
        assertFalse(ordinary instanceof DynamicPerkRender);
        assertFalse(major instanceof DynamicPerkRender);
        assertInstanceOf(DynamicPerkRender.class, gem);
        assertInstanceOf(DynamicPerkRender.class, constellation);
        assertSame(ordinary, registry.get(new PerkTreePoint<>(null, new Point.Float(42, 8))));
    }

    @Test
    void extensionsInheritNearestBindingAndCanRegisterAnExplicitRendererWithoutChangingTheModel() {
        var registry = new PerkRenderRegistry();
        var model = new ExtendedConstellation();
        PerkRender inherited = registry.get(model);
        assertInstanceOf(PerkConstellationRenderer.class, inherited);
        var custom = new PerkPointRenderer();
        registry.register(ExtendedConstellation.class, custom);
        assertSame(custom, registry.get(model));
        assertInstanceOf(PerkConstellationRenderer.class,
                registry.get(new PerkTreeConstellation<>(null, new Point.Float(), null, 40)));
        assertThrows(IllegalArgumentException.class, () -> registry.register(ExtendedConstellation.class, inherited));
        assertSame(custom, registry.get(model), "Duplicate registration must not replace the binding");
        assertThrows(IllegalArgumentException.class, () -> registry.register(String.class, custom));
        assertThrows(NullPointerException.class, () -> registry.register(null, custom));
        assertThrows(NullPointerException.class, () -> registry.get(null));
    }

    @Test
    void hitBoundsAndHaloMultipliersMatchAllAllocationStatesAndZoomLevels() {
        var registry = new PerkRenderRegistry();
        for (float scale : new float[]{0.1F, 0.35F, 1F}) {
            for (PerkTreePoint<?> point : new PerkTreePoint<?>[]{new PerkTreePoint<>(null, new Point.Float()),
                    new PerkTreeMajor<>(null, new Point.Float()), new PerkTreeGem<>(null, new Point.Float()),
                    new PerkTreeConstellation<>(null, new Point.Float(), null, 40),
                    new PerkTreeConstellation<>(null, new Point.Float(), null, 50)}) {
                PerkPointRenderer renderer = (PerkPointRenderer) registry.get(point);
                float radius = (point instanceof PerkTreeConstellation<?> constellation
                        ? constellation.getPerkSpriteSize() : point.getRenderSize()) * scale;
                Rectangle.Float bounds = renderer.getBounds(point, scale);
                assertEquals(-radius, bounds.x, 0.0001F);
                assertEquals(-radius, bounds.y, 0.0001F);
                assertEquals(2F * radius, bounds.width, 0.0001F);
                assertEquals(2F * radius, bounds.height, 0.0001F);
                if (renderer instanceof PerkHaloRenderer halo) {
                    for (AllocationStatus status : AllocationStatus.values()) {
                        boolean allocated = status == AllocationStatus.ALLOCATED || status == AllocationStatus.GRANTED;
                        float expected = point instanceof PerkTreeConstellation<?>
                                ? radius * (allocated ? 1.3F : 1F)
                                : radius * 0.8F * (allocated ? 1.5F : 1F);
                        assertEquals(expected, halo.getHaloSize(point, status, scale), 0.0001F);
                    }
                }
            }
        }
    }

    private static final class ExtendedConstellation extends PerkTreeConstellation<AbstractPerk> {
        private ExtendedConstellation() { super(null, new Point.Float(), null, 40); }
    }
}
