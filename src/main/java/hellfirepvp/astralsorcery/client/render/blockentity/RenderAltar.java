/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.util.ColorizationHelper;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.crafting.ActiveAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderAltar
 * Created by HellFirePvP
 * Date: 28.09.2019 / 22:05
 *
 * 1.16.5 -&gt; 1.20.1: TileEntityRendererDispatcher -&gt; BlockEntityRendererProvider.Context, IRenderTypeBuffer -&gt;
 * MultiBufferSource, MatrixStack#push/pop -&gt; PoseStack#pushPose/popPose, TileEntity#getWorld -&gt;
 * BlockEntity#getLevel, BlockPos#toLong -&gt; BlockPos#asLong. AltarType moved from
 * {@code common.block.tile.altar} to {@code common.blockentity.altar}.
 */
public class RenderAltar extends CustomBlockEntityRenderer<AltarBlockEntity> {

    public RenderAltar(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(AltarBlockEntity blockEntity, float pTicks, PoseStack renderStack, MultiBufferSource renderTypeBuffer, int combinedLight, int combinedOverlay) {
        if (blockEntity.getAltarType().isThisGEThan(AltarType.RADIANCE) && blockEntity.hasMultiblock()) {
            IConstellation cst = blockEntity.getFocusedConstellation();
            if (cst != null) {
                float dayAlpha = DayTimeHelper.getCurrentDaytimeDistribution(blockEntity.getLevel()) * 0.6F;

                int max = 3000;
                int t = (int) (ClientScheduler.getClientTick() % max);
                float halfAge = max / 2F;
                float tr = 1F - (Math.abs(halfAge - t) / halfAge);
                tr *= 1.3;

                RenderingConstellationUtils.renderConstellationIntoWorldFlat(
                        cst, renderStack, renderTypeBuffer,
                        new Vector3(0.5, 0.03, 0.5),
                        5.5 + tr,
                        2,
                        0.1F + dayAlpha);
            }
        }

        ActiveAltarRecipe recipe = blockEntity.getActiveRecipe();
        if (recipe != null) {
            //TODO_PORT[AS-TER-01]: 1.16.5 had a single ActiveSimpleAltarRecipe; the 1.20.1 tree currently carries two
            //parallel classes - common.crafting.ActiveAltarRecipe (what TileAltar#getActiveRecipe returns) and
            //common.crafting.recipe.altar.ActiveSimpleAltarRecipe (what AltarRecipeEffect#onTESR still takes). Both
            //declare the same CraftingState constants (ACTIVE, WAITING), so the state is bridged by name here rather
            //than dropping the crafting-effect pass. Remove the bridge once the two classes are unified.
            //(1.16.5 client/render/tile/RenderAltar.java:66-70)
            ActiveSimpleAltarRecipe.CraftingState state = ActiveSimpleAltarRecipe.CraftingState.valueOf(recipe.getState().name());
            recipe.getRecipe().getCraftingEffects()
                    .forEach(effect -> effect.onTESR(blockEntity, state, renderStack, renderTypeBuffer, pTicks, combinedLight));
        }

        if (blockEntity.getAltarType().isThisGEThan(AltarType.RADIANCE) && blockEntity.hasMultiblock()) {
            renderStack.pushPose();
            renderStack.translate(0.5F, 4.5F, 0.5F);

            long id = blockEntity.getBlockPos().asLong();
            if (recipe != null) {
                List<WrappedIngredient> traitInputs = recipe.getRecipe().getRelayInputs();
                if (!traitInputs.isEmpty()) {
                    int amount = 60 / traitInputs.size();
                    for (int i = 0; i < traitInputs.size(); i++) {
                        WrappedIngredient ingredient = traitInputs.get(i);
                        ItemStack traitInput = ingredient.getRandomMatchingStack(ClientScheduler.getClientTick());
                        Color color = ColorizationHelper.getColor(traitInput).orElse(ColorsAS.CELESTIAL_CRYSTAL);

                        RenderingDrawUtils.renderLightRayFan(renderStack, renderTypeBuffer, color, 0x1231943167156902L | id | (i * 0x5151L), 20, 2F, amount);
                    }
                } else {
                    RenderingDrawUtils.renderLightRayFan(renderStack, renderTypeBuffer, Color.WHITE, id * 31L, 15, 1.5F, 35);
                    RenderingDrawUtils.renderLightRayFan(renderStack, renderTypeBuffer, ColorsAS.CELESTIAL_CRYSTAL, id * 16L, 10, 1F, 25);
                }
                RenderingDrawUtils.renderLightRayFan(renderStack, renderTypeBuffer, Color.WHITE, id * 31L, 10, 1F, 10);
            } else {
                RenderingDrawUtils.renderLightRayFan(renderStack, renderTypeBuffer, Color.WHITE, id * 31L, 15, 1.5F, 35);
                RenderingDrawUtils.renderLightRayFan(renderStack, renderTypeBuffer, ColorsAS.CELESTIAL_CRYSTAL, id * 16L, 10, 1F, 25);
            }

            renderStack.popPose();
        }
    }
}
