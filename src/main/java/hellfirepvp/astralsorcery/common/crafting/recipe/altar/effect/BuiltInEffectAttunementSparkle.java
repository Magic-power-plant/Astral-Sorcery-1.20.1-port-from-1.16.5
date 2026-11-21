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
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.api.effect.EffectSpec;
import hellfirepvp.astralsorcery.common.effect.visual.spec.EffectTemplateIds;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BuiltInEffectAttunementSparkle
 * Created by HellFirePvP
 * Date: 24.09.2019 / 06:34
 */
public class BuiltInEffectAttunementSparkle extends AltarRecipeEffect {

    private static final EffectSpec SPARKLE = EffectSpec.builder(EffectTemplateIds.GENERIC_PARTICLE)
            .color(ColorsAS.ROCK_CRYSTAL.getRGB() & 0xFFFFFF).build();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTick(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state) {
        EffectSpec spec = SPARKLE.toBuilder()
                .motion(new Vector3(
                        rand.nextFloat() * 0.06 * (rand.nextBoolean() ? 1 : -1),
                        rand.nextFloat() * 0.06 * (rand.nextBoolean() ? 1 : -1),
                        rand.nextFloat() * 0.06 * (rand.nextBoolean() ? 1 : -1)))
                .scale(0.7F * rand.nextFloat() * 0.3F)
                .maxAge(20 + rand.nextInt(30)).build();
        Effects.of(spec, ClientEffectSpecs.REGISTRY).spawn(new Vector3(altar).add(0.5, 0.5, 0.5));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onTESR(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state, PoseStack renderStack, MultiBufferSource buffer, float pTicks, int combinedLight) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onCraftingFinish(AltarBlockEntity altar, boolean isChaining) {}
}
