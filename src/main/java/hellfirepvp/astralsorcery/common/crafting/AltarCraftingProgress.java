/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import net.minecraft.nbt.CompoundTag;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarCraftingProgress
 * Created by HellFirePvP
 * Date: 13.08.2019 / 21:05
 */
public interface AltarCraftingProgress {

    boolean tryProcess(AltarBlockEntity altar, ActiveAltarRecipe currentRecipe, CompoundTag data, int ticksCrafting, int totalCraftingTime);

    //TODO_PORT[AS-ALT-03]: 1.16.5 declared exactly one method, taking the one and only active-altar-recipe
    // type ActiveSimpleAltarRecipe (1.16.5 common/crafting/recipe/altar/AltarCraftingProgress.java:23). This
    // tree carries two divergent active-recipe classes - the live common.crafting.ActiveAltarRecipe that
    // TileAltar drives, and the reference-faithful
    // common.crafting.recipe.altar.ActiveSimpleAltarRecipe - and both tick() implementations have to be able
    // to reach this hook, so the method is declared twice. Nothing in the mod implements
    // AltarCraftingProgress (neither in 1.16.5 nor here); it is purely an addon extension point, which is
    // why the ActiveSimpleAltarRecipe form can default to "keep going". Collapse the two back into one
    // method once the two recipe hierarchies are merged.
    default boolean tryProcess(AltarBlockEntity altar, ActiveSimpleAltarRecipe currentRecipe, CompoundTag data, int ticksCrafting, int totalCraftingTime) {
        return true;
    }
}
