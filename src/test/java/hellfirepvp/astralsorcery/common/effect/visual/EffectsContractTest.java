package hellfirepvp.astralsorcery.common.effect.visual;

import hellfirepvp.astralsorcery.api.effect.EffectSpec;
import hellfirepvp.astralsorcery.api.effect.EffectSpecBackend;
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.common.effect.visual.spec.EffectTemplateIds;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.Vec3i;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EffectsContractTest {

    @Test
    void independentBackendCanSpawnAndConfigureConcreteHandles() {
        RecordingTemplate template = new RecordingTemplate();
        UUID owner = UUID.randomUUID();
        Vector3 position = new Vector3(3.5, 4, -2);
        RecordingEffect effect = Effects.of(template).setOwner(owner)
                .setIgnoreLimit(true).setPosition(Vec3i.ZERO).spawn(position);

        assertSame(template.spawned, effect);
        assertEquals(owner, template.builder.owner);
        assertTrue(template.builder.ignoreLimit);
        assertEquals(position.toBlockPos(), template.builder.position);
        assertEquals(position, effect.position);
        assertNotSame(position, effect.position);
        // Registration can be refused by a backend; the result must still be usable by fluent callers.
        assertTrue(effect.isRemoved());
        effect.requestRemoval();
        assertTrue(effect.removalRequested);
    }

    @Test
    void refreshOnlyResubmitsRemovedHandlesWithoutOwningTheirLifecycle() {
        RecordingTemplate template = new RecordingTemplate();
        RecordingEffect effect = new RecordingEffect(new Vector3());
        effect.age = 17;
        effect.removed = false;

        Effects.refresh(effect, template);
        assertEquals(0, template.refreshes);

        effect.removed = true;
        effect.requestRemoval();
        Effects.refresh(effect, template);
        assertEquals(1, template.refreshes);
        assertSame(effect, template.refreshed);
        assertEquals(17, effect.age);
        assertTrue(effect.removalRequested);
        assertTrue(effect.isRemoved(), "Only the backend can accept and activate a handle");

        // A rejected submission can be retried later. The facade must not cache/suppress it.
        Effects.refresh(effect, template);
        assertEquals(2, template.refreshes);
    }

    @Test
    void sourceSubmissionKeepsIdentityConcreteTypeAndState() {
        RecordingSource source = new RecordingSource();
        source.age = 7;
        RecordingSource result = Effects.spawnSource(source);
        assertSame(source, result);
        assertEquals(1, result.submissions);
        assertEquals(7, result.age);
        assertTrue(result.isRemoved());
        result.requestRemoval();
        assertTrue(source.removalRequested);
    }

    @Test
    void backendFailuresAreNotSilentlyDiscarded() {
        IllegalStateException failure = new IllegalStateException("backend unavailable");
        EffectTemplate<RecordingEffect> failingTemplate = new EffectTemplate<>() {
            @Override
            public EffectBuilder<RecordingEffect> createEffectBuilder() { throw failure; }

            @Override
            public void refreshEffect(RecordingEffect effect) { throw failure; }
        };
        assertSame(failure, assertThrows(IllegalStateException.class, () -> Effects.of(failingTemplate)));
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> Effects.refresh(new RecordingEffect(new Vector3()), failingTemplate)));
        RecordingSource source = new RecordingSource() {
            @Override
            public void spawnEffectSource() { throw failure; }
        };
        assertSame(failure, assertThrows(IllegalStateException.class, () -> Effects.spawnSource(source)));
    }

    @Test
    void invalidCapabilitiesFailAtTheFacadeBoundary() {
        RecordingTemplate template = new RecordingTemplate();
        assertThrows(NullPointerException.class, () -> Effects.of(null));
        assertThrows(NullPointerException.class, () -> Effects.spawnSource(null));
        assertThrows(NullPointerException.class, () -> Effects.refresh(null, template));
        assertThrows(NullPointerException.class,
                () -> Effects.refresh(new RecordingEffect(new Vector3()), null));
        assertThrows(NullPointerException.class, () -> Effects.of(new RecordingTemplate() {
            @Override
            public EffectBuilder<RecordingEffect> createEffectBuilder() { return null; }
        }));
    }

    @Test
    void dataDescriptionsUseExplicitBackendsAndRetainSpawnOptions() {
        RecordingTemplate template = new RecordingTemplate();
        EffectSpec spec = EffectSpec.builder(EffectTemplateIds.GENERIC_PARTICLE).maxAge(75).build();
        EffectSpecBackend<RecordingEffect> backend = received -> {
            assertSame(spec, received);
            return template.createEffectBuilder();
        };
        UUID owner = UUID.randomUUID();
        RecordingEffect effect = Effects.of(spec, backend).setOwner(owner).setIgnoreLimit(true)
                .spawn(new Vector3(2, 3, 4));
        assertSame(effect, template.spawned);
        assertEquals(owner, template.builder.owner);
        assertTrue(template.builder.ignoreLimit);
        assertTrue(effect.isRemoved());
        assertThrows(NullPointerException.class, () -> Effects.of(null, backend));
        assertThrows(NullPointerException.class, () -> Effects.of(spec, null));
        assertThrows(NullPointerException.class, () -> Effects.of(spec, ignored -> null));
        IllegalArgumentException failure = new IllegalArgumentException("unknown template");
        assertSame(failure, assertThrows(IllegalArgumentException.class,
                () -> Effects.of(spec, ignored -> { throw failure; })));
    }

    // A separate backend proves the facade has no dependency on the game's renderer or singleton.
    private abstract static class RecordingHandle implements EffectHandle {
        boolean removed = true;
        boolean removalRequested;
        int age;

        @Override
        public boolean isRemoved() { return removed; }

        @Override
        public void requestRemoval() { removalRequested = true; }
    }

    private static final class RecordingEffect extends RecordingHandle {
        final Vector3 position;

        RecordingEffect(Vector3 position) { this.position = position.clone(); }
    }

    private static class RecordingSource extends RecordingHandle implements EffectSource {
        int submissions;

        @Override
        public void spawnEffectSource() { submissions++; }
    }

    private static class RecordingTemplate implements EffectTemplate<RecordingEffect> {
        RecordingBuilder builder;
        RecordingEffect spawned;
        RecordingEffect refreshed;
        int refreshes;

        @Override
        public EffectBuilder<RecordingEffect> createEffectBuilder() {
            builder = new RecordingBuilder(this);
            return builder;
        }

        @Override
        public void refreshEffect(RecordingEffect effect) {
            refreshed = effect;
            refreshes++;
        }
    }

    private static final class RecordingBuilder implements EffectBuilder<RecordingEffect> {
        final RecordingTemplate template;
        UUID owner;
        Vec3i position = Vec3i.ZERO;
        boolean ignoreLimit;

        RecordingBuilder(RecordingTemplate template) { this.template = template; }

        @Override
        public EffectBuilder<RecordingEffect> setOwner(UUID owner) { this.owner = owner; return this; }

        @Override
        public EffectBuilder<RecordingEffect> setPosition(Vec3i position) { this.position = position; return this; }

        @Override
        public EffectBuilder<RecordingEffect> setIgnoreLimit(boolean ignoreLimit) {
            this.ignoreLimit = ignoreLimit;
            return this;
        }

        @Override
        public RecordingEffect spawn(Vector3 position) {
            this.position = position.toBlockPos();
            template.spawned = new RecordingEffect(position);
            return template.spawned;
        }
    }
}
