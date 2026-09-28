/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.dust;

import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemUsableDust
 * Created by HellFirePvP
 * Date: 17.08.2019 / 08:44
 */
public abstract class ItemUsableDust extends Item implements DispenseItemBehavior {

    // 1.16.5 built its own Properties here with group(CommonProxy.ITEM_GROUP_AS).
    // 1.20.1 has no Properties#group, so the properties are supplied by ItemsAS
    // and the creative tab is applied centrally.
    public ItemUsableDust(Properties properties) {
        super(properties);
    }

    abstract boolean dispense(BlockSource dispenser);

    abstract boolean rightClickAir(Level world, Player player, ItemStack dust);

    abstract boolean rightClickBlock(UseOnContext ctx);

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!ctx.getLevel().isClientSide()) {
            if (this.rightClickBlock(ctx)) {
                if (!ctx.getPlayer().isCreative()) {
                    ctx.getItemInHand().shrink(1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && !world.isClientSide()) {
            if (this.rightClickAir(world, player, held)) {
                if (!player.isCreative()) {
                    held.shrink(1);
                }
            }
        }

        return InteractionResultHolder.success(held);
    }

    @Override
    public ItemStack dispense(BlockSource src, ItemStack stack) {
        if (this.dispense(src)) {
            stack.shrink(1);
        }
        return stack;
    }
}
