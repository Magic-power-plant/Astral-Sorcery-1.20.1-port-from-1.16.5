/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.effect.ClientEffectSpecs;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.api.effect.EffectSpec;
import hellfirepvp.astralsorcery.common.effect.visual.spec.EffectTemplateIds;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectCommonRandomLightbeams
 * Created by HellFirePvP
 * Date: 23.09.2019 / 20:51
 */
public class EffectAltarDefaultLightbeams extends AltarRecipeEffect {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTick(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state) {
        if (state == ActiveSimpleAltarRecipe.CraftingState.ACTIVE &&
                rand.nextInt(8) == 0) {
            float scale = (float) getRandomPillarOffset(altar.getAltarType()).getX();

            Vector3 from = new Vector3(altar).add(0.5, 0, 0.5);
            MiscUtils.applyRandomOffset(from, rand, scale * 0.85F);
            from.setY(altar.getBlockPos().getY() - 0.6F);

            EffectSpec spec = EffectSpec.builder(EffectTemplateIds.LIGHTBEAM)
                    .beam(new Vector3(0, 5 + rand.nextFloat() * 3, 0), 1, 1)
                    .maxAge(40 + rand.nextInt(30)).build();
            Effects.of(spec, ClientEffectSpecs.REGISTRY).spawn(from);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTESR(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state, PoseStack renderStack, MultiBufferSource buffer, float pTicks, int combinedLight) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onCraftingFinish(AltarBlockEntity altar, boolean isChaining) {}
}
