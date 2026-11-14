/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.effect.function.RefreshFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXSpritePlane;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.common.crafting.ActiveAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectLuminescenceFlare
 * Created by HellFirePvP
 * Date: 27.09.2019 / 21:56
 */
public class EffectLuminescenceFlare extends AltarRecipeEffect {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTick(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state) {
        //1.16.5 typed this against ActiveSimpleAltarRecipe/SimpleAltarRecipe; TileAltar in this tree still hands out
        //the legacy ActiveAltarRecipe/AltarRecipe pair. getRecipe() is the rename of getRecipeToCraft(), and the
        //refresh predicate compares against ActiveAltarRecipe.CraftingState.ACTIVE - the same state the 'state'
        //parameter mirrors - so the effect lives exactly as long as it did in 1.16.5.
        //VERIFY_PORT[AS-ALT-01]: revert to ActiveSimpleAltarRecipe once TileAltar#getActiveRecipe() is migrated.
        ActiveAltarRecipe recipe = altar.getActiveRecipe();
        if (recipe != null && state == ActiveSimpleAltarRecipe.CraftingState.ACTIVE) {
            ResourceLocation recipeName = recipe.getRecipe().getId();

            FXSpritePlane spr = recipe.getEffectContained(INDEX_CRAFT_FLARE, i -> {
                return EffectHelper.of(EffectTemplatesAS.TEXTURE_SPRITE)
                        .spawn(new Vector3(altar).add(0.5, 0.04, 0.5))
                        .setSprite(SpritesAS.SPR_CRAFT_FLARE)
                        .setAxis(Vector3.RotAxis.Y_AXIS)
                        .setNoRotation(0)
                        .color(VFXColorFunction.constant(ColorsAS.EFFECT_CRAFT_FLARE))
                        .alpha(VFXAlphaFunction.fadeIn(30))
                        .setScaleMultiplier(9F)
                        .setAlphaMultiplier(0.65F)
                        .refresh(RefreshFunction.blockEntityExistsAnd(altar,
                                (tAltar, fx) -> tAltar.getActiveRecipe() != null &&
                                        tAltar.getActiveRecipe().getState() == ActiveAltarRecipe.CraftingState.ACTIVE &&
                                        recipeName.equals(tAltar.getActiveRecipe().getRecipe().getId())));
            });

            EffectHelper.refresh(spr, EffectTemplatesAS.TEXTURE_SPRITE);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTESR(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state, PoseStack renderStack, MultiBufferSource buffer, float pTicks, int combinedLight) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onCraftingFinish(AltarBlockEntity altar, boolean isChaining) {}
}
