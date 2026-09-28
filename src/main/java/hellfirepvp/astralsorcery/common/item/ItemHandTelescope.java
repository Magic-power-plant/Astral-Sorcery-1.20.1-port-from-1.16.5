/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.GuiType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemHandTelescope
 * Created by HellFirePvP
 * Date: 15.02.2020 / 16:30
 */
public class ItemHandTelescope extends Item {

    // 1.16.5 built its own Properties here with maxStackSize(1).group(CommonProxy.ITEM_GROUP_AS).
    // 1.20.1 has no Properties#group, so the properties (incl. the stack size) are supplied by ItemsAS
    // and the creative tab is applied centrally.
    public ItemHandTelescope(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return InteractionResultHolder.success(held);
        }
        if (world.isClientSide()) {
            AstralSorcery.getProxy().openGui(player, GuiType.HAND_TELESCOPE);
        }
        return InteractionResultHolder.success(held);
    }
}
