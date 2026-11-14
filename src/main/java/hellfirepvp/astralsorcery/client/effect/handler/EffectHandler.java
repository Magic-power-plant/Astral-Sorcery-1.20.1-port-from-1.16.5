/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.effect.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.effect.EffectProperties;
import hellfirepvp.astralsorcery.client.effect.EntityComplexFX;
import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.context.base.BatchRenderContext;
import hellfirepvp.astralsorcery.client.effect.source.FXSource;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.render.IDrawRenderTypeBuffer;
import hellfirepvp.astralsorcery.client.resource.AssetLibrary;
import hellfirepvp.astralsorcery.common.util.collection.AlternatingSet;
import hellfirepvp.astralsorcery.common.util.math.Counter;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.order.DependencySorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectHandler
 * Created by HellFirePvP
 * Date: 30.05.2019 / 00:09
 */
public final class EffectHandler {

    private static final Random STATIC_EFFECT_RAND = new Random();
    private static final EffectHandler INSTANCE = new EffectHandler();

    private boolean cleanRequested = false;
    private boolean acceptsNewEffects = false; //Wait for 1st tick to finish.
    private final List<PendingEffect> toAddBuffer = Lists.newLinkedList();

    private final AlternatingSet<FXSource<?, ?>> sources = new AlternatingSet<>();
    private final Map<BatchRenderContext<?>, List<PendingEffect>> effectMap = Maps.newLinkedHashMap();

    private List<BatchRenderContext<?>> orderedEffects = null;

    private EffectHandler() {}

    public static EffectHandler getInstance() {
        return INSTANCE;
    }

    public int getEffectCount() {
        final Counter c = new Counter(0);
        this.effectMap.values().stream().flatMap(Collection::stream).forEach(p -> c.increment());
        return c.getValue();
    }

    public void render(PoseStack renderStack, float pTicks) {
        if (this.orderedEffects == null || AssetLibrary.isReloading()) {
            return;
        }

        IDrawRenderTypeBuffer drawBuffer = defaultDrawBuffer();
        this.acceptsNewEffects = false;

        for (BatchRenderContext<?> ctx : this.orderedEffects) {
            List<PendingEffect> effects = this.effectMap.get(ctx);
            if (!effects.isEmpty()) {
                ctx.renderAll(effects, renderStack, drawBuffer, pTicks);
            }
        }

        this.acceptsNewEffects = true;
    }

    // 1.16.5: IDrawRenderTypeBuffer.defaultBuffer() -> IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer())
    // TODO_PORT[AS-CLI-02]: IDrawRenderTypeBuffer.defaultBuffer()/of(Impl) - move these statics onto IDrawRenderTypeBuffer once that file is ported
    private static IDrawRenderTypeBuffer defaultDrawBuffer() {
        return wrapDrawBuffer(MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()));
    }

    // 1.16.5: IDrawRenderTypeBuffer.of(IRenderTypeBuffer.Impl)
    private static IDrawRenderTypeBuffer wrapDrawBuffer(MultiBufferSource.BufferSource drawBuffer) {
        return new IDrawRenderTypeBuffer() {
            @Override
            public void draw() {
                drawBuffer.endBatch();
            }

            @Override
            public void draw(RenderType type) {
                drawBuffer.endBatch(type);
            }

            @Override
            public VertexConsumer getBuffer(RenderType renderType) {
                return drawBuffer.getBuffer(renderType);
            }
        };
    }

    void tick() throws IOException {
        if (this.cleanRequested) {
            this.toAddBuffer.clear();
            this.sources.clear();
            this.effectMap.values().forEach(effects -> effects.stream().map(PendingEffect::getEffect).forEach(EntityComplexFX::flagAsRemoved));
            this.effectMap.values().forEach(List::clear);
            this.cleanRequested = false;
        }

        Entity rView = Minecraft.getInstance().getCameraEntity();
        if (rView == null) {
            rView = Minecraft.getInstance().player;
        }
        if (rView == null) {
            cleanUp();
            return;
        }

        if (this.orderedEffects == null) {
            this.orderedEffects = DependencySorter.getSorted(EffectTemplatesAS.LIST_ALL_RENDER_CONTEXT);
            this.orderedEffects.forEach(ctx -> this.effectMap.put(ctx, new ArrayList<>()));
        }

        this.acceptsNewEffects = false;

        this.effectMap.values().forEach(l -> {
            Iterator<PendingEffect> iterator = l.iterator();
            while (iterator.hasNext()) {
                PendingEffect effect = iterator.next();
                EntityComplexFX fx = effect.getEffect();

                fx.tick();
                if (fx.canRemove()) {
                    iterator.remove();
                    fx.flagAsRemoved();
                }
            }
        });

        this.sources.forEach(src -> {
            src.tick();
            src.tickSpawnFX(new EffectRegistrar(src));
            if (src.canRemove()) {
                src.flagAsRemoved();
                return false;
            }
            return true;
        });

        this.acceptsNewEffects = true;

        this.toAddBuffer.forEach(this::registerUnsafe);
        this.toAddBuffer.clear();
    }

    void queueSource(FXSource<?, ?> source) {
        this.sources.add(source);
    }

    void queueParticle(PendingEffect pendingEffect) {
        if (this.acceptsNewEffects) {
            registerUnsafe(pendingEffect);
        } else {
            this.toAddBuffer.add(pendingEffect);
        }
    }

    private void registerUnsafe(PendingEffect pendingEffect) {
        if (!mayAcceptParticle(pendingEffect.getProperties())) {
            return;
        }
        EntityVisualFX effect = pendingEffect.getEffect();
        BatchRenderContext<?> ctx = pendingEffect.getProperties().getContext();
        pendingEffect.getProperties().applySpecialEffects(effect);

        List<PendingEffect> effects = this.effectMap.get(ctx);
        if (effects == null) {
            // Context not part of EffectTemplatesAS.LIST_ALL_RENDER_CONTEXT (or registered after first tick) -> cannot be rendered.
            return;
        }
        effects.add(pendingEffect);
        effect.setActive();
    }

    private boolean mayAcceptParticle(EffectProperties<?> properties) {
        if (properties.ignoresSpawnLimit()) {
            return true;
        }
        RenderingConfig.ParticleAmount cfg = RenderingConfig.CONFIG.particleAmount.get();
        if (!Minecraft.useFancyGraphics()) {
            cfg = cfg.less();
        }
        return cfg.shouldSpawn(STATIC_EFFECT_RAND);
    }

    public static void cleanUp() {
        getInstance().cleanRequested = true;
    }

    private static class EffectRegistrar<E extends EntityVisualFX> implements Function<Vector3, E> {

        private final FXSource<E, ?> source;

        private EffectRegistrar(FXSource<E, ?> source) {
            this.source = source;
        }

        @Override
        public E apply(Vector3 pos) {
            EffectHelper.Builder<E> prop = source.generateFX();
            E fx = prop.getContext().makeParticle(pos);
            PendingEffect effect = new PendingEffect(fx, prop);
            getInstance().registerUnsafe(effect);
            return fx;
        }
    }

    public static class PendingEffect {

        private final EntityVisualFX effect;
        private final EffectProperties<?> runProperties;

        PendingEffect(EntityVisualFX effect, EffectProperties<?> runProperties) {
            this.effect = effect;
            this.runProperties = runProperties;
        }

        EffectProperties<?> getProperties() {
            return runProperties;
        }

        public EntityVisualFX getEffect() {
            return effect;
        }
    }
}
