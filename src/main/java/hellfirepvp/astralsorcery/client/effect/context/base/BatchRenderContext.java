/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.effect.context.base;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import hellfirepvp.astralsorcery.client.effect.EntityDynamicFX;
import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHandler;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.render.IDrawRenderTypeBuffer;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.util.draw.RenderInfo;
import hellfirepvp.astralsorcery.client.util.draw.RenderTypeDecorator;
import hellfirepvp.astralsorcery.common.effect.visual.EffectBuilder;
import hellfirepvp.astralsorcery.common.effect.visual.EffectTemplate;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.order.OrderSortable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.BiFunction;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BatchRenderContext
 * Created by HellFirePvP
 * Date: 07.07.2019 / 10:58
 */
public class BatchRenderContext<T extends EntityVisualFX> extends OrderSortable implements EffectTemplate<T> {

    private static int counter = 0;

    private final int id;
    private final SpriteSheetResource sprite;
    private boolean drawWithTexture = true;
    protected RenderType renderType;
    protected BiFunction<BatchRenderContext<T>, Vector3, T> particleCreator;

    public BatchRenderContext(RenderType renderType,
                              BiFunction<BatchRenderContext<T>, Vector3, T> particleCreator) {
        this(new SpriteSheetResource(BlockAtlasTexture.getInstance()), renderType, particleCreator);
    }

    public BatchRenderContext(AbstractRenderableTexture texture,
                              RenderType renderType,
                              BiFunction<BatchRenderContext<T>, Vector3, T> particleCreator) {
        this(new SpriteSheetResource(texture), renderType, particleCreator);
    }

    public BatchRenderContext(SpriteSheetResource sprite,
                              RenderType renderType,
                              BiFunction<BatchRenderContext<T>, Vector3, T> particleCreator) {
        this.id = counter++;
        this.sprite = sprite;
        this.renderType = renderType;
        this.particleCreator = particleCreator.andThen(fx -> {
            int frames = this.sprite.getFrameCount();
            if (frames > 1) {
                fx.setMaxAge(frames);
            }
            return fx;
        });
    }

    @Override
    public EffectBuilder<T> createEffectBuilder() {
        return EffectHelper.of(this);
    }

    @Override
    public void refreshEffect(T effect) {
        EffectHelper.refresh(effect, this);
    }

    public T makeParticle(Vector3 pos) {
        return this.particleCreator.apply(this, pos);
    }

    public BatchRenderContext<T> setDrawWithTexture(boolean drawWithTexture) {
        this.drawWithTexture = drawWithTexture;
        return this;
    }

    public SpriteSheetResource getSprite() {
        return sprite;
    }

    public void renderAll(List<EffectHandler.PendingEffect> effects, PoseStack renderStack, IDrawRenderTypeBuffer drawBuffer, float pTicks) {
        //Erase type due to impossible typing
        BatchRenderContext blankCtx = this;
        effects.stream()
                .filter(effect -> effect.getEffect() instanceof EntityDynamicFX)
                .forEach(effect -> ((EntityDynamicFX) effect.getEffect()).renderNow(blankCtx, renderStack, drawBuffer, pTicks));

        RenderType drawType = this.getRenderType();
        if (this.drawWithTexture) {
            // 1.16.5 additionally toggled RenderSystem.enableTexture()/disableTexture() around the binds; the fixed-function
            // texture toggle no longer exists in 1.20.1, binding the texture into shader sampler 0 is sufficient.
            drawType = RenderTypeDecorator.wrapSetup(this.getRenderType(),
                    () -> this.getSprite().bindTexture(),
                    () -> BlockAtlasTexture.getInstance().bindTexture());
        }
        VertexConsumer buf = drawBuffer.getBuffer(drawType);
        effects.forEach(effect -> effect.getEffect().render(this, renderStack, buf, pTicks));
        this.drawBatched(buf, drawBuffer);
    }

    private void drawBatched(VertexConsumer buf, IDrawRenderTypeBuffer renderTypeBuffer) {
        if (buf instanceof BufferBuilder && this.getRenderType().mode() == VertexFormat.Mode.QUADS) {
            // 1.16.5: BufferBuilder.sortVertexData(x, y, z) with ActiveRenderInfo.getProjectedView()
            // VERIFY_PORT[AS-VFX-01]: translucent quad ordering - vertices are camera-relative, so VertexSorting.DISTANCE_TO_ORIGIN may be the more correct sort key than the absolute camera position used by 1.16.5
            Vec3 view = RenderInfo.getInstance().getARI().getPosition();
            ((BufferBuilder) buf).setQuadSorting(VertexSorting.byDistance((float) view.x, (float) view.y, (float) view.z));
        }
        renderTypeBuffer.draw();
    }

    public RenderType getRenderType() {
        return renderType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchRenderContext that = (BatchRenderContext) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
