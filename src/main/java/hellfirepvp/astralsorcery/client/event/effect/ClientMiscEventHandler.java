/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event.effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.client.util.obj.WavefrontObject;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.joml.Matrix4f;

import java.util.zip.GZIPInputStream;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientMiscEventHandler
 * Created by HellFirePvP
 * Date: 30.05.2019 / 15:12
 */
public class ClientMiscEventHandler {

    private static boolean attemptLoad = false;
    private static WavefrontObject obj;
    private static ResourceLocation tex = AstralSorcery.key("textures/model/texw.png");
    private static VertexBuffer vboR, vboL;

    private ClientMiscEventHandler() {}

    //Obligatory, dev gimmick
    @OnlyIn(Dist.CLIENT)
    static void onRender(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        if (player == null) return;
        if (player.getUUID().hashCode() != 1529485240) return;

        if (!attemptLoad) {
            attemptLoad = true;
            ResourceLocation mod = new ResourceLocation(AstralSorcery.MODID + ":models/obj/modelassec.obj");
            try {
                obj = new WavefrontObject("astralSorcery:wingsrender", new GZIPInputStream(Minecraft.getInstance().getResourceManager().getResourceOrThrow(mod).open()));
            } catch (Exception exc) {}
        }
        if (attemptLoad && obj == null) {
            return;
        }

        if (player.isPassenger() || player.isFallFlying()) return;

        Vec3 motion = player.getDeltaMovement();

        boolean f = player.getAbilities().flying;
        float ma = f ? 15 : 5;
        float r = (ma * (Math.abs((ClientScheduler.getClientTick() % 80) - 40) / 40F)) +
                ((65 - ma) * Math.max(0, Math.min(1, (float) new Vector3(motion.x, 0, motion.z).length())));
        float rot = RenderingVectorUtils.interpolateRotation(player.yBodyRotO, player.yBodyRot, event.getPartialTick());

        PoseStack renderStack = event.getPoseStack();
        renderStack.pushPose();
        float swimAngle = player.getSwimAmount(event.getPartialTick());
        if (swimAngle > 0) {
            float waterPitch = player.isInWater() ? -90.0F - player.getXRot() : -90.0F;
            float bodySwimAngle = Mth.lerp(swimAngle, 0.0F, waterPitch);
            renderStack.mulPose(Axis.YP.rotationDegrees(180 - rot));
            renderStack.mulPose(Axis.XP.rotationDegrees(bodySwimAngle));
            if (player.isVisuallySwimming()) {
                renderStack.translate(0, -1, 0.3F);
            }
        } else {
            renderStack.mulPose(Axis.YP.rotationDegrees(180 - rot));
        }

        renderStack.scale(0.07F, 0.07F, 0.07F);
        renderStack.translate(0, 5.5, 0.7 - ((r / ma) * (f ? 0.5D : 0.2D)));

        // TODO_PORT[AS-CLI-02]: WavefrontObject.batchOnly(BufferBuilder, String...) - target client/util/obj/WavefrontObject is a stub; must return an uploaded com.mojang.blaze3d.vertex.VertexBuffer (VertexBuffer.upload(BufferBuilder.RenderedBuffer))
        if (vboR == null) {
            vboR = obj.batchOnly(Tesselator.getInstance().getBuilder(), "wR");
        }
        if (vboL == null) {
            vboL = obj.batchOnly(Tesselator.getInstance().getBuilder(), "wL");
        }

        // 1.16.5: setupRenderState + RenderSystem.enableTexture + TextureManager.bindTexture(tex);
        //         POSITION_COLOR_TEX_NORMAL.setupBufferState(0) + VertexBuffer.draw(matrix, GL_QUADS) + clearBufferState
        RenderTypesAS.MODEL_DEMON_WINGS.setupRenderState();
        RenderSystem.setShaderTexture(0, tex);
        ShaderInstance shader = RenderSystem.getShader();
        if (shader == null) {
            // VERIFY_PORT[AS-CLI-02]: MODEL_DEMON_WINGS is expected to provide its own shader state; fallback for a null shader
            shader = GameRenderer.getPositionColorTexShader();
        }
        Matrix4f projection = RenderSystem.getProjectionMatrix();

        renderStack.pushPose();
        renderStack.mulPose(Axis.YN.rotationDegrees(20 + r));
        vboR.bind();
        // 1.16.5: VertexBuffer.draw(renderStack.getLast().getMatrix(), GL_QUADS) multiplied the pose onto the active GL modelview
        vboR.drawWithShader(new Matrix4f(RenderSystem.getModelViewMatrix()).mul(renderStack.last().pose()), projection, shader);
        VertexBuffer.unbind();
        renderStack.popPose();

        renderStack.pushPose();
        renderStack.mulPose(Axis.YP.rotationDegrees(20 + r));
        vboL.bind();
        vboL.drawWithShader(new Matrix4f(RenderSystem.getModelViewMatrix()).mul(renderStack.last().pose()), projection, shader);
        VertexBuffer.unbind();
        renderStack.popPose();

        BlockAtlasTexture.getInstance().bindTexture();
        RenderTypesAS.MODEL_DEMON_WINGS.clearRenderState();

        renderStack.popPose();
    }
}
