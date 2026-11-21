package hellfirepvp.astralsorcery.client.effect;

import hellfirepvp.astralsorcery.client.effect.context.base.BatchRenderContext;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.effect.vfx.FXLightbeam;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.api.effect.EffectSpec;
import hellfirepvp.astralsorcery.common.effect.visual.spec.EffectTemplateIds;
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class EffectSpecAdapterTest {

    @Test
    void propertiesAreAppliedBeforeSubmissionAndValuesAreIsolatedPerParticle() {
        var previous = EffectTemplatesAS.GENERIC_PARTICLE;
        try {
            EffectTemplatesAS.GENERIC_PARTICLE = context(position -> new FXFacingParticle(position));
            EffectSpec spec = EffectSpec.builder(EffectTemplateIds.GENERIC_PARTICLE)
                    .maxAge(20).scale(2).alpha(0.8F).color(0x2384BD)
                    .motion(new Vector3(0.1, 0.2, -0.3)).gravity(0.05F)
                    .alphaMode(EffectSpec.AlphaMode.FADE_OUT).scaleMode(EffectSpec.ScaleMode.SHRINK).build();
            var builder = ClientEffectSpecs.REGISTRY.createEffectBuilder(spec);
            UUID owner = UUID.randomUUID();
            builder.setOwner(owner).setIgnoreLimit(true);
            Vector3 position = new Vector3(3, 4, 5);
            EntityVisualFX first = builder.createParticle(position);
            EntityVisualFX second = builder.createParticle(position);
            position.addY(100);
            assertTrue(first.isRemoved(), "Creating/configuring does not activate or queue a particle");
            assertNotEquals(first.getId(), second.getId());
            assertEquals(20, first.getMaxAge());
            assertEquals(2, first.getScale(0));
            assertEquals(new Color(0x2384BD), first.getColor(0));
            assertEquals(new Vector3(3, 4, 5), first.getPosition());
            assertEquals(owner, builder.getOwner());
            assertTrue(builder.ignoresSpawnLimit());
            first.tick();
            assertEquals(0.15, first.getMotion().getY(), 0.000001);
            assertEquals(new Vector3(3, 4, 5), second.getPosition());
            assertEquals(0.2, second.getMotion().getY(), 0.000001);
            assertEquals((int) (0.95F * 0.8F * 255), first.getAlpha(0));
            assertEquals(1.9F, first.getScale(1), 0.000001F);
            var fresh = ClientEffectSpecs.REGISTRY.createEffectBuilder(spec);
            assertNotSame(builder, fresh);
            assertNull(fresh.getOwner());
            assertFalse(fresh.ignoresSpawnLimit());
        } finally {
            EffectTemplatesAS.GENERIC_PARTICLE = previous;
        }
    }

    @Test
    void omittedPropertiesPreserveTemplateConfigurationAndReResolveReloadedContexts() {
        var previous = EffectTemplatesAS.GENERIC_PARTICLE;
        try {
            EffectTemplatesAS.GENERIC_PARTICLE = context(position -> new FXFacingParticle(position)
                    .setScaleMultiplier(0.2F).setAlphaMultiplier(0.85F)
                    .color(VFXColorFunction.constant(Color.CYAN)).alpha(VFXAlphaFunction.PYRAMID).setMaxAge(72));
            EffectSpec spec = EffectSpec.builder(EffectTemplateIds.GENERIC_PARTICLE).build();
            var firstBuilder = ClientEffectSpecs.REGISTRY.createEffectBuilder(spec);
            EntityVisualFX first = firstBuilder.createParticle(new Vector3());
            assertEquals(72, first.getMaxAge(), "Sprite/template lifetime is not reset to 40");
            assertEquals(0.2F, first.getScaleMultiplier());
            assertEquals(0.85F, first.getAlphaMultiplier());
            assertEquals(Color.CYAN, first.getColor(0));
            assertEquals(0, first.getAlpha(0), "Template pyramid is preserved");
            EffectTemplatesAS.GENERIC_PARTICLE = context(position -> new FXFacingParticle(position).setMaxAge(96));
            var secondBuilder = ClientEffectSpecs.REGISTRY.createEffectBuilder(spec);
            assertSame(EffectTemplatesAS.GENERIC_PARTICLE, secondBuilder.getContext());
            assertNotSame(firstBuilder.getContext(), secondBuilder.getContext());
            assertEquals(96, secondBuilder.createParticle(new Vector3()).getMaxAge());
        } finally {
            EffectTemplatesAS.GENERIC_PARTICLE = previous;
        }
    }

    @Test
    void beamsReceiveRelativeGeometryBeforeRegistration() {
        var previous = EffectTemplatesAS.LIGHTBEAM;
        try {
            EffectTemplatesAS.LIGHTBEAM = context(RecordingBeam::new);
            EffectSpec spec = EffectSpec.builder(EffectTemplateIds.LIGHTBEAM)
                    .beam(new Vector3(0, 6, 2), 0.5, 1.25).maxAge(64).build();
            var builder = ClientEffectSpecs.REGISTRY.createEffectBuilder(spec);
            RecordingBeam first = assertInstanceOf(RecordingBeam.class, builder.createParticle(new Vector3(10, 20, 30)));
            RecordingBeam second = assertInstanceOf(RecordingBeam.class, builder.createParticle(new Vector3(1, 2, 3)));
            assertEquals(new Vector3(10, 26, 32), first.endpoint);
            assertEquals(new Vector3(1, 8, 5), second.endpoint);
            assertEquals(0.5, first.fromSize);
            assertEquals(1.25, first.toSize);
            assertEquals(64, first.getMaxAge());
            assertTrue(first.isRemoved());
        } finally {
            EffectTemplatesAS.LIGHTBEAM = previous;
        }
    }

    @Test
    void badIdsAndGeometryFailBeforeContextLookupOrCreation() {
        assertThrows(IllegalArgumentException.class, () -> ClientEffectSpecs.REGISTRY.createEffectBuilder(
                EffectSpec.builder(ResourceLocation.fromNamespaceAndPath("example", "unknown")).build()));
        assertThrows(IllegalArgumentException.class, () -> ClientEffectSpecs.REGISTRY.createEffectBuilder(
                EffectSpec.builder(EffectTemplateIds.LIGHTBEAM).build()));
        assertThrows(IllegalArgumentException.class, () -> ClientEffectSpecs.REGISTRY.createEffectBuilder(
                EffectSpec.builder(EffectTemplateIds.GENERIC_PARTICLE).beam(new Vector3(0, 1, 0), 1, 1).build()));
        var previous = EffectTemplatesAS.GENERIC_PARTICLE;
        try {
            EffectTemplatesAS.GENERIC_PARTICLE = null;
            assertThrows(IllegalStateException.class, () -> ClientEffectSpecs.REGISTRY.createEffectBuilder(
                    EffectSpec.builder(EffectTemplateIds.GENERIC_PARTICLE).build()));
        } finally {
            EffectTemplatesAS.GENERIC_PARTICLE = previous;
        }
    }

    @Test
    void customTypedBindingsComposeWithTheFacadeWithoutUncheckedCasts() {
        EffectSpecRegistry registry = new EffectSpecRegistry();
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("example", "particle");
        AtomicReference<EffectSpec> received = new AtomicReference<>();
        var context = context(FXFacingParticle::new);
        EffectSpecAdapter<FXFacingParticle> adapter = new EffectSpecAdapter<>() {
            @Override
            public void validate(EffectSpec spec) { received.set(spec); }
            @Override
            public void apply(FXFacingParticle effect, EffectSpec spec) {
                assertEquals(0.5F, effect.getScaleMultiplier(), "Common properties precede template specialization");
                effect.setMaxAge(123);
            }
        };
        registry.register(id, () -> context, adapter);
        EffectSpec spec = EffectSpec.builder(id).scale(0.5F).build();
        assertInstanceOf(EffectHelper.Builder.class, Effects.of(spec, registry));
        assertSame(spec, received.get());
        assertEquals(123, registry.createEffectBuilder(spec).createParticle(new Vector3()).getMaxAge());
        assertThrows(IllegalArgumentException.class, () -> registry.register(id, () -> context, adapter));
    }

    @Test
    void spawnRunsTheInitializerBeforeTouchingTheMinecraftRegistrar() {
        IllegalStateException failure = new IllegalStateException("initialization failed");
        EffectHelper.Builder<FXFacingParticle> builder = new EffectHelper.Builder<>(context(FXFacingParticle::new),
                effect -> { throw failure; });
        // The registrar requires a running Minecraft instance. Seeing our failure proves it was not reached.
        assertSame(failure, assertThrows(IllegalStateException.class, () -> builder.spawn(new Vector3(2, 3, 4))));
        assertEquals(new Vector3(2, 3, 4).toBlockPos(), builder.getPosition());
    }

    private static <T extends EntityVisualFX> BatchRenderContext<T> context(Function<Vector3, T> factory) {
        // Exercise the real builder and adapters without initializing GPU assets or starting Minecraft.
        return new BatchRenderContext<T>((SpriteSheetResource) null, null, (ctx, pos) -> factory.apply(pos)) {
            @Override
            public T makeParticle(Vector3 position) { return factory.apply(position); }
        };
    }

    private static final class RecordingBeam extends FXLightbeam {
        Vector3 endpoint;
        double fromSize;
        double toSize;

        RecordingBeam(Vector3 position) { super(position); }

        @Override
        public FXLightbeam setup(Vector3 endpoint, double fromSize, double toSize) {
            this.endpoint = endpoint;
            this.fromSize = fromSize;
            this.toSize = toSize;
            return super.setup(endpoint, fromSize, toSize);
        }
    }
}
