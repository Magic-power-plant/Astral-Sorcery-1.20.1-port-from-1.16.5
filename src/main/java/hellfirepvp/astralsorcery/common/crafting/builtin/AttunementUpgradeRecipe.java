/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builtin;

import hellfirepvp.astralsorcery.common.crafting.ActiveAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.crafting.AltarUpgradeRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.lib.AltarRecipeEffectsAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttunementUpgradeRecipe
 * Created by HellFirePvP
 * Date: 27.09.2019 / 22:23
 */
//1.16.5 declares the same AltarUpgradeRecipe marker interface (common/crafting/recipe/altar/
//AltarUpgradeRecipe.java) but none of the three builtin upgrade recipes actually implement it there - only
//client/screen/journal/page/RenderPageRecipeTemplate:258 tests for it. This tree's upgrade recipes do
//implement it, which is additive and changes no behaviour of the ported members below.
public class AttunementUpgradeRecipe extends AltarRecipe implements AltarUpgradeRecipe {

    public AttunementUpgradeRecipe(ResourceLocation id, AltarType altarType, AltarRecipeGrid grid, List<ItemStack> outputs, int starlightRequired, int craftingTime) {
        super(id, altarType, grid, outputs, starlightRequired, craftingTime);

        //AS-ALT-02 (resolved): 1.16.5 common/crafting/recipe/altar/builtin/AttunementUpgradeRecipe.java:41.
        //AltarRecipeEffectsAS.UPGRADE_ALTAR is assigned by RegistryRecipeTypes#initAltarEffects during
        //RegisterEvent, i.e. before the datapack builds this recipe, so it is no longer null here.
        this.addAltarEffect(AltarRecipeEffectsAS.UPGRADE_ALTAR);
    }

    public static AttunementUpgradeRecipe convertToThis(AltarRecipe other) {
        AttunementUpgradeRecipe recipe = new AttunementUpgradeRecipe(other.getId(), other.getAltarType(), other.getGrid(), other.getOutputs(), other.getStarlightRequired(), other.getCraftingTime());
        recipe.copyCommonPropertiesFrom(other);
        return recipe;
    }

    @Override
    public boolean matches(LogicalSide side, Player crafter, TileAltar altar, boolean ignoreStarlightRequirement) {
        return altar.getAltarType() == AltarType.DISCOVERY && super.matches(side, crafter, altar, ignoreStarlightRequirement);
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputsForAltar(TileAltar altar) {
        return Collections.emptyList();
    }

    @Override
    public void onRecipeCompletion(TileAltar altar, ActiveAltarRecipe activeRecipe) {
        super.onRecipeCompletion(altar, activeRecipe);

        //1.16.5 .../AttunementUpgradeRecipe.java:64. This tree's ResearchManager#informCraftedAltar is typed
        //against the live ActiveAltarRecipe and performs the very same two steps as 1.16.5's: resolve the
        //crafting player off the active recipe, informCrafted(..) and AdvancementsAS.ALTAR_CRAFT.trigger(..)
        //(common/data/research/ResearchManager.java:385-396). BlocksAS holds RegistryObjects in this tree,
        //hence the extra get().
        ResearchManager.informCraftedAltar(altar, activeRecipe, new ItemStack(BlocksAS.ALTAR_ATTUNEMENT.get()));
        if (altar.getLevel() != null) {
            //1.16.5: World#setBlockState + Block#getDefaultState + Constants.BlockFlags.DEFAULT (3) ->
            //Level#setBlock, Block#defaultBlockState and Block.UPDATE_ALL in 1.20.1. BlocksAS holds
            //RegistryObjects in this tree, hence the extra get().
            altar.getLevel().setBlock(altar.getBlockPos(), BlocksAS.ALTAR_ATTUNEMENT.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
