/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.interaction.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.util.LightmapUtil;
//1.16.5 imported hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction and
//hellfirepvp.astralsorcery.common.crafting.recipe.interaction.{InteractionResult, ResultSpawnEntity}; this
//tree keeps all three as top-level classes in common/crafting (tracked as TODO_PORT[AS-PKG-01]).
import hellfirepvp.astralsorcery.common.crafting.InteractionResult;
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.ResultSpawnEntity;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIHandlerSpawnEntity
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:51
 */
public class JEIHandlerSpawnEntity extends JEIInteractionResultHandler {

    //1.16.5 had an empty addToRecipeLayout and an empty addToRecipeIngredients
    //(1.16.5 .../JEIHandlerSpawnEntity.java:35, :41); JEI 15 merged the two, and the merged body is still
    //empty - a spawned entity is drawn in drawRecipe rather than occupying a slot.
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addToRecipeLayout(IRecipeLayoutBuilder builder, LiquidInteraction recipe) {

    }

    //1.16.5 signature: drawRecipe(LiquidInteraction recipe, MatrixStack renderStack, double mouseX,
    //double mouseY) (1.16.5 .../JEIHandlerSpawnEntity.java:47). JEI 15 hands a category a GuiGraphics, whose
    //pose() is the 1.16.5 MatrixStack, so the only caller
    //(common/integration/jei/CategoryLiquidInteraction#draw) can pass it straight through. The body itself is
    //a complete port.
    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawRecipe(LiquidInteraction recipe, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        InteractionResult result = recipe.getResult();
        if (!(result instanceof ResultSpawnEntity)) {
            return;
        }
        //1.16.5: Minecraft#world - renamed to level in 1.20.1.
        Entity le = ((ResultSpawnEntity) result).getEntityType().create(Minecraft.getInstance().level);
        if (!(le instanceof LivingEntity)) {
            return;
        }

        PoseStack poseStack = guiGraphics.pose();
        //1.16.5: MatrixStack#push/pop -> PoseStack#pushPose/popPose, #rotate(Quaternion) -> #mulPose, and
        //Vector3f.XP/YP.rotationDegrees moved onto com.mojang.math.Axis in 1.20.1.
        poseStack.pushPose();
        poseStack.translate(55, 35, 500);
        poseStack.scale(15, 15, 15);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(145));
        //1.16.5: IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer()) - the type became
        //MultiBufferSource.BufferSource, the factory MultiBufferSource#immediate and Tessellator#getBuffer
        //Tesselator#getBuilder in 1.20.1.
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        //1.16.5: Minecraft#getRenderManager().renderEntityStatic(...) - the dispatcher getter is
        //getEntityRenderDispatcher() and the method plain render(...) in 1.20.1.
        Minecraft.getInstance().getEntityRenderDispatcher()
                .render(le, 0, 0, 0, 0, 0, poseStack, buffer, LightmapUtil.getPackedFullbrightCoords());
        //1.16.5: IRenderTypeBuffer.Impl#finish - renamed to endBatch in 1.20.1.
        buffer.endBatch();
        poseStack.popPose();
    }
}
