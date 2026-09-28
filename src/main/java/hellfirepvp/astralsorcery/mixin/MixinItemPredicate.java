/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentHelper;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinItemPredicate
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5: ItemPredicate#test(ItemStack) built the "worn" enchantment map with
 *         EnchantmentHelper.getEnchantments(stack.getEnchantmentTagList()) - which the MCP name
 *         "deserializeEnchantments" referred to - and stored it in local 2.
 * 1.20.1: ItemPredicate#matches(ItemStack) uses ItemStack#getAllEnchantments() for the item's own
 *         enchantments (offset 131 -> astore_2, verified with javap) and only uses
 *         EnchantmentHelper#deserializeEnchantments for the *stored* (enchanted book) list further
 *         down. Injecting on deserializeEnchantments - as the first port attempt did - would have
 *         enhanced the stored-book predicate instead of the item predicate, so the injection point
 *         moved to getAllEnchantments. Shift.BY 2 lands right after the astore that fills local 2,
 *         exactly like the 1.16.5 version did.
 *         getAllEnchantments is the Forge extension IForgeItemStack#getAllEnchantments and is
 *         therefore absent from the official-&gt;SRG mappings (it keeps that name in production too),
 *         so the &#64;At needs remap = false while the enclosing &#64;Inject target
 *         (ItemPredicate#matches -&gt; m_35907_) must still be remapped.
 */
@Mixin(ItemPredicate.class)
public class MixinItemPredicate {

    @Inject(
            method = "matches",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getAllEnchantments()Ljava/util/Map;",
                    ordinal = 0,
                    shift = At.Shift.BY,
                    by = 2,
                    remap = false),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void enhanceEnchantmentList(ItemStack item, CallbackInfoReturnable<Boolean> cir, Map<Enchantment, Integer> enchantments) {
        DynamicEnchantmentHelper.addNewLevels(enchantments, item);
    }

}
