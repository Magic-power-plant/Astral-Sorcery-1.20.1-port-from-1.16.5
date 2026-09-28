/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.wand;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.entity.technical.EntityGrapplingHook;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemGrappleWand
 * Created by HellFirePvP
 * Date: 29.02.2020 / 18:15
 */
public class ItemGrappleWand extends Item implements AlignmentChargeConsumer {

    private static final float COST_PER_GRAPPLE = 450F;

    /**
     * 1.16.5: {@code super(new Properties().maxStackSize(1).group(CommonProxy.ITEM_GROUP_AS))}; in 1.20.1 the
     * properties are handed in by {@code ItemsAS} and creative tabs are data driven, so only the stack size remains.
     */
    public ItemGrappleWand(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        //1.16.5: player.getCooldownTracker().hasCooldown(this)
        return player.getCooldowns().isOnCooldown(this) ? 0 : COST_PER_GRAPPLE;
    }

    //1.16.5: onItemRightClick(World, PlayerEntity, Hand) - renamed to use(...) in 1.20.1
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack held = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide() || held.isEmpty()) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, held);
        }
        if (!playerIn.getCooldowns().isOnCooldown(this) &&
                AlignmentChargeHandler.INSTANCE.drainCharge(playerIn, LogicalSide.SERVER, COST_PER_GRAPPLE, false)) {
            //1.16.5: worldIn.addEntity(entity)
            worldIn.addFreshEntity(new EntityGrapplingHook(playerIn, worldIn));
            playerIn.getCooldowns().addCooldown(this, 40);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, held);
    }
}
