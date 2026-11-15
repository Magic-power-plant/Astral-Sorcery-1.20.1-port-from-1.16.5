package hellfirepvp.astralsorcery.client.effect;

import hellfirepvp.astralsorcery.client.effect.context.base.BatchRenderContext;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.common.effect.visual.EffectBuilder;
import hellfirepvp.astralsorcery.common.effect.visual.EffectHandle;
import hellfirepvp.astralsorcery.common.effect.visual.Effects;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EffectsAdapterTest {

    @Test
    void commonBuilderUsesExistingRendererConfigurationWithoutLeakingBetweenSpawns() {
        // Building does not use textures or a render thread. Actual registration needs a running client.
        BatchRenderContext<FXFacingParticle> context = new BatchRenderContext<>(
                (SpriteSheetResource) null, null, (ctx, pos) -> new FXFacingParticle(pos));
        EffectBuilder<FXFacingParticle> builder = Effects.of(context);
        assertInstanceOf(EffectHelper.Builder.class, builder);
        EffectHelper.Builder<FXFacingParticle> clientBuilder = (EffectHelper.Builder<FXFacingParticle>) builder;
        UUID owner = UUID.randomUUID();
        Vec3i origin = new Vec3i(5, 8, -11);

        assertSame(builder, builder.setOwner(owner));
        assertSame(builder, builder.setPosition(origin));
        assertSame(builder, builder.setIgnoreLimit(true));
        assertSame(context, clientBuilder.getContext());
        assertEquals(owner, clientBuilder.getOwner());
        assertEquals(origin, clientBuilder.getPosition());
        assertTrue(clientBuilder.ignoresSpawnLimit());

        EffectHelper.Builder<FXFacingParticle> next = (EffectHelper.Builder<FXFacingParticle>) Effects.of(context);
        assertNotSame(builder, next);
        assertSame(context, next.getContext());
        assertNull(next.getOwner());
        assertEquals(Vec3i.ZERO, next.getPosition());
        assertFalse(next.ignoresSpawnLimit());
        assertSame(builder, builder.setOwner(null));
        assertNull(clientBuilder.getOwner());
    }

    @Test
    void commonHandleUsesTheExistingEntityLifecycleState() {
        FXFacingParticle particle = new FXFacingParticle(new Vector3());
        particle.setMaxAge(50);
        EffectHandle handle = particle;
        assertTrue(handle.isRemoved());
        particle.setActive();
        assertFalse(handle.isRemoved());
        assertFalse(particle.canRemove());
        handle.requestRemoval();
        assertTrue(particle.canRemove());
        particle.flagAsRemoved();
        assertTrue(handle.isRemoved());
        assertFalse(particle.canRemove());
        particle.setActive();
        assertFalse(handle.isRemoved());
        assertEquals(50, particle.getMaxAge());
    }
}
