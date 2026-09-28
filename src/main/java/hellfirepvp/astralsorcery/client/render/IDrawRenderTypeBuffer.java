/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IDrawRenderTypeBuffer
 * Created by HellFirePvP
 * Date: 06.06.2020 / 09:38
 *
 * 1.16.5 -&gt; 1.20.1: IRenderTypeBuffer -&gt; MultiBufferSource, IRenderTypeBuffer.Impl -&gt;
 * MultiBufferSource.BufferSource, IRenderTypeBuffer.getImpl(..) -&gt; MultiBufferSource.immediate(..),
 * Impl.finish(..) -&gt; BufferSource.endBatch(..), Tessellator.getInstance().getBuffer() -&gt;
 * Tesselator.getInstance().getBuilder(), IVertexBuilder -&gt; VertexConsumer.
 */
public interface IDrawRenderTypeBuffer extends MultiBufferSource {

    public void draw();

    public void draw(RenderType type);

    public static IDrawRenderTypeBuffer defaultBuffer() {
        return of(MultiBufferSource.immediate(Tesselator.getInstance().getBuilder()));
    }

    public static IDrawRenderTypeBuffer of(MultiBufferSource.BufferSource drawBuffer) {
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

}
