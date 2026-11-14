/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectUpgradeAltar
 * Created by HellFirePvP
 * Date: 23.09.2019 / 18:06
 */
public class EffectUpgradeAltar extends AltarRecipeEffect {

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTick(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state) {
        //1.16.5: Minecraft.getInstance().particles.addBlockDestroyEffects(pos, state)
        //1.20.1: the field is ParticleEngine 'particleEngine' and the method is named 'destroy'.
        ParticleEngine mgr = Minecraft.getInstance().particleEngine;
        if (state == ActiveSimpleAltarRecipe.CraftingState.ACTIVE &&
                rand.nextInt(8) == 0) {
            mgr.destroy(altar.getBlockPos(), BlocksAS.MARBLE_RUNED.get().defaultBlockState());
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTESR(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state, PoseStack renderStack, MultiBufferSource buffer, float pTicks, int combinedLight) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onCraftingFinish(AltarBlockEntity altar, boolean isChaining) {}
}
