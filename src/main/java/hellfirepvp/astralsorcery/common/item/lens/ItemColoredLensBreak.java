/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.lens;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.auxiliary.BlockBreakHelper;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.math.PartialEffectExecutor;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemColoredLensBreak
 * Created by HellFirePvP
 * Date: 21.09.2019 / 18:30
 */
public class ItemColoredLensBreak extends ItemColoredLens {

    private static final ColorTypeBreak COLOR_TYPE_BREAK = new ColorTypeBreak();

    public ItemColoredLensBreak(Properties properties) {
        super(COLOR_TYPE_BREAK, properties);
    }

    private static class ColorTypeBreak extends LensColorType {

        private ColorTypeBreak() {
            super(AstralSorcery.key("break"),
                    TargetType.BLOCK,
                    () -> new ItemStack(ItemsAS.COLORED_LENS_BREAK.get()),
                    ColorsAS.COLORED_LENS_BREAK,
                    0.1F,
                    false);
        }

        @Override
        public void entityInBeam(Level world, Vector3 origin, Vector3 target, Entity entity, PartialEffectExecutor executor) {}

        @Override
        public void blockInBeam(Level world, BlockPos pos, BlockState state, PartialEffectExecutor executor) {
            if (world.isClientSide()) {
                return;
            }

            boolean ranOnce = executor.executeAll(() -> {
                BlockBreakHelper.addProgress(world, pos, 0.4F, () -> {
                    // 1.16.5 BlockState#getBlockHardness(World, BlockPos) -> 1.20.1 BlockState#getDestroySpeed(BlockGetter, BlockPos)
                    float hardness = state.getDestroySpeed(world, pos);
                    if (hardness < 0) {
                        return null;
                    }
                    return hardness * Math.max(1, getHarvestLevel(state));
                });
            });
            if (ranOnce) {
                PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.BEAM_BREAK)
                        .addData((buf) -> {
                            ByteBufUtils.writePos(buf, pos);
                            // 1.16.5 Block#getStateId(BlockState) was renamed to Block#getId(BlockState).
                            buf.writeInt(Block.getId(state));
                        });
                PacketChannel.sendToAllAround(pkt, world, new Vector3(pos).toVector3d(), 16);
            }
        }

        /**
         * 1.16.5 exposed BlockState#getHarvestLevel(); 1.20.1 removed the per-state harvest level entirely and
         * expresses the same information through the vanilla/forge "needs_*_tool" block tags.
         * The returned values mirror the 1.16.5 numeric tiers (0 = wood/gold, 1 = stone, 2 = iron, 3 = diamond, 4 = netherite).
         */
        private static int getHarvestLevel(BlockState state) {
            if (state.is(Tags.Blocks.NEEDS_NETHERITE_TOOL)) {
                return 4;
            }
            if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
                return 3;
            }
            if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
                return 2;
            }
            if (state.is(BlockTags.NEEDS_STONE_TOOL)) {
                return 1;
            }
            return 0;
        }
    }
}
