/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.dust;

import hellfirepvp.astralsorcery.common.entity.EntityIlluminationSpark;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.block.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemIlluminationPowder
 * Created by HellFirePvP
 * Date: 17.08.2019 / 13:54
 */
public class ItemIlluminationPowder extends ItemUsableDust {

    public ItemIlluminationPowder(Properties properties) {
        super(properties);
    }

    @Override
    boolean dispense(BlockSource dispenser) {
        // 1.16.5 IBlockSource#getBlockPos()/getWorld() -> 1.20.1 BlockSource#getPos()/getLevel()
        BlockPos at = dispenser.getPos();
        Direction face = dispenser.getBlockState().getValue(DispenserBlock.FACING);
        EntityIlluminationSpark nocSpark = new EntityIlluminationSpark(EntityTypesAS.ILLUMINATION_SPARK.get(), at.getX(), at.getY(), at.getZ(), dispenser.getLevel());
        nocSpark.shoot(face.getStepX(), face.getStepY() + 0.1F, face.getStepZ(), 0.7F, 0.9F);
        return dispenser.getLevel().addFreshEntity(nocSpark);
    }

    @Override
    boolean rightClickAir(Level world, Player player, ItemStack dust) {
        return world.addFreshEntity(new EntityIlluminationSpark(EntityTypesAS.ILLUMINATION_SPARK.get(), player, world));
    }

    @Override
    boolean rightClickBlock(UseOnContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        if (player == null) {
            return false;
        }

        if (!BlockUtils.isReplaceable(world, pos)) {
            pos = pos.relative(ctx.getClickedFace());
        }

        if (!BlockUtils.isReplaceable(world, pos)) {
            return false;
        }

        // 1.16.5 PlayerEntity#canPlayerEdit -> 1.20.1 Player#mayUseItemAt; World#getDimensionKey -> Level#dimension
        if (player.mayUseItemAt(pos, ctx.getClickedFace(), ctx.getItemInHand()) && !ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(world.dimension(), world, pos), ctx.getClickedFace())) {
            return world.setBlockAndUpdate(pos, BlocksAS.FLARE_LIGHT.get().defaultBlockState());
        }
        return false;
    }
}
