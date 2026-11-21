/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectPillarLightbeams
 * Created by HellFirePvP
 * Date: 25.09.2019 / 18:57
 */
public class EffectPillarLightbeams extends AltarRecipeEffect {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTick(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state) {
        if (state == ActiveSimpleAltarRecipe.CraftingState.ACTIVE &&
                getClientTick() % 45 == 0) {
            Vector3 altarVec = new Vector3(altar).add(0.5, 0.5, 0.5);
            for (int i = 0; i < getPillarAmount(altar.getAltarType()); i++) {
                Vector3 pillar = getPillarOffset(altar.getAltarType(), i).add(0.5, 0.5, 0.5);
                Effects.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(altarVec)
                        .setup(pillar, 1.2F, 0.8F);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTESR(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state, PoseStack renderStack, MultiBufferSource buffer, float pTicks, int combinedLight) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onCraftingFinish(AltarBlockEntity altar, boolean isChaining) {}
}
