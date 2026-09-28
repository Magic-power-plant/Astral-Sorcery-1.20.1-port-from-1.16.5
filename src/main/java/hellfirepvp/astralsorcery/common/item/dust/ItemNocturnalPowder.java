/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.dust;

import hellfirepvp.astralsorcery.common.entity.EntityNocturnalSpark;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemNocturnalPowder
 * Created by HellFirePvP
 * Date: 17.08.2019 / 10:59
 */
public class ItemNocturnalPowder extends ItemUsableDust {

    public ItemNocturnalPowder(Properties properties) {
        super(properties);
    }

    @Override
    boolean dispense(BlockSource dispenser) {
        // 1.16.5 IBlockSource#getBlockPos()/getWorld() -> 1.20.1 BlockSource#getPos()/getLevel()
        BlockPos at = dispenser.getPos();
        Direction face = dispenser.getBlockState().getValue(DispenserBlock.FACING);
        EntityNocturnalSpark nocSpark = new EntityNocturnalSpark(EntityTypesAS.NOCTURNAL_SPARK.get(), at.getX(), at.getY(), at.getZ(), dispenser.getLevel());
        nocSpark.shoot(face.getStepX(), face.getStepY() + 0.1F, face.getStepZ(), 0.7F, 0.9F);
        return dispenser.getLevel().addFreshEntity(nocSpark);
    }

    @Override
    boolean rightClickAir(Level world, Player player, ItemStack dust) {
        return world.addFreshEntity(new EntityNocturnalSpark(EntityTypesAS.NOCTURNAL_SPARK.get(), player, world));
    }

    @Override
    boolean rightClickBlock(UseOnContext ctx) {
        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        EntityNocturnalSpark noc = new EntityNocturnalSpark(EntityTypesAS.NOCTURNAL_SPARK.get(), ctx.getPlayer(), ctx.getLevel());
        noc.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        noc.setSpawning();
        return ctx.getLevel().addFreshEntity(noc);
    }
}
