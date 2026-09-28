/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.tool;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CrystalToolTier
 * Created by HellFirePvP
 * Date: 17.08.2019 / 16:13
 * <p>
 * 1.16.5 implemented {@code IItemTier}; 1.20.1 renamed the interface to {@link Tier} and every one of its methods:
 * {@code getMaxUses -> getUses}, {@code getEfficiency -> getSpeed}, {@code getAttackDamage -> getAttackDamageBonus},
 * {@code getHarvestLevel -> getLevel}, {@code getEnchantability -> getEnchantmentValue} and
 * {@code getRepairMaterial -> getRepairIngredient}. All returned values are the unchanged 1.16.5 ones.
 * <p>
 * The tier is deliberately not registered with Forge's {@code TierSortingRegistry}: for unregistered tiers
 * {@code TierSortingRegistry#isCorrectTierForDrops} falls back to comparing {@link #getLevel()} against the vanilla
 * {@code needs_stone_tool}/{@code needs_iron_tool}/{@code needs_diamond_tool} tags, which is exactly the harvest
 * level semantics 1.16.5 had for harvest level 3 (diamond).
 */
public class CrystalToolTier implements Tier {

    private static final CrystalToolTier INSTANCE = new CrystalToolTier();

    private CrystalToolTier() {}

    public static CrystalToolTier getInstance() {
        return INSTANCE;
    }

    @Override
    public int getUses() {
        return 16192;
    }

    @Override
    public float getSpeed() {
        return 4.5F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 3.5F;
    }

    @Override
    public int getLevel() {
        return 3;
    }

    @Override
    public int getEnchantmentValue() {
        return 24;
    }

    @Nonnull
    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}
