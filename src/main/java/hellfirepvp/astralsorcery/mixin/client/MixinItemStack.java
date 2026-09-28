/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin.client;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentHelper;
import hellfirepvp.astralsorcery.common.perk.DynamicModifierHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinItemStack
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5 -&gt; 1.20.1 renames: ItemStack#getTooltip -&gt; ItemStack#getTooltipLines,
 * ItemStack#getEnchantmentTagList -&gt; ItemStack#getEnchantmentTags,
 * ITooltipFlag -&gt; TooltipFlag, ITextComponent -&gt; Component,
 * new TranslationTextComponent(..) -&gt; Component.translatable(..),
 * ITextComponent#mergeStyle(TextFormatting) -&gt; Component#withStyle(ChatFormatting),
 * Enchantment#getDisplayName(int) -&gt; Enchantment#getFullname(int).
 * Local 3 of getTooltipLines is still the List&lt;Component&gt; accumulator and the first
 * ItemStack#hasTag() call still sits right after it is filled (verified with javap:
 * offset 151 is the first invokevirtual hasTag, offset 197 the first getEnchantmentTags).
 */
@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasTag()Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void addMissingEnchantmentTooltip(Player player, TooltipFlag advanced, CallbackInfoReturnable<List<Component>> cir, List<Component> tooltip) {
        ItemStack stack = (ItemStack) (Object) this;

        List<Component> addition = new ArrayList<>();
        try {
            //Add any dynamic modifiers this item has.
            DynamicModifierHelper.addModifierTooltip(stack, addition);

            //Add prism enchantments
            Map<Enchantment, Integer> enchantments;
            if (!stack.hasTag() && !(enchantments = EnchantmentHelper.getEnchantments(stack)).isEmpty()) {
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    addition.add(entry.getKey().getFullname(entry.getValue()));
                }
            }
        } catch (Exception exc) {
            addition.clear();
            tooltip.add(Component.translatable("astralsorcery.misc.tooltipError").withStyle(ChatFormatting.GRAY));
        }
        tooltip.addAll(addition);
    }

    @Redirect(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentTags()Lnet/minecraft/nbt/ListTag;"))
    public ListTag enhanceEnchantmentTooltip(ItemStack stack) {
        return DynamicEnchantmentHelper.modifyEnchantmentTags(stack.getEnchantmentTags(), stack);
    }
}
