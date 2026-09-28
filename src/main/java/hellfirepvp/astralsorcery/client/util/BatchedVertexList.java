/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BatchedVertexList
 * Created by HellFirePvP
 * Date: 13.01.2020 / 21:03
 */
public class BatchedVertexList {

    private final VertexFormat vFormat;
    private VertexBuffer vbo = null;
    private boolean initialized = false;

    public BatchedVertexList(VertexFormat vFormat) {
        this.vFormat = vFormat;
    }

    public void batch(Consumer<BufferBuilder> batchFn) {
        if (this.initialized) {
            return;
        }

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        //1.16.5: new VertexBuffer(VertexFormat) - 1.20.1 buffers derive their format from the uploaded data and
        //only need an upload hint instead; the batched geometry here is built exactly once, hence STATIC.
        this.vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        batchFn.accept(buf);
        //1.16.5: buf.finishDrawing() + vbo.upload(buf) -> 1.20.1: BufferBuilder#end() hands over a RenderedBuffer
        BufferBuilder.RenderedBuffer rendered = buf.end();
        this.vbo.bind();
        this.vbo.upload(rendered);
        VertexBuffer.unbind();

        this.initialized = true;
    }

    public void render(PoseStack renderStack) {
        if (!this.initialized) {
            return;
        }

        this.vbo.bind();
        //1.16.5 set up/cleared the fixed-function buffer state around the draw call
        //(vFormat.setupBufferState(0L) / clearBufferState()) and passed GL11.GL_QUADS explicitly.
        //VERIFY_PORT[AS-BVL-01]: In 1.20.1 the vertex format *and* the draw mode are baked into the uploaded
        //RenderedBuffer, so both are implied by whatever the batch function began the BufferBuilder with;
        //the attribute setup happens inside drawWithShader.
        if (RenderSystem.getShader() == null) {
            RenderingUtils.setupShaderFor(this.vFormat);
        }
        this.vbo.drawWithShader(renderStack.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        VertexBuffer.unbind();
    }

    public void reset() {
        if (this.vbo != null) {
            this.vbo.close();
        }

        this.initialized = false;
    }
}
