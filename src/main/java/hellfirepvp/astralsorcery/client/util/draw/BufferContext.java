/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.draw;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BufferContext
 * Created by HellFirePvP
 * Date: 08.07.2019 / 20:39
 */
public class BufferContext extends BufferBuilder {

    private boolean inDrawing = false;
    @Nullable
    private VertexFormat activeFormat = null;

    BufferContext(int size) {
        super(size);
    }

    /**
     * 1.16.5 compatibility bridge: the original {@code BufferBuilder#begin(int, VertexFormat)} took a raw
     * GL draw mode constant. 1.20.1 replaced it with the {@link VertexFormat.Mode} enum; this overload keeps the
     * old call sites (i.e. {@code BatchPerkContext#beginDrawingPerks}) working unchanged.
     */
    public void begin(int glMode, VertexFormat format) {
        this.begin(RenderingUtils.toDrawMode(glMode), format);
    }

    @Override
    public void begin(VertexFormat.Mode mode, VertexFormat format) {
        super.begin(mode, format);

        this.activeFormat = format;
        this.inDrawing = true;
    }

    @Override
    public void setQuadSorting(VertexSorting sorting) {
        if (this.inDrawing) {
            super.setQuadSorting(sorting);
        }
    }

    /**
     * 1.16.5 equivalent of {@code BufferBuilder#sortVertexData(float, float, float)}; 1.20.1 expresses the very
     * same operation through a {@link VertexSorting} strategy.
     */
    public void sortVertexData(float x, float y, float z) {
        this.setQuadSorting(VertexSorting.byDistance(x, y, z));
    }

    public void draw() {
        if (this.inDrawing) {
            if (this.activeFormat != null) {
                RenderingUtils.setupShaderFor(this.activeFormat);
            }
            RenderingUtils.finishDrawing(this);
            this.inDrawing = false;
            this.activeFormat = null;
        }
    }

}
