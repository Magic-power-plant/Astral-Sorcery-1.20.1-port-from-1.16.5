/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinEnchantmentHelper
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5 -&gt; 1.20.1 renames: getEnchantmentLevel -&gt; getItemEnchantmentLevel,
 * applyEnchantmentModifier -&gt; runIterationOnItem, getEnchantments stayed.
 */
@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {

    @Inject(method = "getItemEnchantmentLevel", at = @At("RETURN"), cancellable = true)
    private static void getEnhancedEnchantmentLevel(Enchantment enchID, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(DynamicEnchantmentHelper.getNewEnchantmentLevel(cir.getReturnValue(), enchID, stack, null));
    }

    /**
     * 1.16.5 equivalent:
     *
     * <pre>
     * &#64;Redirect(method = "applyEnchantmentModifier",
     *           at = &#64;At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getEnchantmentTagList()Lnet/minecraft/nbt/ListNBT;"))
     * private static ListNBT applyEnhancedEnchantmentsTag(ItemStack stack) {
     *     return DynamicEnchantmentHelper.modifyEnchantmentTags(stack.getEnchantmentTagList(), stack);
     * }
     * </pre>
     *
     * In 1.20.1 Forge patches the method (renamed to runIterationOnItem) so that it no longer walks
     * the raw NBT list; verified with javap against forge-1.20.1-47.4.20:
     *
     * <pre>
     * private static void runIterationOnItem(EnchantmentVisitor visitor, ItemStack stack) {
     *     if (!stack.isEmpty()) {
     *         for (Map.Entry&lt;Enchantment, Integer&gt; e : stack.getAllEnchantments().entrySet()) {
     *             visitor.accept(e.getKey(), e.getValue());
     *         }
     *     }
     * }
     * </pre>
     *
     * ItemStack#getAllEnchantments is the Forge extension IForgeItemStack#getAllEnchantments (hence
     * remap = false; it keeps that name in the SRG runtime as well) and resolves to
     * EnchantmentHelper#deserializeEnchantments(stack.getEnchantmentTags()) - it deliberately does
     * NOT route through EnchantmentHelper#getEnchantments, so the getEnchantments injector below
     * does not cover this path and this redirect is genuinely required. The map handed back by
     * deserializeEnchantments is a fresh mutable LinkedHashMap, so DynamicEnchantmentHelper can
     * rewrite it in place exactly like modifyEnchantmentTags rewrote the ListNBT in 1.16.5.
     *
     * VERIFY_PORT[AS-MIX-01]: modifyEnchantmentTags additionally filters ADD_TO_SPECIFIC
     * enchantments through ItemStack#canApplyAtEnchantingTable while addNewLevels does not - that
     * difference already existed between the two helpers in 1.16.5 and is kept as-is.
     */
    @Redirect(
            method = "runIterationOnItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getAllEnchantments()Ljava/util/Map;",
                    remap = false)
    )
    private static Map<Enchantment, Integer> applyEnhancedEnchantmentsTag(ItemStack stack) {
        return DynamicEnchantmentHelper.addNewLevels(stack.getAllEnchantments(), stack);
    }

    @Inject(method = "getEnchantments", at = @At("RETURN"))
    private static void applyDeserializedEnhancedEnchantments(ItemStack stack, CallbackInfoReturnable<Map<Enchantment, Integer>> cir) {
        DynamicEnchantmentHelper.addNewLevels(cir.getReturnValue(), stack);
    }
}
