/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.builtin.AttunementUpgradeRecipe;
import hellfirepvp.astralsorcery.common.crafting.builtin.ConstellationBaseAverageStatsRecipe;
import hellfirepvp.astralsorcery.common.crafting.builtin.ConstellationBaseItemRecipe;
import hellfirepvp.astralsorcery.common.crafting.builtin.ConstellationBaseMergeStatsRecipe;
import hellfirepvp.astralsorcery.common.crafting.builtin.ConstellationBaseNBTCopyRecipe;
import hellfirepvp.astralsorcery.common.crafting.builtin.ConstellationCopyStatsRecipe;
import hellfirepvp.astralsorcery.common.crafting.builtin.ConstellationItemRecipe;
import hellfirepvp.astralsorcery.common.crafting.builtin.ConstellationUpgradeRecipe;
import hellfirepvp.astralsorcery.common.crafting.builtin.CrystalCountRecipe;
import hellfirepvp.astralsorcery.common.crafting.builtin.NBTCopyRecipe;
import hellfirepvp.astralsorcery.common.crafting.builtin.TraitUpgradeRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarRecipeTypeHandler
 * Created by HellFirePvP
 * Date: 27.09.2019 / 20:05
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this in common/crafting/recipe/altar; this tree has it at the top level
//of common/crafting. Not moved because the package layout is a tree-wide decision.
//1.16.5 types every Type over its common/crafting/recipe/SimpleAltarRecipe; this tree's live altar recipe is
//the divergent top-level AltarRecipe (TODO_PORT[AS-ALT-03]), so the type parameter bound follows that. The
//recipe-type names in JSON are byte-identical to 1.16.5.
public class AltarRecipeTypeHandler {

    public static final Type<AttunementUpgradeRecipe> ALTAR_UPGRADE_ATTUNEMENT = new Type<>(AstralSorcery.key("attunement_upgrade"));
    public static final Type<ConstellationUpgradeRecipe> ALTAR_UPGRADE_CONSTELLATION = new Type<>(AstralSorcery.key("constellation_upgrade"));
    public static final Type<TraitUpgradeRecipe> ALTAR_UPGRADE_TRAIT = new Type<>(AstralSorcery.key("trait_upgrade"));

    public static final Type<ConstellationBaseAverageStatsRecipe> CONSTELLATION_CRYSTAL_AVERAGE = new Type<>(AstralSorcery.key("constellation_base_average"));
    public static final Type<ConstellationBaseMergeStatsRecipe> CONSTELLATION_CRYSTAL_MERGE = new Type<>(AstralSorcery.key("constellation_base_merge"));
    public static final Type<ConstellationBaseItemRecipe> CONSTELLATION_ITEM_BASE = new Type<>(AstralSorcery.key("constellation_base"));
    public static final Type<ConstellationItemRecipe> CONSTELLATION_ITEM = new Type<>(AstralSorcery.key("constellation_item"));
    public static final Type<CrystalCountRecipe> CRYSTAL_SET_COUNT = new Type<>(AstralSorcery.key("crystal_count"));
    public static final Type<ConstellationBaseNBTCopyRecipe> CONSTELLATION_BASE_NBT_COPY = new Type<>(AstralSorcery.key("constellation_base_nbt_copy"));
    public static final Type<ConstellationCopyStatsRecipe> CONSTELLATION_COPY_CRYSTAL = new Type<>(AstralSorcery.key("constellation_copy_stats"));
    public static final Type<NBTCopyRecipe> NBT_COPY = new Type<>(AstralSorcery.key("nbt_copy"));
    public static final Type<AltarRecipe> DEFAULT = new Type<>(AstralSorcery.key("default"));

    private static final Map<ResourceLocation, Type<?>> CONVERTER_MAP = new HashMap<>();

    public static <T extends AltarRecipe> Type<T> registerConverter(ResourceLocation name, Function<AltarRecipe, T> converter) {
        Type<T> type = new Type<>(name, converter);
        CONVERTER_MAP.put(name, type);
        return type;
    }

    private static <T extends AltarRecipe> void registerInternal(Type<T> type, Function<AltarRecipe, T> converter) {
        type.converter = converter;
        CONVERTER_MAP.put(type.key, type);
    }

    @SuppressWarnings("unchecked")
    public static <T extends AltarRecipe> T convert(AltarRecipe recipe, ResourceLocation alternativeBase) {
        return (T) CONVERTER_MAP.getOrDefault(alternativeBase, DEFAULT).convert(recipe);
    }

    public static void init() {
        registerInternal(ALTAR_UPGRADE_ATTUNEMENT, AttunementUpgradeRecipe::convertToThis);
        registerInternal(ALTAR_UPGRADE_CONSTELLATION, ConstellationUpgradeRecipe::convertToThis);
        registerInternal(ALTAR_UPGRADE_TRAIT, TraitUpgradeRecipe::convertToThis);

        //The four crystal-stat converters below were missing in this tree: the recipe classes existed under
        //common/crafting/builtin but had no type key, so "constellation_base_average",
        //"constellation_base_merge", "crystal_count" and "constellation_copy_stats" recipe JSONs silently
        //fell through to DEFAULT and lost their crystal attribute handling (1.16.5
        //common/crafting/recipe/altar/AltarRecipeTypeHandler.java:65-73).
        registerInternal(CONSTELLATION_CRYSTAL_AVERAGE, ConstellationBaseAverageStatsRecipe::convertToThis);
        registerInternal(CONSTELLATION_CRYSTAL_MERGE, ConstellationBaseMergeStatsRecipe::convertToThis);
        registerInternal(CONSTELLATION_ITEM_BASE, ConstellationBaseItemRecipe::convertToThis);
        registerInternal(CONSTELLATION_ITEM, ConstellationItemRecipe::convertToThis);
        registerInternal(CRYSTAL_SET_COUNT, CrystalCountRecipe::convertToThis);
        registerInternal(CONSTELLATION_BASE_NBT_COPY, ConstellationBaseNBTCopyRecipe::convertToThis);
        registerInternal(CONSTELLATION_COPY_CRYSTAL, ConstellationCopyStatsRecipe::convertToThis);
        registerInternal(NBT_COPY, NBTCopyRecipe::convertToThis);
        registerInternal(DEFAULT, Function.identity());
    }

    public static class Type<T extends AltarRecipe> {

        private final ResourceLocation key;
        private Function<AltarRecipe, T> converter;

        private Type(ResourceLocation key) {
            this.key = key;
        }

        private Type(ResourceLocation key, Function<AltarRecipe, T> converter) {
            this.key = key;
            this.converter = converter;
        }

        public T convert(AltarRecipe recipe) {
            return this.converter.apply(recipe);
        }

        public final ResourceLocation getKey() {
            return key;
        }
    }
}
