/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.research;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import hellfirepvp.astralsorcery.common.data.journal.JournalPageBlockTransmutation;
import hellfirepvp.astralsorcery.common.data.journal.JournalPageLiquidInfusion;
import hellfirepvp.astralsorcery.common.data.journal.JournalPageRecipe;
import hellfirepvp.astralsorcery.common.data.journal.JournalPageStructure;
import hellfirepvp.astralsorcery.common.data.journal.JournalPageText;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Locale;
import java.util.function.Predicate;

/** Shared page factories retain recipe preference and translation-key semantics. */
final class ResearchPages {
    private ResearchPages() {}

    static JournalPage recipe(ItemLike outputItem) {
        return JournalPageRecipe.fromOutputPreferAltarRecipes(stack -> !stack.isEmpty() && stack.getItem().equals(outputItem.asItem()));
    }

    static JournalPage recipeVanilla(ItemLike outputItem) {
        return JournalPageRecipe.fromOutputPreferVanillaRecipes(stack -> !stack.isEmpty() && stack.getItem().equals(outputItem.asItem()));
    }

    static JournalPage recipeTransmutation(ItemLike outputItem) {
        return JournalPageBlockTransmutation.fromOutput(stack -> !stack.isEmpty() && stack.getItem().equals(outputItem.asItem()));
    }

    static JournalPage recipeInfusion(ItemLike outputItem) {
        return JournalPageLiquidInfusion.fromOutput(stack -> !stack.isEmpty() && stack.getItem().equals(outputItem.asItem()));
    }

    static JournalPage recipe(Predicate<ItemStack> itemStackFilter) {
        return JournalPageRecipe.fromOutputPreferAltarRecipes(itemStackFilter);
    }

    static JournalPage structure(StructureType structure) {
        return new JournalPageStructure(structure.getStructure());
    }

    static JournalPage text(String identifier) {
        return new JournalPageText(AstralSorcery.MODID.toLowerCase(Locale.ROOT) + ".journal." + identifier + ".text");
    }
}
