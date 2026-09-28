/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.armor;

import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ArmorMaterialImbuedLeather
 * Created by HellFirePvP
 * Date: 17.02.2020 / 19:16
 */
// 1.16.5 IArmorMaterial -> 1.20.1 ArmorMaterial (the vanilla enum of the same name moved to ArmorMaterials).
// 1.16.5 CommonProxy did `new ArmorMaterialImbuedLeather()`; the ported CommonProxy references the singleton
// below instead, so the constructor is kept private.
public class ArmorMaterialImbuedLeather implements ArmorMaterial {

    public static final ArmorMaterialImbuedLeather INSTANCE = new ArmorMaterialImbuedLeather();

    private ArmorMaterialImbuedLeather() {}

    // 1.16.5 getDurability(EquipmentSlotType) -> 1.20.1 getDurabilityForType(ArmorItem.Type);
    // the flat value is intentional, this material is not scaled by slot like the vanilla ones.
    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return 486;
    }

    // 1.16.5 getDamageReductionAmount(EquipmentSlotType) -> 1.20.1 getDefenseForType(ArmorItem.Type).
    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        switch (type) {
            case CHESTPLATE:
                return 7;
        }
        return 0;
    }

    @Override
    public int getEnchantmentValue() {
        return 24;
    }

    @Override
    public SoundEvent getEquipSound() {
        return ArmorMaterials.LEATHER.getEquipSound();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ItemsAS.STARDUST.get());
    }

    // 1.16.5 marked this @OnlyIn(Dist.CLIENT); in 1.20.1 ArmorMaterial#getName is part of the common interface
    // (ArmorItem uses it for the material key), so the annotation has to go.
    @Override
    public String getName() {
        return "imbued_leather";
    }

    @Override
    public float getToughness() {
        return 1.5F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0F;
    }
}
