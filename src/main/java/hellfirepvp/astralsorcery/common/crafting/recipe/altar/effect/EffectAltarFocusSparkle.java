/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.ActiveAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.effect.visual.Effects;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectAltarFocusSparkle
 * Created by HellFirePvP
 * Date: 27.09.2019 / 21:17
 */
public class EffectAltarFocusSparkle extends AltarRecipeEffect implements IFocusEffect {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTick(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state) {
        //1.16.5: TileAltar#getActiveRecipe() returns ActiveSimpleAltarRecipe and
        //recipe.getRecipeToCraft() returns SimpleAltarRecipe. In this tree TileAltar still exposes the
        //legacy ActiveAltarRecipe/AltarRecipe pair; getRecipe() is the rename of getRecipeToCraft()
        //and getFocusConstellation() is identical, so the behaviour is unchanged.
        //VERIFY_PORT[AS-ALT-01]: revert to ActiveSimpleAltarRecipe once TileAltar#getActiveRecipe() is migrated.
        ActiveAltarRecipe recipe = altar.getActiveRecipe();
        if (recipe == null) {
            return; //Uh... what
        }
        IConstellation focus = recipe.getRecipe().getFocusConstellation();

        double scale = getRandomPillarOffset(altar.getAltarType()).getX();
        double edgeScale = (scale * 2 + 1);
        Vector3 at = new Vector3(altar).add(-scale + rand.nextFloat() * edgeScale, 0, -scale + rand.nextFloat() * edgeScale);

        Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(at)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .color(VFXColorFunction.constant(getFocusColor(focus, rand)))
                .setScaleMultiplier(0.1F + rand.nextFloat() * 0.2F)
                .setMaxAge(50);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTESR(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state, PoseStack renderStack, MultiBufferSource buffer, float pTicks, int combinedLight) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onCraftingFinish(AltarBlockEntity altar, boolean isChaining) {}
}
