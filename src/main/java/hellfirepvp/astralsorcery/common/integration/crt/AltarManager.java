/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.crt;

import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.crafting.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Arrays;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarManager
 * Created by Jaredlll08
 * Date: 03.17.2021 / 15:36
 */
//BLOCKED_PORT[AS-INT-03]: 1.16.5 declared
//   @ZenRegister
//   @ZenCodeType.Name("mods.astralsorcery.AltarManager")
//   public class AltarManager implements IRecipeManager
// (1.16.5 AltarManager.java:35-37). No CraftTweaker artifact is on the compile classpath of this port
// (build.gradle declares no com.blamejared.crafttweaker dependency), so com.blamejared.crafttweaker.*
// and org.openzen.zencode.java.ZenCodeType cannot be imported: the two annotations, the interface,
// the @ZenCodeType.Method markers and IRecipeManager#fixRecipeName all have to wait for it.
// Parameter degradation rule used across this package: a CraftTweaker wrapper on which the reference
// only ever calls getInternal()/asVanillaIngredient() is replaced by the vanilla type it wraps
// (IItemStack -> ItemStack, IIngredient -> Ingredient), so the whole vanilla-side body stays ported;
// wrappers whose CraftTweaker-only methods are called degrade to Object, the convention this tree uses
// in common/crafting/recipe/interaction/jei/JEIInteractionResultHandler.java. Restoring the real
// signatures is then a pure type swap plus re-adding the unwrap call.
// Note for whoever unblocks this: CraftTweaker 1.20.1 restructured IRecipeManager into
// IRecipeManager<T extends Recipe<?>> and moved the actions to
// com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe.
//TODO_PORT[AS-PKG-01]: 1.16.5 read the altar type from
// hellfirepvp.astralsorcery.common.block.tile.altar.AltarType; this tree has it at
// hellfirepvp.astralsorcery.common.blockentity.altar.AltarType, and AltarRecipeGrid moved from
// common.crafting.recipe.altar to common.crafting.
public class AltarManager {

    /**
     * 1.16.5 signature:
     * void addRecipe(String name, String altarType, IItemStack output, IIngredient[][] ingredients,
     *                int duration, int starlightRequired)
     */
    public void addRecipe(String name, String altarType, ItemStack output, Ingredient[][] ingredients, int duration, int starlightRequired) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5: name = fixRecipeName(name);
        // (default method of CraftTweaker's IRecipeManager - it prefixes the script's namespace when
        // the given name has none and validates the remaining characters.)
        if (Arrays.stream(AltarType.values()).map(Enum::name).noneMatch(s -> s.equalsIgnoreCase(altarType))) {
            throw new IllegalArgumentException("Unknown Astral Sorcery Altar Type: " + altarType);
        }
        if (ingredients.length != 5) {
            throw new IllegalArgumentException("Astral Sorcery Altar ingredients needs to be a 5x5 array with all values filled. Use <item:minecraft:air> to pad it out!");
        }
        for (Ingredient[] ingredient : ingredients) {
            if (ingredient.length != 5) {
                throw new IllegalArgumentException("Astral Sorcery Altar ingredients needs to be a 5x5 array with all values filled. Use <item:minecraft:air> to pad it out!");
            }
        }

        AltarRecipeGrid.Builder builder = AltarRecipeGrid.builder();
        builder.patternLine("ABCDE").patternLine("FGHIJ").patternLine("KLMNO").patternLine("PQRST").patternLine("UVWXY");
        int index = 'A';
        for (Ingredient[] ingredient : ingredients) {
            // 1.16.5: builder.key((char) (index), iIngredient.asVanillaIngredient());
            for (Ingredient iIngredient : ingredient) {
                builder.key((char) (index), iIngredient);
                index++;
            }
        }

        SimpleAltarRecipe recipe = new SimpleAltarRecipe(new ResourceLocation(name), AltarType.valueOf(altarType.toUpperCase()), duration, starlightRequired, builder.build());
        // 1.16.5: recipe.addOutput(output.getInternal());
        recipe.addOutput(output);
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 (1.16.5 AltarManager.java:66):
        //   CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe));
        // The recipe itself is built exactly as upstream; only handing it to CraftTweaker is missing.
    }

    /**
     * 1.16.5 signature:
     * void addRecipe(String name, String altarType, IItemStack output, String[] pattern,
     *                Map<String, IIngredient> ingredients, int duration, int starlightRequired)
     */
    public void addRecipe(String name, String altarType, ItemStack output, String[] pattern, Map<String, Ingredient> ingredients, int duration, int starlightRequired) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5: name = fixRecipeName(name);
        if (Arrays.stream(AltarType.values()).map(Enum::name).noneMatch(s -> s.equalsIgnoreCase(altarType))) {
            throw new IllegalArgumentException("Unknown Astral Sorcery Altar Type: " + altarType);
        }
        if (pattern.length != 5) {
            throw new IllegalArgumentException("Astral Sorcery Altar ingredients needs to be a 5x5 array with all values filled.");
        }
        if (ingredients.keySet().stream().anyMatch(s -> s.length() != 1)) {
            throw new IllegalArgumentException("Cannot have multiple characters as pattern key!");
        }

        AltarRecipeGrid.Builder builder = AltarRecipeGrid.builder();
        for (String strings : pattern) {
            builder.patternLine(strings);
        }
        for (String character : ingredients.keySet()) {
            // 1.16.5: builder.key(character.charAt(0), ingredients.get(character).asVanillaIngredient());
            builder.key(character.charAt(0), ingredients.get(character));
        }

        SimpleAltarRecipe recipe = new SimpleAltarRecipe(new ResourceLocation(name), AltarType.valueOf(altarType.toUpperCase()), duration, starlightRequired, builder.build());
        // 1.16.5: recipe.addOutput(output.getInternal());
        recipe.addOutput(output);
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 (1.16.5 AltarManager.java:92):
        //   CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe));
    }

    /**
     * 1.16.5 @Override of IRecipeManager#removeRecipe(IItemStack)
     */
    public void removeRecipe(Object output) {
        // Not all recipes return an output, and they all require a TileAltar, which we don't have.
        throw new UnsupportedOperationException("Cannot remove Altar recipes by their output, remove them by their name instead!");
    }

    /**
     * 1.16.5 @Override of IRecipeManager#getRecipeType, which returned IRecipeType<SimpleAltarRecipe>
     * via RecipeTypesAS.TYPE_ALTAR.getType().
     */
    //TODO_PORT[AS-INT-06]: this tree's RecipeTypesAS registers RecipeType<AltarRecipe> under "altar"
    // rather than a ResolvingRecipeType over SimpleAltarRecipe, so the declared element type cannot be
    // SimpleAltarRecipe (1.16.5 AltarManager.java:102-103). The wildcard keeps the accessor honest
    // until common/crafting/recipe/SimpleAltarRecipe.java and RecipeTypesAS agree again.
    public RecipeType<?> getRecipeType() {
        return RecipeTypesAS.ALTAR.get();
    }
}
