package hellfirepvp.astralsorcery.client.screen.journal.perk;

import hellfirepvp.astralsorcery.common.perk.tree.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Client-owned model-to-renderer table. Subclasses inherit their nearest registered renderer. */
@OnlyIn(Dist.CLIENT)
public final class PerkRenderRegistry {
    public static final PerkRenderRegistry INSTANCE = new PerkRenderRegistry();
    private final Map<Class<?>, PerkRender> renderers = new HashMap<>();

    public PerkRenderRegistry() {
        register(PerkTreePoint.class, new PerkPointRenderer());
        register(PerkTreeMajor.class, new PerkHaloRenderer());
        register(PerkTreeGem.class, new PerkGemRenderer());
        register(PerkTreeConstellation.class, new PerkConstellationRenderer());
    }

    /** Register additional point types during client setup, before building journal draw batches. */
    public void register(Class<?> pointType, PerkRender renderer) {
        Objects.requireNonNull(pointType, "pointType");
        Objects.requireNonNull(renderer, "renderer");
        if (!PerkTreePoint.class.isAssignableFrom(pointType)) {
            throw new IllegalArgumentException("Not a perk point type: " + pointType.getName());
        }
        if (renderers.putIfAbsent(pointType, renderer) != null) {
            throw new IllegalArgumentException("Renderer already registered for " + pointType.getName());
        }
    }

    public PerkRender get(PerkTreePoint<?> point) {
        Objects.requireNonNull(point, "point");
        for (Class<?> type = point.getClass(); type != null; type = type.getSuperclass()) {
            PerkRender renderer = renderers.get(type);
            if (renderer != null) return renderer;
        }
        throw new IllegalArgumentException("No renderer for " + point.getClass().getName());
    }
}
