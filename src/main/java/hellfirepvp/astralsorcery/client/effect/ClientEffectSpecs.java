package hellfirepvp.astralsorcery.client.effect;

import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.effect.vfx.FXLightbeam;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.effect.visual.EffectSpec;
import hellfirepvp.astralsorcery.common.effect.visual.EffectTemplateIds;

/** Client-only built-in bindings. Constructing the registry does not dereference render assets. */
public final class ClientEffectSpecs {

    private static final EffectSpecAdapter<FXFacingParticle> PARTICLE = new EffectSpecAdapter<>() {
        @Override
        public void validate(EffectSpec spec) {
            if (spec.beam().isPresent()) {
                throw new IllegalArgumentException("Particle template does not support beam geometry: " + spec.template());
            }
        }

        @Override
        public void apply(FXFacingParticle effect, EffectSpec spec) {}
    };

    private static final EffectSpecAdapter<FXLightbeam> BEAM = new EffectSpecAdapter<>() {
        @Override
        public void validate(EffectSpec spec) {
            if (spec.beam().isEmpty()) {
                throw new IllegalArgumentException("Beam template requires beam geometry: " + spec.template());
            }
        }

        @Override
        public void apply(FXLightbeam effect, EffectSpec spec) {
            EffectSpec.Beam beam = spec.beam().orElseThrow();
            effect.setup(effect.getPosition().add(beam.endOffset().toVector3()), beam.fromSize(), beam.toSize());
        }
    };

    public static final EffectSpecRegistry REGISTRY = createRegistry();

    private ClientEffectSpecs() {}

    private static EffectSpecRegistry createRegistry() {
        EffectSpecRegistry registry = new EffectSpecRegistry();
        registry.register(EffectTemplateIds.GENERIC_PARTICLE, () -> EffectTemplatesAS.GENERIC_PARTICLE, PARTICLE);
        registry.register(EffectTemplateIds.GENERIC_DEPTH_PARTICLE, () -> EffectTemplatesAS.GENERIC_DEPTH_PARTICLE, PARTICLE);
        registry.register(EffectTemplateIds.GENERIC_GATEWAY_PARTICLE, () -> EffectTemplatesAS.GENERIC_GATEWAY_PARTICLE, PARTICLE);
        registry.register(EffectTemplateIds.CRYSTAL_BURST_1, () -> EffectTemplatesAS.CRYSTAL_BURST_1, PARTICLE);
        registry.register(EffectTemplateIds.CRYSTAL_BURST_2, () -> EffectTemplatesAS.CRYSTAL_BURST_2, PARTICLE);
        registry.register(EffectTemplateIds.CRYSTAL_BURST_3, () -> EffectTemplatesAS.CRYSTAL_BURST_3, PARTICLE);
        registry.register(EffectTemplateIds.GEM_CRYSTAL_BURST, () -> EffectTemplatesAS.GEM_CRYSTAL_BURST, PARTICLE);
        registry.register(EffectTemplateIds.GEM_CRYSTAL_BURST_SKY, () -> EffectTemplatesAS.GEM_CRYSTAL_BURST_SKY, PARTICLE);
        registry.register(EffectTemplateIds.GEM_CRYSTAL_BURST_DAY, () -> EffectTemplatesAS.GEM_CRYSTAL_BURST_DAY, PARTICLE);
        registry.register(EffectTemplateIds.GEM_CRYSTAL_BURST_NIGHT, () -> EffectTemplatesAS.GEM_CRYSTAL_BURST_NIGHT, PARTICLE);
        registry.register(EffectTemplateIds.COLLECTOR_BURST, () -> EffectTemplatesAS.COLLECTOR_BURST, PARTICLE);
        registry.register(EffectTemplateIds.LIGHTBEAM, () -> EffectTemplatesAS.LIGHTBEAM, BEAM);
        registry.register(EffectTemplateIds.LIGHTBEAM_TRANSFER, () -> EffectTemplatesAS.LIGHTBEAM_TRANSFER, BEAM);
        return registry;
    }
}
