package hellfirepvp.astralsorcery.api.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Client-only rendering callbacks for an altar recipe effect. */
@OnlyIn(Dist.CLIENT)
public interface IAltarRecipeEffectRenderer {
    void onTick(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state);
    void onTESR(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state,
                PoseStack renderStack, MultiBufferSource buffer, float partialTicks, int combinedLight);
    void onCraftingFinish(AltarBlockEntity altar, boolean isChaining);
}
