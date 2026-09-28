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
import net.minecraft.client.renderer.RenderType;

import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderTypeDecorator
 * Created by HellFirePvP
 * Date: 06.06.2020 / 20:23
 *
 * Ported from ObserverLib 1.20.1 (hellfirepvp.observerlib.client.util.RenderTypeDecorator);
 * embedded per the source-only ObserverLib policy.
 */
public class RenderTypeDecorator extends RenderType {

    private final RenderType decorated;
    private final Runnable afterSetup;
    private final Runnable beforeClean;

    private RenderTypeDecorator(RenderType type, Runnable afterSetup, Runnable beforeClean) {
        super(type.toString(), type.format(), type.mode(), type.bufferSize(), type.affectsCrumbling(), false, () -> {}, () -> {});
        this.decorated = type;
        this.afterSetup = afterSetup;
        this.beforeClean = beforeClean;
    }

    public static RenderTypeDecorator decorate(RenderType type) {
        return new RenderTypeDecorator(type, () -> {}, () -> {});
    }

    public static RenderTypeDecorator wrapSetup(RenderType type, Runnable setup, Runnable clean) {
        return new RenderTypeDecorator(type, setup, clean);
    }

    @Override
    public void setupRenderState() {
        this.decorated.setupRenderState();
        this.afterSetup.run();
    }

    @Override
    public void clearRenderState() {
        this.beforeClean.run();
        this.decorated.clearRenderState();
    }

    @Override
    public void end(BufferBuilder buf, VertexSorting sort) {
        super.end(buf, sort);
    }

    @Override
    public boolean canConsolidateConsecutiveGeometry() {
        return this.decorated.canConsolidateConsecutiveGeometry();
    }

    @Override
    public String toString() {
        return this.decorated.toString();
    }

    @Override
    public int bufferSize() {
        return this.decorated.bufferSize();
    }

    @Override
    public VertexFormat format() {
        return this.decorated.format();
    }

    @Override
    public VertexFormat.Mode mode() {
        return this.decorated.mode();
    }

    @Override
    public Optional<RenderType> outline() {
        return this.decorated.outline();
    }

    @Override
    public boolean isOutline() {
        return this.decorated.isOutline();
    }

    @Override
    public boolean affectsCrumbling() {
        return this.decorated.affectsCrumbling();
    }

    @Override
    public Optional<RenderType> asOptional() {
        return Optional.of(this);
    }
}
