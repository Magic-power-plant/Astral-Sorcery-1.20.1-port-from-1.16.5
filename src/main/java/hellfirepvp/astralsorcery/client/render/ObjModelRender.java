/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.util.draw.BufferDecoratorBuilder;
import hellfirepvp.astralsorcery.client.util.obj.WavefrontObject;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ObjModelRender
 * Created by HellFirePvP
 * Date: 05.04.2020 / 10:59
 */
public class ObjModelRender {

    private static WavefrontObject crystalModel;
    //private static VertexBuffer vboCrystal;

    private static WavefrontObject celestialWingsModel;
    private static VertexBuffer vboCelestialWings;

    private static WavefrontObject wraithWingsModel;
    private static VertexBuffer wraithWingsBones, wraithWingsWing;

    public static void renderCrystal(PoseStack renderStack, VertexConsumer buf, Runnable drawFn) {
        if (crystalModel == null) {
            crystalModel = AssetLoader.loadObjModel(AssetLoader.ModelLocation.OBJ, "crystal");
        }
        //1.16.5 kept a commented-out VBO path here; it depended on the removed
        //VertexFormat#setupBufferState/VertexBuffer#draw(Matrix4f, int) API and stays disabled.

        //1.16.5 wrapped the draw into RenderSystem.pushMatrix/loadIdentity/multMatrix(renderStack) because
        //the obj vertices are emitted in raw model space. The legacy matrix stack is gone in 1.20.1, so the
        //pose is applied per-vertex instead.
        //VERIFY_PORT[AS-OBJ-01]: in-game parity of crystal obj placement/orientation vs 1.16.5 ObjModelRender.renderCrystal.
        crystalModel.render(renderStack, buf);
        drawFn.run();
    }

    public static void renderCelestialWings(PoseStack renderStack) {
        if (celestialWingsModel == null) {
            celestialWingsModel = AssetLoader.loadObjModel(AssetLoader.ModelLocation.OBJ, "celestial_wings");
        }
        if (vboCelestialWings == null) {
            int[] lightGray = new int[] { 178, 178, 178, 255 };
            BufferDecoratorBuilder.withColor((r, g, b, a) -> lightGray)
                    .decorate(Tesselator.getInstance().getBuilder(),
                            (BufferBuilder decorated) -> vboCelestialWings = celestialWingsModel.batch(decorated));
        }
        drawBuffer(vboCelestialWings, renderStack);
    }

    public static void renderWraithWings(PoseStack renderStack) {
        if (wraithWingsModel == null) {
            wraithWingsModel = AssetLoader.loadObjModel(AssetLoader.ModelLocation.OBJ, "wraith_wings");
        }

        if (wraithWingsBones == null) {
            int[] gray = new int[] { 77, 77, 77, 255 };
            BufferDecoratorBuilder.withColor((r, g, b, a) -> gray)
                    .decorate(Tesselator.getInstance().getBuilder(),
                            (BufferBuilder decorated) -> wraithWingsBones = wraithWingsModel.batchOnly(decorated, "Bones"));
        }
        if (wraithWingsWing == null) {
            int[] black = new int[] { 0, 0, 0, 255 };
            BufferDecoratorBuilder.withColor((r, g, b, a) -> black)
                    .decorate(Tesselator.getInstance().getBuilder(),
                            (BufferBuilder decorated) -> wraithWingsWing = wraithWingsModel.batchOnly(decorated, "Wing"));
        }

        drawBuffer(wraithWingsBones, renderStack);
        drawBuffer(wraithWingsWing, renderStack);
    }

    /**
     * 1.16.5 equivalent:
     * <pre>
     *     vbo.bindBuffer();
     *     RenderTypesAS.POSITION_COLOR_TEX_NORMAL.setupBufferState(0L);
     *     vbo.draw(renderStack.getLast().getMatrix(), model.getGLDrawingMode());
     *     RenderTypesAS.POSITION_COLOR_TEX_NORMAL.clearBufferState();
     *     VertexBuffer.unbindBuffer();
     * </pre>
     * 1.20.1 has no manual buffer-state setup/draw-mode argument; the uploaded buffer carries both and the
     * draw needs an explicit shader plus the projection matrix. The callers of renderCelestialWings /
     * renderWraithWings already ran RenderType#setupRenderState, which installs the shader on RenderSystem.
     */
    private static void drawBuffer(VertexBuffer vbo, PoseStack renderStack) {
        if (vbo == null || vbo.isInvalid()) {
            return;
        }
        ShaderInstance shader = RenderSystem.getShader();
        if (shader == null) {
            // VERIFY_PORT[AS-OBJ-01]: the wing RenderTypes are expected to install their own shader state; fallback for a null shader
            shader = GameRenderer.getPositionColorTexShader();
        }
        Matrix4f projection = RenderSystem.getProjectionMatrix();

        vbo.bind();
        vbo.drawWithShader(new Matrix4f(RenderSystem.getModelViewMatrix()).mul(renderStack.last().pose()), projection, shader);
        VertexBuffer.unbind();
    }
}
